#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <termios.h>
#include <sys/ioctl.h>
#include <linux/spi/spidev.h>
#include "pic32.h"

#define SPI_SEND_FRM_SIZE 128
#define SPI_FRM_HEADER_SIZE 3

pthread_mutex_t pic32::mutex;

pic32* pic32::m_instance = NULL;

pic32* pic32::getInstance()
{
    if(NULL == m_instance) {
        mutex = PTHREAD_MUTEX_INITIALIZER;
        pthread_mutex_lock(&mutex);
        m_instance = new pic32();
        pthread_mutex_unlock(&mutex);
    }
    return m_instance;
}

pic32::pic32()
{
    config_changed = 0;
}

pic32::~pic32()
{
    netClose();
    NULL;
}

void pic32::netGpioPowerOn()
{
    system("echo 1 > /sys/class/gpio/gpio56/value");
    system("echo 1 > /sys/class/gpio/gpio91/value");
}

void pic32::netGpioPowerOff()
{
    system("echo 0 > /sys/class/gpio/gpio56/value");
	system("echo 0 > /sys/class/gpio/gpio91/value");
}

void pic32::netCsOn()
{
    system("echo 0 > /sys/class/gpio/gpio18/value");
}

void pic32::netCsOff()
{
    system("echo 1 > /sys/class/gpio/gpio18/value");
}

int pic32::netInit()
{
    int ret;
    uint8_t spi_mode = SPI_MODE;
    uint8_t spi_bits = SPI_BITS;
    uint32_t spi_speed = SPI_SPEED;

    LOGD("PIC32 netInit\n");

    spi_fd = open(SPI_DEV, O_RDWR | O_NOCTTY | O_NDELAY);
    if (spi_fd < 0)
    {
        LOGE("open %s failed\n", SPI_DEV);
        return spi_fd;
    }

    spi_mode |= SPI_MODE_0;     //
    spi_mode &= ~SPI_CS_HIGH;   //低电平片�?

    ret = ioctl(spi_fd, SPI_IOC_WR_MODE, &spi_mode);
    if (ret == -1)
    {
        LOGE("set spi write mode failed\n");
        close(spi_fd);
        spi_fd = -1;
        return ret;
    }
    ioctl(spi_fd, SPI_IOC_RD_MODE, &spi_mode);
    if (ret == -1)
    {
        LOGE("set spi read mode failed\n");
        close(spi_fd);
        spi_fd = -1;
        return ret;
    }
    ioctl(spi_fd, SPI_IOC_WR_BITS_PER_WORD, &spi_bits);
    if (ret == -1)
    {
        LOGE("set spi write bit failed\n");
        close(spi_fd);
        spi_fd = -1;
        return ret;
    }
    ioctl(spi_fd, SPI_IOC_RD_BITS_PER_WORD, &spi_bits);
    if (ret == -1)
    {
        LOGE("set spi read bit failed\n");
        close(spi_fd);
        spi_fd = -1;
        return ret;
    }
    ioctl(spi_fd, SPI_IOC_WR_MAX_SPEED_HZ, &spi_speed);
    if (ret == -1)
    {
        LOGE("set spi write speed failed\n");
        close(spi_fd);
        spi_fd = -1;
        return ret;
    }
    ioctl(spi_fd, SPI_IOC_RD_MAX_SPEED_HZ, &spi_speed);
    if (ret == -1)
    {
        LOGE("set spi read speed failed\n");
        close(spi_fd);
        spi_fd = -1;
        return ret;
    }

    memset(&tr, 0, sizeof(tr));
    tr.tx_buf = (unsigned long)send_buf;
    tr.rx_buf = (unsigned long)recv_buf;
    tr.len = 0;
    tr.delay_usecs = 0;
    tr.speed_hz = spi_speed;
    tr.bits_per_word = spi_bits;

    LOGD("PIC32 netInit OK\n");

    return 0;
}

int pic32::netReadVersion(int v [])
{
    int ret;

    if(-1 == spi_fd)
    {
        ret = netOpen();
        if(-1 == ret)
        {
            return ret;
        }
        sleep(4);
    }

    tr.len = 4;
    send_buf[0] = 0xD0;
    send_buf[1] = 0;
    send_buf[2] = 0;
    send_buf[3] = 0;
    recv_buf[1] = 0;
    recv_buf[2] = 0;
    recv_buf[3] = 0;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();
    if(-1 == ret)
    {
        LOGD("netReadVersion ioctl failed, errno[%d]\n", errno);
        return ret;
    }
    LOGD("recvbuf[1] : 0x%x\n", recv_buf[1]);

    if (recv_buf[1] == 0x99)
    {
        v[0] = recv_buf[2];
        v[1] = recv_buf[3];
        LOGD("The pic32 ver is %d.%d\n", v[0], v[1]);

        ret = 0;
    }
    else
    {
        ret = -1;
    }

    return ret;
}

void * pic32::spiConfigFunc(void *arg)
{
    uint8_t len = 0;
    int ret;

    NOTUSED(arg);

    //sleep 2 seconds for pic32 bootloader software update interval
    sleep(2);

    //if (pic32::getInstance()->config_changed == 1)
    {
        //帧格式： 0xAA + 控制域 + 数据长度 + 数据域 + 校验 + 0xEE
        pic32::getInstance()->send_buf[len++] = 0xAA; //start flag
        pic32::getInstance()->send_buf[len++] = 20;   //data len low
        pic32::getInstance()->send_buf[len++] = 0;    //data len high

        pic32::getInstance()->send_buf[len++] = pic32::getInstance()->config.local_ip.v[3];
        pic32::getInstance()->send_buf[len++] = pic32::getInstance()->config.local_ip.v[2];
        pic32::getInstance()->send_buf[len++] = pic32::getInstance()->config.local_ip.v[1];
        pic32::getInstance()->send_buf[len++] = pic32::getInstance()->config.local_ip.v[0];

        pic32::getInstance()->send_buf[len++] = pic32::getInstance()->config.sub_mask.v[3];
        pic32::getInstance()->send_buf[len++] = pic32::getInstance()->config.sub_mask.v[2];
        pic32::getInstance()->send_buf[len++] = pic32::getInstance()->config.sub_mask.v[1];
        pic32::getInstance()->send_buf[len++] = pic32::getInstance()->config.sub_mask.v[0];

        pic32::getInstance()->send_buf[len++] = pic32::getInstance()->config.gateway.v[3];
        pic32::getInstance()->send_buf[len++] = pic32::getInstance()->config.gateway.v[2];
        pic32::getInstance()->send_buf[len++] = pic32::getInstance()->config.gateway.v[1];
        pic32::getInstance()->send_buf[len++] = pic32::getInstance()->config.gateway.v[0];

        pic32::getInstance()->send_buf[len++] = pic32::getInstance()->config.protocol_type;
        pic32::getInstance()->send_buf[len++] = pic32::getInstance()->config.mode;
        pic32::getInstance()->send_buf[len++] = pic32::getInstance()->config.port & 0xff;
        pic32::getInstance()->send_buf[len++] = pic32::getInstance()->config.port >> 8;

        pic32::getInstance()->send_buf[len++] = pic32::getInstance()->config.remote_ip.v[3];
        pic32::getInstance()->send_buf[len++] = pic32::getInstance()->config.remote_ip.v[2];
        pic32::getInstance()->send_buf[len++] = pic32::getInstance()->config.remote_ip.v[1];
        pic32::getInstance()->send_buf[len++] = pic32::getInstance()->config.remote_ip.v[0];


        pic32::getInstance()->tr.len = len;        
        pic32::getInstance()->netCsOn();
        ret = ioctl(pic32::getInstance()->spi_fd, SPI_IOC_MESSAGE(1), &(pic32::getInstance()->tr));//执行spidev.c中ioctl的default进行数据传输
        pic32::getInstance()->netCsOff();
        if (ret < 0)
        {
            LOGE("write config failed\n");
            return NULL;
        }
		LOGD("set config sucessfully, local_ip:%x, mask:%x, gateway:%x, protocol:%d, mode:%d, port:%d, remote_ip:%x\n",
              pic32::getInstance()->config.local_ip.Val, pic32::getInstance()->config.sub_mask.Val, pic32::getInstance()->config.gateway.Val,
              pic32::getInstance()->config.protocol_type, pic32::getInstance()->config.mode, pic32::getInstance()->config.port, pic32::getInstance()->config.remote_ip.Val);
        sleep(2);
    }

    pic32::getInstance()->config_changed = 0;

    //begin to read config
    memset(pic32::getInstance()->send_buf, 0, 26);
    pic32::getInstance()->send_buf[0] = 0xCC; //start flag
    pic32::getInstance()->send_buf[1] = 23;
    pic32::getInstance()->send_buf[2] = 0;
    pic32::getInstance()->tr.len = 26;
    pic32::getInstance()->netCsOn();
    ret = ioctl(pic32::getInstance()->spi_fd, SPI_IOC_MESSAGE(1), &(pic32::getInstance()->tr));//执行spidev.c中ioctl的default进行数据传输
    pic32::getInstance()->netCsOff();
    if (ret < 0)
    {
        LOGE("read config failed\n");
        return NULL;
    }

    len = 0;
    while (pic32::getInstance()->recv_buf[len] != 0xCE && len < 25)
    {
        len++;
    }

    if ((pic32::getInstance()->recv_buf[len] == 0xCE) &&
        (pic32::getInstance()->recv_buf[len+1] == 20) &&
        (pic32::getInstance()->recv_buf[len+2] == 0))
    {
        pic32::getInstance()->config.local_ip.v[3] = pic32::getInstance()->recv_buf[len+3];
        pic32::getInstance()->config.local_ip.v[2] = pic32::getInstance()->recv_buf[len+4];
        pic32::getInstance()->config.local_ip.v[1] = pic32::getInstance()->recv_buf[len+5];
        pic32::getInstance()->config.local_ip.v[0] = pic32::getInstance()->recv_buf[len+6];
        pic32::getInstance()->config.sub_mask.v[3] = pic32::getInstance()->recv_buf[len+7];
        pic32::getInstance()->config.sub_mask.v[2] = pic32::getInstance()->recv_buf[len+8];
        pic32::getInstance()->config.sub_mask.v[1] = pic32::getInstance()->recv_buf[len+9];
        pic32::getInstance()->config.sub_mask.v[0] = pic32::getInstance()->recv_buf[len+10];
        pic32::getInstance()->config.gateway.v[3]  = pic32::getInstance()->recv_buf[len+11];
        pic32::getInstance()->config.gateway.v[2]  = pic32::getInstance()->recv_buf[len+12];
        pic32::getInstance()->config.gateway.v[1]  = pic32::getInstance()->recv_buf[len+13];
        pic32::getInstance()->config.gateway.v[0]  = pic32::getInstance()->recv_buf[len+14];

        pic32::getInstance()->config.protocol_type = pic32::getInstance()->recv_buf[len+15];
        pic32::getInstance()->config.mode          = pic32::getInstance()->recv_buf[len+16];

        pic32::getInstance()->config.port = pic32::getInstance()->recv_buf[len+17] + (uint16_t)(pic32::getInstance()->recv_buf[len+18] << 8);

        pic32::getInstance()->config.remote_ip.v[3] = pic32::getInstance()->recv_buf[len+19];
        pic32::getInstance()->config.remote_ip.v[2] = pic32::getInstance()->recv_buf[len+20];
        pic32::getInstance()->config.remote_ip.v[1] = pic32::getInstance()->recv_buf[len+21];
        pic32::getInstance()->config.remote_ip.v[0] = pic32::getInstance()->recv_buf[len+22];

		LOGD("read config sucessfully, local_ip:%x, mask:%x, gateway:%x, protocol:%d, mode:%d, port:%d, remote_ip:%x\n",
              pic32::getInstance()->config.local_ip.Val, pic32::getInstance()->config.sub_mask.Val, pic32::getInstance()->config.gateway.Val,
              pic32::getInstance()->config.protocol_type, pic32::getInstance()->config.mode, pic32::getInstance()->config.port, pic32::getInstance()->config.remote_ip.Val);
    }
    else
    {
        LOGE("read config failed\n");
    }

    return NULL;
}

int pic32::netOpen()
{
    int ret;
    pthread_t tidFrm;
    pthread_attr_t attrFrm;

    LOGI("Open the pic32\n");

    ret = netInit();

    if(-1 == ret)
    {
        LOGD("Open pic32 failed\n");
        return ret;
    }

    netGpioPowerOn();

    //create thread to config ip address
    pthread_attr_init(&attrFrm);
    pthread_attr_setdetachstate(&attrFrm, PTHREAD_CREATE_DETACHED);
    ret = pthread_create(&tidFrm, &attrFrm, spiConfigFunc, NULL);

    return spi_fd;
}

void pic32::netClose()
{
    int ret = -1;

    if(spi_fd >= 0) {
        close(spi_fd);
        spi_fd = -1;
    }

    netGpioPowerOff();
}

int pic32::netIsOpen()
{
    return spi_fd;
}

int pic32::netCheckConnected()
{
    int ver[2];
    int ret;

    ret = netReadVersion(ver);
    sleep(3);

    if(ver[0] > 0)
    {
        return 1;
    }

    return 0;
}

int pic32::netWrite(void *buf, int buf_len)
{
    uint8_t done = 0;
    uint16_t idx;
    uint16_t len = buf_len;
    uint16_t write_len;
    uint16_t ret;
	uint8_t *tmp = (uint8_t *)buf;

    if (tmp == NULL)
    {
        LOGE("input buffer null\n");
        return -1;
    }

    while (len > 0)
    {
        if (len > SPI_SEND_FRM_SIZE)
        {
            write_len = SPI_SEND_FRM_SIZE;
            memcpy(&send_buf[3], tmp, SPI_SEND_FRM_SIZE);
            tmp += SPI_SEND_FRM_SIZE;
            len -= SPI_SEND_FRM_SIZE;
        }
        else
        {
            write_len = len;
            memcpy(&send_buf[3], tmp, len);
            done = 1;
        }

        tr.len = 0;
        send_buf[tr.len++] = 0xBB;
        send_buf[tr.len++] = write_len & 0xFF;
        send_buf[tr.len++] = (write_len >> 8) & 0xFF;

        tr.len += write_len;

        netCsOn();
        ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);//执行spidev.c中ioctl的default进行数据传输
        netCsOff();
        if (ret < 0)
        {
            LOGE("write spi failed\n");
            return -1;
        }

        if (done == 1)
        {
            break;
        }
    }

    return buf_len;
}

int pic32::netRead(void *buf, int buf_len)
{
    uint16_t ret;
    uint16_t frm_len;
    uint16_t out_len = 0;
    uint8_t *recv = &recv_buf[3];
    uint8_t *end;

    if (buf == NULL)
    {
        LOGE("input buffer null\n");
        return -1;
    }

    if (buf_len > 4096)
    {
        buf_len = 4096;
    }

    tr.len = buf_len;
    memset(send_buf, 0, tr.len);
    send_buf[0] = 0xDD;
    send_buf[1] = (tr.len - SPI_FRM_HEADER_SIZE) & 0xFF;
    send_buf[2] = ((tr.len - SPI_FRM_HEADER_SIZE) >> 8) &0xFF;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);//执行spidev.c中ioctl的default进行数据传输
    netCsOff();
    if (ret < 0)
    {
        LOGE("read spi failed\n");
        return ret;
    }

    end = recv_buf+buf_len;
    while (recv < end)
    {
        if (*recv != 0xEE)
        {
            recv++;
            continue;
        }

		frm_len = *(recv+1) + (uint16_t)((*(recv+2))<<8);
		if ((frm_len == 0) || (frm_len > (SPI_BUF_SIZE-6)))
		{
			recv += SPI_FRM_HEADER_SIZE;
			continue;
		}

		if ((recv + SPI_FRM_HEADER_SIZE + frm_len) > (end))
		{
			LOGE("invalid frm_len:%d, data was truncated\n",frm_len);
    		memcpy((uint8_t *)buf+out_len, recv+SPI_FRM_HEADER_SIZE, end - recv - SPI_FRM_HEADER_SIZE);
    		out_len += frm_len;
			break;
		}

		memcpy((uint8_t *)buf+out_len, recv+SPI_FRM_HEADER_SIZE, frm_len);
		out_len += frm_len;
		recv += (SPI_FRM_HEADER_SIZE + frm_len);
    }

    return out_len;
}

void pic32::decodeEcryptData(char *data, int len, const char *pwd)
{
    int pwd_len;
    int idx;

    if (NULL == pwd)
    {
        pwd = "pic32mx664_ethernet_software_topscomm-shandong-qingdao-china,www.topscomm.com";
    }

    pwd_len = strlen(pwd);

    for (idx=0; idx < len; idx++)
    {
        data[idx] = data[idx] ^ (pwd[idx%pwd_len] - 0x30);
    }
}

int pic32::upRequest()
{
    uint8_t ret;
    int i = 0;

    tr.len = 0;
    send_buf[tr.len++] = 0x55;
    send_buf[tr.len++] = 0x55;
    send_buf[tr.len++] = 0x55;
    send_buf[tr.len++] = BFM_CTRL_REQUEST;
    tr.len++;   //last byte for ACK

    //delay 250ms to wait slave device up
    usleep(250000);

    //keep try for one second until slave accept upgrade
    while (i++ < 100)
    {
        recv_buf[tr.len - 1] = 0;

        netCsOn();
        ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
        netCsOff();

        ret = recv_buf[tr.len - 1];
        if (ret == 0x99)
        {
            printf("up request success\n");
            return ret;
        }
        else
        {
            usleep(10000);
        }
    }

    return 0;
}

char pic32::asciiToNum(char a)
{
    if (a >= '0' && a <= '9')
    {
        return (a - '0');
    }
    else if (a >= 'a' && a <= 'f')
    {
        return (a - 'a' + 0xa);
    }
    else if (a >= 'A' && a <= 'F')
    {
        return (a - 'A' + 0xa);
    }
    else
    {
        return 0;
    }
}

int pic32::upWrite(UP_FSM_INFO *fsm, int type)
{
    uint8_t *ret;
    int i = 0;

    tr.len = 0;
    send_buf[tr.len++] = 0x55;
    send_buf[tr.len++] = 0x55;
    send_buf[tr.len++] = 0x55;

    if (type == BFM_CTRL_WRITE_HALF_1)
    {
        //wait for previous page burn
        usleep(20000);

        send_buf[tr.len++] = BFM_CTRL_WRITE_HALF_1;
        send_buf[tr.len++] = (fsm->addr >> 16) & 0xff;
        send_buf[tr.len++] = (fsm->addr >> 8) & 0xff;
        send_buf[tr.len++] = fsm->addr & 0xff;

        memcpy(&send_buf[tr.len], fsm->data, 2048);

        tr.len += 2049; //2048 for data, 1 for ACK
    }
    else if (type == BFM_CTRL_WRITE_HALF_2)
    {
        send_buf[tr.len++] = BFM_CTRL_WRITE_HALF_2;

        memcpy(&send_buf[tr.len], fsm->data, 2048);

        tr.len += 2049;
    }

    ret = &recv_buf[tr.len - 1];

    while (i++ < 100)
    {
        *ret = 0;

        netCsOn();
        ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
        netCsOff();

        if (*ret == 0x99)
        {
            printf("write 0x%x success, type=%d, ret=%x\n", fsm->addr, type, *ret);
            return *ret;
        }
        usleep(1000);
    }

    LOGE("write 0x%x failed, type=%d\n", fsm->addr, type);

    return 0;
}

void pic32::upExec(UP_FSM_INFO *fsm)
{
    int i = 0;
    uint8_t ret;

    tr.len = 0;
    send_buf[tr.len++] = 0x55;
    send_buf[tr.len++] = 0x55;
    send_buf[tr.len++] = 0x55;
    send_buf[tr.len++] = BFM_CTRL_EXEC;

    send_buf[tr.len++] = (fsm->op_addr >> 16) & 0xff;
    send_buf[tr.len++] = (fsm->op_addr >> 8) & 0xff;
    send_buf[tr.len++] = fsm->op_addr & 0xff;
    tr.len++;   //for ACK

    while (i++ < 100)
    {
        recv_buf[tr.len - 1] = 0;

        netCsOn();
        ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
        netCsOff();

        ret = recv_buf[tr.len - 1];
        if (ret == 0x99)
        {
            break;
        }
        else
        {
            usleep(1000);
        }
    }
}

int pic32::upFsm(UP_FSM_INFO *fsm, int addr, char *data, int len)
{
    int i;
    char byte;
    int ret;

    switch (fsm->state)
    {
        case UP_START:
            fsm->addr = (addr & (~4095));
            fsm->op_addr = fsm->addr;
            fsm->data_len = 0;
            memset(fsm->data, 0xff, 2048);

            if (addr < (fsm->addr+0x800))
            {
                if (addr > fsm->addr)
                {
                    fsm->data_len = addr - fsm->addr;
                }
                for (i=0; i<len; i++)
                {
                    byte = (asciiToNum(data[i*2])<<4) + asciiToNum(data[i*2+1]);
                    fsm->data[fsm->data_len++] = byte;
                }
                fsm->state = UP_HALF_1;
            }
            else
            {
                ret = upWrite(fsm, BFM_CTRL_WRITE_HALF_1);
                if (ret == 0)
                {
                    return -1;
                }
                if (addr > (fsm->addr+0x800))
                {
                    fsm->data_len = addr - fsm->addr - 0x800;
                }
                for (i=0; i<len; i++)
                {
                    byte = (asciiToNum(data[i*2])<<4) + asciiToNum(data[i*2+1]);
                    fsm->data[fsm->data_len++] = byte;
                }
                fsm->state = UP_HALF_2;
            }
            break;
        case UP_HALF_1:
            if (addr >= 0x20000)
            {
                ret = upWrite(fsm, BFM_CTRL_WRITE_HALF_1);
                if (ret == 0)
                {
                    return -1;
                }
                memset(fsm->data, 0xff, 2048);
                ret = upWrite(fsm, BFM_CTRL_WRITE_HALF_2);
                if (ret == 0)
                {
                    return -1;
                }
                fsm->state = UP_DONE;
                break;
            }
            if (addr < (fsm->addr + 0x800))
            {
                if (addr > (fsm->addr+fsm->data_len))
                {
                    fsm->data_len = addr - fsm->addr;
                }
                for (i=0; i<len; i++)
                {
                    byte = (asciiToNum(data[i*2])<<4) + asciiToNum(data[i*2+1]);
                    fsm->data[fsm->data_len++] = byte;
                }
            }
            else if (addr < (fsm->addr + 0x1000))
            {
                ret = upWrite(fsm, BFM_CTRL_WRITE_HALF_1);
                if (ret == 0)
                {
                    return -1;
                }
                fsm->data_len = 0;
                memset(fsm->data, 0xff, 2048);
                if (addr > (fsm->addr + 0x800))
                {
                    fsm->data_len = addr - (fsm->addr + 0x800);
                }

                for (i=0; i<len; i++)
                {
                    byte = (asciiToNum(data[i*2])<<4) + asciiToNum(data[i*2+1]);
                    fsm->data[fsm->data_len++] = byte;
                }
                fsm->state = UP_HALF_2;
            }
            else
            {
                ret = upWrite(fsm, BFM_CTRL_WRITE_HALF_1);
                if (ret == 0)
                {
                    return -1;
                }
                memset(fsm->data, 0xff, 2048);
                ret = upWrite(fsm, BFM_CTRL_WRITE_HALF_2);
                if (ret == 0)
                {
                    return -1;
                }

                fsm->addr = (addr & (~4095));
                fsm->data_len = 0;
                memset(fsm->data, 0xff, 2048);

                if (addr < (fsm->addr+0x800))
                {
                    if (addr > fsm->addr)
                    {
                        fsm->data_len = addr - fsm->addr;
                    }
                    for (i=0; i<len; i++)
                    {
                        byte = (asciiToNum(data[i*2])<<4) + asciiToNum(data[i*2+1]);
                        fsm->data[fsm->data_len++] = byte;
                    }
                    fsm->state = UP_HALF_1;
                }
                else
                {
                    memset(fsm->data, 0xff, 2048);
                    ret = upWrite(fsm, BFM_CTRL_WRITE_HALF_1);
                    if (ret == 0)
                    {
                        return -1;
                    }
                    if (addr > (fsm->addr+0x800))
                    {
                        fsm->data_len = addr - fsm->addr - 0x800;
                    }
                    for (i=0; i<len; i++)
                    {
                        byte = (asciiToNum(data[i*2])<<4) + asciiToNum(data[i*2+1]);
                        fsm->data[fsm->data_len++] = byte;
                    }
                    fsm->state = UP_HALF_2;
                }
            }
            break;
        case UP_HALF_2:
            if (addr >= 0x20000)
            {
                ret = upWrite(fsm, BFM_CTRL_WRITE_HALF_2);
                if (ret == 0)
                {
                    return -1;
                }
                fsm->state = UP_DONE;
                break;
            }

            if (addr < (fsm->addr + 0x1000))
            {
                if ((addr - 0x800) > (fsm->addr+fsm->data_len))
                {
                    fsm->data_len = addr - fsm->addr - 0x800;
                }
                for (i=0; i<len; i++)
                {
                    byte = (asciiToNum(data[i*2])<<4) + asciiToNum(data[i*2+1]);
                    fsm->data[fsm->data_len++] = byte;
                }
            }
            else
            {
                ret = upWrite(fsm, BFM_CTRL_WRITE_HALF_2);
                if (ret == 0)
                {
                    return -1;
                }

                fsm->addr = (addr & (~4095));
                fsm->data_len = 0;
                memset(fsm->data, 0xff, 2048);

                if (addr < (fsm->addr+0x800))
                {
                    if (addr > fsm->addr)
                    {
                        fsm->data_len = addr - fsm->addr;
                    }
                    for (i=0; i<len; i++)
                    {
                        byte = (asciiToNum(data[i*2])<<4) + asciiToNum(data[i*2+1]);
                        fsm->data[fsm->data_len++] = byte;
                    }
                    fsm->state = UP_HALF_1;
                }
                else
                {
                    memset(fsm->data, 0xff, 2048);
                    ret = upWrite(fsm, BFM_CTRL_WRITE_HALF_1);
                    if (ret == 0)
                    {
                        return -1;
                    }
                    if (addr > (fsm->addr+0x800))
                    {
                        fsm->data_len = addr - fsm->addr - 0x800;
                    }
                    for (i=0; i<len; i++)
                    {
                        byte = (asciiToNum(data[i*2])<<4) + asciiToNum(data[i*2+1]);
                        fsm->data[fsm->data_len++] = byte;
                    }
                    fsm->state = UP_HALF_2;
                }
            }
            break;
        case UP_DONE:
        default:
            break;
    }

    return 0;
}

int pic32::netUpgrade(void *path, void *pwd)
{
    FILE *fp;
    char *data;
    int len, hex_len;
    int idx = 0, i;
    int ret;
    int addr, addr_1, addr_2, addr_3;
    char crc;
    UP_FSM_INFO fsm;

    pwd = NULL;
    memset(&fsm, 0, sizeof(UP_FSM_INFO));

    fp = fopen(reinterpret_cast<char *>(path), "r");
    if (NULL == fp)
    {
        LOGE("Open file(%s) failed\n", reinterpret_cast<char *>(path));
        return -1;
    }

    fseek(fp, 0, SEEK_END);
    len = ftell(fp);
    data = (char*)malloc(len+1);
    fseek(fp, 0, SEEK_SET);
    fread(data, len, 1, fp);
    data[len] = '\0';
    fclose(fp);

    decodeEcryptData(data, len, reinterpret_cast<char *>(pwd));

    netGpioPowerOff();
    netGpioPowerOn();

    ret = netInit();
    if (ret < 0)
    {
        free(data);
        netGpioPowerOff();
        return ret;
    }

    //check if slave device is ready for upgrade
    if ((ret = upRequest()) == 0)
    {
        LOGE("Slave not ready for upgrade\n");
        free(data);
        return -1;
    }

    addr_1 = -1;
    while (idx < len)
    {
        if (data[idx] != ':')
        {
            idx++;
            continue;
        }

        hex_len = (asciiToNum(data[idx+1]) << 4) + asciiToNum(data[idx+2]);
        crc = 0;
        for (i=0; i<(hex_len+4); i++)
        {
            crc += ((asciiToNum(data[idx+1+i*2]) << 4) + asciiToNum(data[idx+1+i*2+1]));
        }
        if (((0x100 - crc)&0xff) != ((asciiToNum(data[idx+1+i*2]) << 4) + asciiToNum(data[idx+1+i*2+1])))
        {
            LOGE("crc error, file was destoryed\n");
            close(spi_fd);
            free(data);
            netGpioPowerOff();
            return -1;
        }

        if (data[idx+8] == '4')
        {
            addr_1++;
            idx = idx + (2*hex_len + 11);
            continue;
        }
        else if (data[idx+8] == '0')
        {
            addr = (addr_1 << 16) + ((asciiToNum(data[idx+3])) << 12) + (asciiToNum(data[idx+4]) << 8) + (asciiToNum(data[idx+5]) << 4) + asciiToNum(data[idx+6]);
            if ((ret = upFsm(&fsm, addr, &data[idx+9], hex_len)) != 0)
            {
                LOGE("Slave upgrade failed\n");
                close(spi_fd);
                free(data);
                netGpioPowerOff();
                return ret;
            }
            if (fsm.state == UP_DONE)
            {
                LOGD("Slave upgrade success\n");
                upExec(&fsm);
                close(spi_fd);
                free(data);
                netGpioPowerOff();
                return 0;
            }
            idx = idx + (2*hex_len + 11);
            continue;
        }

        idx++;
    }

    close(spi_fd);
    free(data);
    netGpioPowerOff();
    return 0;
}

