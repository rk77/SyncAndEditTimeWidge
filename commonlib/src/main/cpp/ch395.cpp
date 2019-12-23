#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <sched.h>
#include <string.h>
#include <termios.h>
#include <sys/ioctl.h>
//#include <linux/spi/spidev.h>
#include "ch395.h"
#include "ch395cmd.h"

#define DEF_KEEP_LIVE_IDLE                           (15*1000)        /* 空闲时间 */
#define DEF_KEEP_LIVE_PERIOD                         (20*1000)        /* 间隔为15秒，发送一次KEEPLIVE数据包 */ */                  
#define DEF_KEEP_LIVE_CNT                            (200)            /* 重试次数  */
#define CH395_SEND_BUF_LEN                           (2048)

pthread_mutex_t ch395::mutex;
ch395* ch395::m_instance = NULL;
int ch395::net_open_flag  = 0;
//int ch395::net_run_flag   = 0;
int ch395::send_busy_flag = 0;
int ch395::net_connected_flag = 0;

ch395* ch395::getInstance()
{
    if(NULL == m_instance) {
        mutex = PTHREAD_MUTEX_INITIALIZER;
        pthread_mutex_lock(&mutex);
        m_instance = new ch395();
        pthread_mutex_unlock(&mutex);
    }
    return m_instance;
}

ch395::ch395()
{
    LOGD("Constructor CH395\n");
    ring_wr_buf = &wr_buf;
    ring_rd_buf = &rd_buf;
    ring_send_buf = new uint8_t[RING_BUF_SIZE];
    ring_recv_buf = new uint8_t[RING_BUF_SIZE];
}

ch395::~ch395()
{
    LOGD("Destructor CH395\n");
    delete []ring_send_buf;
    delete []ring_recv_buf;
}

void ch395::netGpioPowerOn()
{
    system("echo 1 > /sys/class/gpio/gpio56/value");
    system("echo 1 > /sys/class/gpio/gpio91/value");
    system("echo 1 > /sys/class/gpio/gpio62/value");
}

void ch395::netGpioPowerOff()
{
    system("echo 0 > /sys/class/gpio/gpio56/value");
    system("echo 0 > /sys/class/gpio/gpio91/value");
    system("echo 0 > /sys/class/gpio/gpio62/value");
}

void * ch395::netDaemonFunc(void *arg)
{
    NOTUSED(arg);
    int ret;
    uint8_t check;

    LOGD("Daemon thread\n");
    ch395::getInstance()->netGpioPowerOn();
    while(0x9a != ch395::getInstance()->netCheckExist(0x65))
    {
        //cnt++;
        //if(cnt > 10) {
            //LOGE("CH395 Check failed\n");
            //return -2;
        //}
        LOGI("CH395 Check again.\n");
        if(!ch395::getInstance()->net_open_flag) {
            return NULL;
        }
        sleep(1);
        //usleep(100000);
    }

    //cnt = 0;
    ret = ch395::getInstance()->netSetLocalIp(ch395::getInstance()->config.local_ip.Val);
    if(-1 == ret) {
        LOGE("Set local ip ERR\n");
        ch395::getInstance()->netGpioPowerOff();
        close(ch395::getInstance()->spi_fd);
        return NULL;//return ret;
    }
    else {
        LOGI("Set local ip OK\n");
    }
    ret = ch395::getInstance()->netSetGateway(ch395::getInstance()->config.gateway.Val);
    if(-1 == ret) {
        LOGE("Set gateway ERR\n");
        ch395::getInstance()->netGpioPowerOff();
        close(ch395::getInstance()->spi_fd);
        return NULL;//return ret;
    }
    else {
        LOGI("Set gateway OK\n");
    }
    ret = ch395::getInstance()->netSetSubMask(ch395::getInstance()->config.sub_mask.Val);
    if(-1 == ret) {
        LOGE("Set submask ERR\n");
        ch395::getInstance()->netGpioPowerOff();
        close(ch395::getInstance()->spi_fd);
        return NULL;//return ret;
    }
    else {
        LOGI("Set submask OK\n");
    }
    ret = ch395::getInstance()->netInit();
    if(0 == ret) {
        LOGI("Net init OK\n");
    }
    else {
        LOGE("Net init ERR[%x]\n", ret);
        ch395::getInstance()->netGpioPowerOff();
        close(ch395::getInstance()->spi_fd);
        return NULL;//return -1;
    }

    ret = ch395::getInstance()->netDHCPEn(0);
    if(0 == ret) {
        LOGI("DHCP close success\n");
    }
    else {
        LOGE("DHCP close error\n");
        ch395::getInstance()->netGpioPowerOff();
        close(ch395::getInstance()->spi_fd);
        return NULL;//return -1;
    }

    if(TCP == ch395::getInstance()->config.protocol_type) {
        ch395::getInstance()->netKeepliveSet();
    }

    while(PHY_DISCONN == ch395::getInstance()->netGetPHYStatus())
    {
        //cnt++;
        //if(cnt > 20) {
            //LOGE("Get PHY status failed\n");
            //return -3;
        //}
        if(!ch395::getInstance()->net_open_flag) {
            return NULL;
        }
        LOGI("Get PHY Status\n");
        usleep(200000);
    }
    LOGI("PHY connected\n");

    ret = ch395::getInstance()->netSetProtoType(0, ch395::getInstance()->config.protocol_type);    
    if(-1 == ret) {
        LOGE("Set prototype ERR\n");
        ch395::getInstance()->netGpioPowerOff();
        close(ch395::getInstance()->spi_fd);
        return NULL;//return ret;
    }
    else {
        LOGI("Set prototype OK\n");
    }

    //Client模式，设置远端ip和端口号
    if(CLIENT == ch395::getInstance()->config.mode)
    {
        ret = ch395::getInstance()->netSetRemoteIp(0, ch395::getInstance()->config.remote_ip.Val);
        if(-1 == ret) {
            LOGE("Set remote IP ERR\n");
            ch395::getInstance()->netGpioPowerOff();
            close(ch395::getInstance()->spi_fd);
            return NULL;//return ret;
        }
        else {
            LOGI("Set remote IP OK\n");
        }
        ret = ch395::getInstance()->netSetRemotePort(0, ch395::getInstance()->config.port);
        if(-1 == ret) {
            LOGE("Set remote port ERR\n");
            ch395::getInstance()->netGpioPowerOff();
            close(ch395::getInstance()->spi_fd);
            return NULL;//return ret;
        }
        else {
            LOGI("Set remote port OK\n");
        }
        ret = ch395::getInstance()->netSetSourcePort(0, ch395::getInstance()->config.port - 1);
        if(-1 == ret) {
            LOGE("Set source port ERR\n");
            ch395::getInstance()->netGpioPowerOff();
            close(ch395::getInstance()->spi_fd);
            return NULL;//return ret;
        }
        else {
            LOGI("Set source port OK\n");
        }
    }
    else //Server mode
    {
        ret = ch395::getInstance()->netSetSourcePort(0, ch395::getInstance()->config.port);
        if(-1 == ret) {
            LOGE("Set source port ERR\n");
            ch395::getInstance()->netGpioPowerOff();
            close(ch395::getInstance()->spi_fd);
            return NULL;//return ret;
        }
        else {
            LOGI("Set source port OK\n");
        }
        if(UDP == ch395::getInstance()->config.protocol_type)
        {
            ret = ch395::getInstance()->netSetRemoteIp(0, 0xffffffff);
            if(-1 == ret) {
                LOGE("Set remote IP ERR\n");
                ch395::getInstance()->netGpioPowerOff();
                close(ch395::getInstance()->spi_fd);
                return NULL;//return ret;
            }
            else {
                LOGI("Set remote IP OK\n");
            }
        }
    }

    ch395::getInstance()->netClearRecvBuf(0);
    ch395::getInstance()->initRingBuf();
    ret = ch395::getInstance()->netOpenSocket(0);
    if(0 == ret) {
        LOGI("Open socket OK\n");
    }
    else {
        LOGE("Open socket ERR[%d]\n", ret);
        ch395::getInstance()->netGpioPowerOff();
        close(ch395::getInstance()->spi_fd);
        return NULL;//return ret;
    }

    ch395::getInstance()->netGetConf();

    if(TCP == ch395::getInstance()->config.protocol_type)
    {
        if(CLIENT == ch395::getInstance()->config.mode)
        {
            ret = ch395::getInstance()->netTCPConnect(0);
            if(0 == ret) {
                LOGI("TCP connect OK\n");
            }
            else {
                LOGE("TCP connect ERR[%d]\n", ret);
                ch395::getInstance()->netGpioPowerOff();
                close(ch395::getInstance()->spi_fd);
                return NULL;//return ret;
            }
        }
        else//SERVER
        {
            ret = ch395::getInstance()->netTCPListen(0);
            if(0 == ret) {
                LOGI("Open TCP listen OK\n");
            }
            else {
                LOGE("Open TCP listen FAIL\n");
                ch395::getInstance()->netGpioPowerOff();
                close(ch395::getInstance()->spi_fd);
                return NULL;//return ret;
            }
        }
    }

    //ch395::getInstance()->net_run_flag = 1;
    while(1)
    {
        if(!ch395::getInstance()->net_open_flag) {
                break;
        }
        /*if(!ch395::getInstance()->net_run_flag) {
            LOGI("Close daemon task\n");
            break;
        }*/
        ch395::getInstance()->netSendTask();
        ch395::getInstance()->netGlobalInterrupt();
    }

    LOGD("DaemonFunc close\n");
    return NULL;//return NULL;
}

int ch395::netOpen()
{
    int ret;
    uint8_t spi_mode = SPI_MODE;
    uint8_t spi_bits = SPI_BITS;
    uint32_t spi_speed = SPI_SPEED;
    static pthread_t tid;
    static pthread_attr_t attr;

    LOGD("Open the ch395\n");
    if(net_open_flag) {
        LOGD("net has been opened\n");
        return 0;
    }
    else {
        net_open_flag = 1;
    }

    if (spi_fd > 0) {
        LOGD("Spi fd has been open\n");
        close(spi_fd);
    }

    netGpioPowerOff();
    usleep(500000);

    spi_fd = open(SPI_DEV, O_RDWR | O_NOCTTY | O_NDELAY);
    if (spi_fd < 0) {
        LOGE("open %s failed\n", SPI_DEV);
        return spi_fd;
    }
    else {
        LOGD("Spi_fd open : %d\n", spi_fd);
    }

    spi_mode = SPI_MODE_0;
    //spi_mode &= ~SPI_CS_HIGH;

    ret = ioctl(spi_fd, SPI_IOC_WR_MODE, &spi_mode);
    if (ret == -1) {
        LOGE("set spi write mode failed\n");
        close(spi_fd);
        spi_fd = -1;
        return ret;
    }
    ret = ioctl(spi_fd, SPI_IOC_RD_MODE, &spi_mode);
    if (ret == -1) {
        LOGE("set spi read mode failed\n");
        close(spi_fd);
        spi_fd = -1;
        return ret;
    }
    ret = ioctl(spi_fd, SPI_IOC_WR_BITS_PER_WORD, &spi_bits);
    if (ret == -1) {
        LOGE("set spi write bit failed\n");
        close(spi_fd);
        spi_fd = -1;
        return ret;
    }
    ret = ioctl(spi_fd, SPI_IOC_RD_BITS_PER_WORD, &spi_bits);
    if (ret == -1) {
        LOGE("set spi read bit failed\n");
        close(spi_fd);
        spi_fd = -1;
        return ret;
    }
    ret = ioctl(spi_fd, SPI_IOC_WR_MAX_SPEED_HZ, &spi_speed);
    if (ret == -1) {
        LOGE("set spi write speed failed\n");
        close(spi_fd);
        spi_fd = -1;
        return ret;
    }
    ret = ioctl(spi_fd, SPI_IOC_RD_MAX_SPEED_HZ, &spi_speed);
    if (ret == -1) {
        LOGE("set spi read speed failed\n");
        close(spi_fd);
        spi_fd = -1;
        return ret;
    }

    //spi_lock = PTHREAD_MUTEX_INITIALIZER;

    memset(&tr, 0, sizeof(tr));
    tr.tx_buf = (unsigned long long)send_buf;
    tr.rx_buf = (unsigned long long)recv_buf;
    tr.len = 0;
    tr.delay_usecs = 0;
    tr.speed_hz = spi_speed;
    tr.bits_per_word = spi_bits;


    LOGD("Create the daemon thread\n");
    pthread_attr_init(&attr);
    pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);
    ret = pthread_create(&tid, &attr, netDaemonFunc, NULL);

    LOGD("netOpen success\n");
    return spi_fd;
}

void ch395::netClose()
{
    LOGD("netClose\n");
    //net_run_flag = 0;
    if(spi_fd >= 0) {
        close(spi_fd);
        spi_fd = -1;
    }
    netGpioPowerOff();
    net_open_flag = 0;
    net_connected_flag = 0;
    send_busy_flag = 0;
}

int ch395::netIsOpen()
{
    return spi_fd;
}

int ch395::netCheckConnected()
{
    return net_connected_flag;
}

/*
int ch395::netCheckConn()
{

    uint8_t  phy_status;
    uint16_t socket_status;

    phy_status = netGetPHYStatus();
    if(1 == phy_status) {    
        net_connected_flag = 0;
        return 0;
    }
    else {
        if(TCP == config.protocol_type) {
            socket_status = netGetSocketStatus(0);
            //LOGD("netCheckConnected : %x\n", socket_status);
            if(TCP_ESTABLISHED == (socket_status>>8)) {
                net_connected_flag = 1;
                return 1;
            }
            else {
                net_connected_flag = 0;
                return 0;
            }
        }
        else {
            net_connected_flag = 1;
            return 1;
        }
    }
}
*/

int ch395::netWrite(void *buf, int buf_len)
{
    int buf_free_size;
    if(!net_connected_flag) {
        LOGE("Net not run yet!\n");
        return -1;
    }
    uint8_t *data     = (uint8_t*)buf;

    while(getRingFreeSize(ring_wr_buf) < buf_len)
    {
        sched_yield();
        usleep(100);
    }
    putDataToRingBuf(ring_wr_buf, data, buf_len);

    return buf_len;
}

int ch395::netRead(void *buf, int buf_len)
{
    if(!net_connected_flag) {
        LOGE("Net not run yet!\n");
        return -1;
    }
    uint8_t *data = (uint8_t*)buf;

    return getDataFromRingBuf(ring_rd_buf, data, buf_len);
}

void ch395::netCsOn()
{
    system("echo 0 > /sys/class/gpio/gpio18/value");
}

void ch395::netCsOff()
{
    system("echo 1 > /sys/class/gpio/gpio18/value");
}

uint8_t ch395::netCheckExist(uint8_t data)
{
    NOTUSED(data);
    uint8_t sbuf[10];
    uint8_t rbuf[10];
    struct spi_ioc_transfer trr;
    int ret;

    send_buf[0] = CMD_CHECK_EXIST;
    send_buf[1] = 0x65;
    send_buf[2] = 0;
    tr.len = 3;

    netCsOn();
    sleep(1);
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    sleep(1);

    netCsOff();

    return recv_buf[2];
}

uint8_t ch395::netGetCMDStatus()
{
    int ret;

    //pthread_mutex_lock(&spi_lock);
    send_buf[0] = CMD_GET_CMD_STATUS;
    send_buf[1] = 0;
    tr.len = 2;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    //pthread_mutex_unlock(&spi_lock);

    return recv_buf[1];
}

uint8_t ch395::netGetGlobINTStatus()
{
    int ret;

    //pthread_mutex_lock(&spi_lock);

    send_buf[0] = CMD_GET_GLOB_INT_STATUS;
    send_buf[1] = 0;
    tr.len = 2;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    //pthread_mutex_unlock(&spi_lock);

    return recv_buf[1];
}

uint16_t ch395::netGetGlobINTStatusAll()
{
    int ret;

    //pthread_mutex_lock(&spi_lock);

    send_buf[0] = CMD_GET_GLOB_INT_STATUS_ALL;
    send_buf[1] = 0;
    send_buf[2] = 0;
    tr.len = 3;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    //pthread_mutex_unlock(&spi_lock);

    return (recv_buf[2]>>8) + recv_buf[1];
}

uint8_t ch395::netGetSocketINTStatus(uint8_t socket_index)
{
    int ret;

    if(socket_index > 7)
    {
        return -1;
    }

    //pthread_mutex_lock(&spi_lock);

    send_buf[0] = CMD_GET_INT_STATUS_SN;
    send_buf[1] = socket_index;
    send_buf[2] = 0;
    tr.len = 3;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    //pthread_mutex_unlock(&spi_lock);

    return recv_buf[2];
}

int ch395::netSetLocalIp(uint32_t local_ip)
{
    int ret;

    LOGD("Set local ip : %x\n", local_ip);
    //pthread_mutex_lock(&spi_lock);

    send_buf[0] = CMD_SET_IP_ADDR;
    send_buf[1] = (local_ip>>24)&0xff;
    send_buf[2] = (local_ip>>16)&0xff;
    send_buf[3] = (local_ip>>8)&0xff;
    send_buf[4] = local_ip&0xff;
    tr.len = 5;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    //pthread_mutex_unlock(&spi_lock);

    return ret;
}

int ch395::netSetSubMask(uint32_t mask_ip)
{
    int ret;

    //pthread_mutex_lock(&spi_lock);

    send_buf[0] = CMD_SET_MASK_ADDR;
    send_buf[1] = (mask_ip>>24)&0xff;
    send_buf[2] = (mask_ip>>16)&0xff;
    send_buf[3] = (mask_ip>>8)&0xff;
    send_buf[4] = mask_ip&0xff;
    tr.len = 5;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    //pthread_mutex_unlock(&spi_lock);

    return ret;
}

int ch395::netSetGateway(uint32_t gateway)
{
    int ret;

    //pthread_mutex_lock(&spi_lock);

    send_buf[0] = CMD_SET_GWIP_ADDR;
    send_buf[1] = (gateway>>24)&0xff;
    send_buf[2] = (gateway>>16)&0xff;
    send_buf[3] = (gateway>>8)&0xff;
    send_buf[4] = gateway&0xff;
    tr.len = 5;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    //pthread_mutex_unlock(&spi_lock);

    return ret;
}

int ch395::netSetSourcePort(uint8_t socket_index, uint16_t port)
{
    int ret;

    if(socket_index > 7)
    {
        LOGE("Socket index error\n");
        return -1;
    }
    //pthread_mutex_lock(&spi_lock);
    send_buf[0] = CMD_SET_SOUR_PORT_SN;
    send_buf[1] = socket_index;
    send_buf[2] = port&0xff;
    send_buf[3] = (port>>8)&0xff;
    tr.len = 4;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    //pthread_mutex_unlock(&spi_lock);

    return ret;
}

int ch395::netSetRemoteIp(uint8_t socket_index, uint32_t remote)
{
    int ret;

    if(socket_index > 7)
    {
        LOGE("Socket index error\n");
        return -1;
    }
    send_buf[0] = CMD_SET_IP_ADDR_SN;
    send_buf[1] = socket_index;
    send_buf[2] = (remote>>24)&0xff;
    send_buf[3] = (remote>>16)&0xff;
    send_buf[4] = (remote>>8)&0xff;
    send_buf[5] = remote&0xff;
    tr.len = 6;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();
    if (ret < 0)
    {
        LOGE("Set RemoteIp failed\n");
        return -1;
    }

    return ret;
}

int ch395::netSetRemotePort(uint8_t socket_index, uint16_t port)
{
    int ret;

    if(socket_index > 7)
    {
        LOGE("Socket index error\n");
        return -1;
    }
    send_buf[0] = CMD_SET_DES_PORT_SN;
    send_buf[1] = socket_index;
    send_buf[2] = port&0xff;
    send_buf[3] = (port>>8)&0xff;
    tr.len = 4;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();
    if (ret < 0)
    {
        LOGE("Set RemotePort failed\n");
        return -1;
    }

    return ret;
}

int ch395::netSetProtoType(uint8_t socket_index, uint8_t type)
{
    int ret;

    if(socket_index > 7)
    {
        LOGE("Socket index error\n");
        return -1;
    }

    send_buf[0] = CMD_SET_PROTO_TYPE_SN;
    send_buf[1] = socket_index;
    if(TCP == type)
    {
        send_buf[2] = 0x3;
    }
    else if(UDP == type)
    {
        send_buf[2] = 0x2;
    }
    else
    {
        return -1;
    }
    tr.len = 3;
    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();
    if (ret < 0)
    {
        LOGE("Set ProtoType failed\n");
        return -1;
    }
    return ret;
}

int ch395::netGetConf(void)
{
    int ret;

    send_buf[0] = CMD_GET_IP_INF;
    tr.len = 1;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);

    memset(send_buf, 0, 20);
    tr.len = 20;
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    LOGI("IPAddr : %x.%x.%x.%x\n", recv_buf[0], recv_buf[1], recv_buf[2], recv_buf[3]);
    LOGI("Gateway: %x.%x.%x.%x\n", recv_buf[4], recv_buf[5], recv_buf[6], recv_buf[7]);
    LOGI("Mask   : %x.%x.%x.%x\n", recv_buf[8], recv_buf[9], recv_buf[10], recv_buf[11]);

    return ret;
}

int ch395::netCheckCH395Status(void)
{
    int ret;

    send_buf[0] = CMD_CHECK_EXIST;
    send_buf[1] = 0x55;
    send_buf[2] = 0;
    tr.len = 3;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    if(0xAA == recv_buf[2])
    {
        ret = 0;
    }
    LOGI("Check status ret : %d\n", ret);
    return ret;
}

int ch395::netInit()
{
    int ret;
    uint8_t status;

    send_buf[0] = CMD_INIT_CH395;
    tr.len = 1;
    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();
    if (ret < 0)
    {
        LOGE("Init ch395 failed\n");
        return -1;
    }

    usleep(350000);
    get_net_init_status:
    status = netGetCMDStatus();
    if(CH395_ERR_BUSY == status)
    {
        LOGI("Get net init busy\n");
        usleep(10);
        goto get_net_init_status;
    }
    else
    {
        return status;
    }
}

int ch395::netOpenSocket(uint8_t socket_index)
{
    int ret;
    uint8_t status;

    if(socket_index > 7)
    {
        LOGE("Socket index error\n");
        return -1;
    }

    send_buf[0] = CMD_OPEN_SOCKET_SN;
    send_buf[1] = socket_index;
    tr.len = 2;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    get_socket_open_status:
    usleep(2500);
    status = netGetCMDStatus();
    if(CH395_ERR_BUSY == status)
    {
        LOGI("Open socket busy\n");
        goto get_socket_open_status;
    }
    else
    {
        return status;
    }
}

int ch395::netCloseSocket(uint8_t socket_index)
{
    int ret;

    send_buf[0] = CMD_CLOSE_SOCKET_SN;
    send_buf[1] = socket_index;
    tr.len = 2;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    return ret;
}

int ch395::netTCPConnect(uint8_t socket_index)
{
    int ret;
    uint8_t status;

    if(socket_index > 7)
    {
        LOGE("Socket index error\n");
        return -1;
    }

    send_buf[0] = CMD_TCP_CONNECT_SN;
    send_buf[1] = socket_index;
    tr.len = 2;
    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    get_tcp_connect_status:
    usleep(2500);
    status = netGetCMDStatus();
    if(CH395_ERR_BUSY == status)
    {
        LOGI("TCP connect busy\n");
        goto get_tcp_connect_status;
    }
    else
    {
        return status;
    }
}

int ch395::netTCPDisconnect(uint8_t socket_index)
{
    int ret;
    uint8_t status;

    if(socket_index > 7)
    {
        LOGE("Socket index error\n");
        return -1;
    }

    send_buf[0] = CMD_TCP_DISNCONNECT_SN;
    send_buf[1] = socket_index;
    tr.len = 2;
    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    get_tcp_disconnect_status:
    usleep(2500);
    status = netGetCMDStatus();
    if(CH395_ERR_BUSY == status)
    {
        LOGI("TCP connect busy\n");
        goto get_tcp_disconnect_status;
    }
    else
    {
        return status;
    }
}

int ch395::netTCPListen(uint8_t socket_index)
{
    int ret;
    uint8_t status;

    send_buf[0] = CMD_TCP_LISTEN_SN;
    send_buf[1] = socket_index;
    tr.len = 2;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    get_listen_status:
    usleep(2500);
    status = netGetCMDStatus();
    if(CH395_ERR_BUSY == status)
    {
        LOGI("TCP listen busy\n");
        goto get_listen_status;
    }
    else
    {
        return status;
    }
}

int ch395::netPingEn(uint8_t en)
{
    int ret;

    send_buf[0] = CMD_PING_ENABLE;
    if(1 == en) {
        send_buf[1] = 1;
    }
    else if(0 == en) {
        send_buf[1] = 0;
    }
    else {
        return -1;
    }

    tr.len = 2;
    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    return ret;
}

int ch395::netDHCPEn(uint8_t en)
{
    int ret;
    uint8_t status;

    send_buf[0] = CMD_DHCP_ENABLE;
    if(1 == en) {
        send_buf[1] = 1;
    }
    else if(0 == en) {
        send_buf[1] = 0;
    }
    else {
        return -1;
    }

    tr.len = 2;
    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    get_dhcp_en_status:
    usleep(2500);
    status = netGetCMDStatus();
    if(CH395_ERR_BUSY == status)
    {
        LOGI("TCP connect busy\n");
        goto get_dhcp_en_status;
    }
    else
    {
        return status;
    }

}

/**************************************
*返回值:
*高8位: 0x0 -socket_closed,  0x05 - socket_open
*低8位: 0x0 -
***************************************/
uint16_t ch395::netGetSocketStatus(uint8_t socket_index)
{
    int ret;
    uint16_t status;

    if(socket_index > 7)
    {
        return -1;
    }

    //pthread_mutex_lock(&spi_lock);

    send_buf[0] = CMD_GET_SOCKET_STATUS_SN;
    send_buf[1] = socket_index;
    send_buf[2] = 0;
    send_buf[3] = 0;

    tr.len = 4;
    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    status = (recv_buf[3]<<8) + recv_buf[2];

    //pthread_mutex_unlock(&spi_lock);
    return status;
}

uint8_t ch395::netGetPHYStatus(void)
{
    int ret;
    send_buf[0] = CMD_GET_PHY_STATUS;
    send_buf[1] = 0;
    tr.len = 2;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    return recv_buf[1];
}

uint16_t ch395::netGetRecvLen(uint8_t socket_index)
{
    int ret;
    uint16_t len;

    send_buf[0] = CMD_GET_RECV_LEN_SN;
    send_buf[1] = socket_index;
    send_buf[2] = 0;
    send_buf[3] = 0;
    tr.len = 4;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    //LOGI("RECV   +++   %d   %d\n", recv_buf[2], recv_buf[3]);
    len = (uint16_t)(recv_buf[3]<<8) + recv_buf[2];
    return len;
}

uint16_t ch395::netGetRecvBuf(uint8_t socket_index, uint16_t read_len, uint8_t *buf)
{
    int ret;
    uint16_t len, recv_len;

    recv_len = netGetRecvLen(socket_index);
    len = read_len < recv_len ? read_len : recv_len;

    send_buf[0] = CMD_READ_RECV_BUF_SN;
    send_buf[1] = socket_index;
    send_buf[2] = len&0xff;
    send_buf[3] = len>>8;
    memset(&send_buf[4], 0, len);
    tr.len = 4 + len;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    memcpy(buf, &recv_buf[4], len);
    return len;
}


int ch395::netClearRecvBuf(uint8_t socket_index)
{
    int ret;

    send_buf[0] = CMD_CLEAR_RECV_BUF_SN;
    send_buf[1] = socket_index;
    tr.len = 2;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    return ret;
}

/********************************************************************************
* Function Name  : netGetUnreachIPPT
* Description    : 获取不可达信息 (IP,Port,Protocol Type)
* Input          : list 保存获取到的不可达
                        第1个字节为不可达代码，请参考 不可达代码(CH395INC.H)
                        第2个字节为IP包协议类型
                        第3-4字节为端口号
                        第4-8字节为IP地址
* Output         : None
* Return         : None
*******************************************************************************/
int ch395::netGetUnreachIPPT(uint8_t *buf)
{
    int ret, loop;

    send_buf[0] = CMD_GET_UNREACH_IPPORT;
    memset(&send_buf[1], 0, 8);
    tr.len = 9;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    memcpy(buf, &recv_buf[1], 8);
    LOGD("The unreach IPPT : ");
    for(loop = 0; loop < 8; loop++)
    {
        LOGD(" %d ", recv_buf[loop+1]);
    }
    LOGD("\n");
    return ret;
}

int ch395::netSetKeepliveOnOff(uint8_t socket_index, uint8_t enable)
{
    int ret;

    send_buf[0] = CMD_SET_KEEP_LIVE_SN;
    send_buf[1] = socket_index;
    send_buf[1] = enable;
    tr.len = 3;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    return ret;
}

int ch395::netSetKeepliveCNT(uint8_t cnt)
{
    int ret;

    send_buf[0] = CMD_SET_KEEP_LIVE_CNT;
    send_buf[1] = cnt;
    tr.len = 2;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    return ret;
}

int ch395::netSetKeepliveIDLE(uint32_t time)
{
    int ret;

    send_buf[0] = CMD_SET_KEEP_LIVE_IDLE;
    send_buf[1] = time&0xff;
    send_buf[2] = (time>>8)&0xff;
    send_buf[3] = (time>>16)&0xff;
    send_buf[4] = (time>>24)&0xff;
    tr.len = 5;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    return ret;
}

int ch395::netSetKeepliveINTVL(uint32_t time)
{
    int ret;

    send_buf[0] = CMD_SET_KEEP_LIVE_INTVL;
    send_buf[1] = time&0xff;
    send_buf[2] = (time>>8)&0xff;
    send_buf[3] = (time>>16)&0xff;
    send_buf[4] = (time>>24)&0xff;
    tr.len = 5;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    return ret;
}

void ch395::netKeepliveSet(void)
{
    netSetKeepliveCNT(DEF_KEEP_LIVE_CNT);
    //netSetKeepliveIDLE(DEF_KEEP_LIVE_IDLE);
    //netSetKeepliveINTVL(DEF_KEEP_LIVE_PERIOD);
}

int ch395::netSetTTL(uint8_t socket_index, uint8_t TTL)
{
    int ret;

    if(TTL > 128)
    {
        LOGI("TTL value too big\n");
        return -1;
    }

    send_buf[0] = CMD_SET_TTL;
    send_buf[1] = socket_index;
    send_buf[2] = TTL;
    tr.len = 3;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    return ret;
}

int ch395::netReadVersion(int v [])
{
    LOGD("The CH395 not have the Soft version\n");

    v[0] = 0xFF;
    v[1] = 0xFF;

    return 0;
}

int ch395::netUpgrade(void *path, void *pwd)
{
    NOTUSED(path);
    NOTUSED(pwd);
    LOGD("The CH395 firmware not support upgrade\n");
    return -1;
}

int ch395::netGetRemoteIPP(uint8_t socket_index, uint32_t *ip, uint16_t *port)
{
    int ret;

    send_buf[0] = CMD_GET_REMOT_IPP_SN;
    send_buf[1] = socket_index;
    memset(&send_buf[2], 0, 6);
    tr.len = 8;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    *ip = (uint32_t)recv_buf[2] + (uint32_t)(recv_buf[3]<<8) + 
          (uint32_t)(recv_buf[4]<<16) + (uint32_t)(recv_buf[5]<<24);

    *port = (uint16_t)recv_buf[6] + (uint16_t)(recv_buf[7]<<8);

    return ret;
}

int ch395::netSendBuf(uint8_t socket_index, uint8_t *buf, uint16_t length)
{
    int ret;
    int i;

    send_buf[0] = CMD_WRITE_SEND_BUF_SN;
    send_buf[1] = socket_index;
    send_buf[2] = length&0xff;
    send_buf[3] = (length>>8)&0xff;
    memcpy(&send_buf[4], buf, length);
    tr.len = 4 + length;

    netCsOn();
    ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    netCsOff();

    return ret;
}

void ch395::initRingBuf(void)
{
    ring_wr_buf->buf       = ring_send_buf;
    ring_wr_buf->get_cur   = &ring_send_buf[0];
    ring_wr_buf->put_cur   = &ring_send_buf[0];
    ring_wr_buf->buf_size  = SPI_RING_BUF_SIZE;
    ring_wr_buf->full_flag = 0;
    ring_wr_buf->mutex     = PTHREAD_MUTEX_INITIALIZER;

    ring_rd_buf->buf       = ring_recv_buf;
    ring_rd_buf->get_cur   = &ring_recv_buf[0];
    ring_rd_buf->put_cur   = &ring_recv_buf[0];
    ring_rd_buf->buf_size  = SPI_RING_BUF_SIZE;
    ring_rd_buf->full_flag = 0;
    ring_rd_buf->mutex     = PTHREAD_MUTEX_INITIALIZER;
}

/*获取环形buffer中数据个数*/
int ch395::getRingDataSize(RING_BUF *ring)
{
    //Don't lock
    int size;
    if(NULL == ring) {
        LOGE("Null pointer\n");
        return 0;
    }

    if(ring->full_flag) {
        return ring->buf_size;
    }

    if(ring->put_cur >= ring->get_cur) {
        size = ring->put_cur - ring->get_cur;
    }
    else {
        size = ring->buf_size + (ring->put_cur - ring->get_cur);
    }

    return size;
}


int ch395::getRingFreeSize(RING_BUF *ring)
{
    //Don't lock
    int size;

    if(NULL == ring) {
        LOGE("Ring is NULL\n");
        return 0;
    }
    size = ring->buf_size - getRingDataSize(ring);

    return size;
}

int ch395::putDataToRingBuf(RING_BUF *ring, uint8_t *data, int data_size)
{
    int tmp;

    if(NULL == ring || NULL == data) {
        return -1;
    }

    pthread_mutex_lock(&ring_wr_buf->mutex);
    if(getRingFreeSize(ring) < data_size) {
        LOGD("Ring buf is full\n");
        return -1;
    }
    if(data_size > ring->buf_size || 0 == data_size) {
        LOGE("Bad data size %d\n", data_size);
        return -1;
    }

    if(ring->put_cur >= ring->get_cur)
    {
        if(ring->buf + ring->buf_size - ring->put_cur >= data_size) {
            memcpy(ring->put_cur, data, data_size);
            ring->put_cur += data_size;
        }
        else {//后半截空闲buffer不够用，需要用前半截
            tmp = ring->buf + ring->buf_size - ring->put_cur;//后半截空闲buffer长度
            memcpy(ring->put_cur, data, tmp);
            memcpy(ring->buf, data + tmp, data_size - tmp);
            ring->put_cur = ring->buf + (data_size - tmp);
        }
    }
    else//put_cur < get_cur
    {
        memcpy(ring->put_cur, data, data_size);
        ring->put_cur += data_size;
    }

    if(ring->put_cur == ring->get_cur) {
        LOGD("Ring is full\n");
        ring->full_flag = 1;
    }
    pthread_mutex_unlock(&ring_wr_buf->mutex);

    return 0;
}

int ch395::getDataFromRingBuf(RING_BUF *ring, uint8_t *buf, int buf_len)
{
    int get_size;
    int tmp;

    if(NULL == ring || NULL == buf) {
        return -1;
    }

    pthread_mutex_lock(&ring_wr_buf->mutex);
    get_size = getRingDataSize(ring) < buf_len? getRingDataSize(ring) : buf_len;

    if(ring->put_cur > ring->get_cur)//Put指针在Get之后
    {
        memcpy(buf, ring->get_cur, get_size);
        ring->get_cur += get_size;
    }
    else//Put指针在Get指针之前或者两者相同
    {
        tmp = ring->buf + ring->buf_size - ring->get_cur;//get指针后半截长度
        if(tmp >= get_size) {//仅获取get指针的后半截
            memcpy(buf, ring->get_cur, get_size);
            ring->get_cur += get_size;
        }
        else {//获取get指针的后半截加前半段中的一截
            memcpy(buf, ring->get_cur, tmp);
            memcpy(buf + tmp, ring->buf, get_size - tmp);
            ring->get_cur = ring->buf + get_size - tmp;
        }
        if(ring->full_flag) {
            ring->full_flag = 0;
        }
    }
    pthread_mutex_unlock(&ring_wr_buf->mutex);

    return get_size;
}

/*ch395 socket中断函数*/
void ch395::netSocketInterrupt(uint8_t socket_index)
{
    uint32_t remoteIP;
    uint16_t remotePort;
    uint8_t  socket_int;
    uint16_t len;
    uint8_t buf[512];
    int loop;

    socket_int = netGetSocketINTStatus(socket_index);
    LOGI("Socket INT  :  %d\n", socket_int);

    if(socket_int & SINT_STAT_SENBUF_FREE)                       /* 发送缓冲区空闲，可以继续写入要发送的数据 */
    {
        LOGI("Unset send_busy_flag\n");
        send_busy_flag = 0;
    }
    if(socket_int & SINT_STAT_SEND_OK)                           /* 发送完成中断*/
    {
        //LOGD("Send a packet OK INT\n");
    }
    if(socket_int & SINT_STAT_RECV)                              /* 接收中断 */
    {
        LOGD("Socket recv INT\n");
        len = netGetRecvLen(socket_index);                          /* 获取当前缓冲区内数据长度 */
        LOGD("Recv len : %d\n", len);
        if(len > 512) {
            len = 512;                                            /* MyBuffer缓冲区长度为512 */
        }
        if(len > getRingFreeSize(ring_rd_buf)) {
            len = getRingFreeSize(ring_rd_buf);
        }
        if(len) {
            netGetRecvBuf(socket_index, len, buf);                        /* 读取数据 */
            putDataToRingBuf(ring_rd_buf, buf, len);
        }
        LOGD("Recv data : ");
        for(loop = 0; loop < len; loop++)
        {
            LOGD(" %x ,", buf[loop]);
        }
        LOGD("\n");
    }
    if(socket_int & SINT_STAT_CONNECT)                          /* 连接中断，仅在TCP模式下有效*/
    {
        LOGI("TCP connected\n");
        netSetKeepliveOnOff(socket_index, 1);                                   /*打开KEEPALIVE保活定器*/
        netSetTTL(socket_index, 128);                          /*设置TTL*/
        if(SERVER == config.mode)
        {
            netGetRemoteIPP(socket_index, &remoteIP, &remotePort);
            LOGI("Have remote ip: %x, remote port: %d\n", remoteIP, remotePort);
        }
        initRingBuf();
        send_busy_flag = 0;
        net_connected_flag = 1;
    }
    /*
    **产生断开连接中断和超时中断时，CH395默认配置是内部主动关闭，用户不需要自己关闭该Socket，如果想配置成不主动关闭Socket需要配置
    **SOCK_CTRL_FLAG_SOCKET_CLOSE标志位（默认�?），如果该标志为1，CH395内部不对Socket进行关闭处理，用户在连接中断和超时中断时调用
    **CH395CloseSocket函数对Socket进行关闭，如果不关闭则该Socket一直为连接的状态（事实上已经断开），就不能再去连接了。
    */
    if(socket_int & SINT_STAT_DISCONNECT)                        /* 断开中断，仅在TCP模式下有效*/
    {
        net_connected_flag = 0;
        LOGI("TCP disconnected\n");
        netOpenSocket(socket_index);
        if(TCP == config.protocol_type) {
            if(SERVER == config.mode) {
                netTCPListen(socket_index);
            }
            else {
                netTCPConnect(socket_index);
            }
        }
    }
    if(socket_int & SINT_STAT_TIM_OUT)                           /* 超时中断 */
    {
        LOGI("Time out \n");
        netOpenSocket(socket_index);
        if(TCP == config.protocol_type) {
            if(CLIENT == config.mode) {
                netTCPConnect(socket_index);
            }
        }
        initRingBuf();
        send_busy_flag = 0;
    }
}

/*ch395全局中断函数*/
void ch395::netGlobalInterrupt(void)
{
    uint8_t int_status;
    uint8_t phy_status;
    uint8_t  unreachIPPT[8];

    int_status = netGetGlobINTStatus();

    if(int_status & GINT_STAT_UNREACH) /* 不可达中断，读取不可达信息*/
    {
        LOGD("Unreach INT\n");
        netGetUnreachIPPT(unreachIPPT);
    }
    if(int_status & GINT_STAT_IP_CONFLI)                            /* 产生IP冲突中断，建议重新修改CH395 IP，并初始化CH395*/
    {
        LOGD("IP CONFLI INT\n");
    }
    if(int_status & GINT_STAT_PHY_CHANGE)                           /* 产生PHY改变中断*/
    {
        LOGD("PHY change INT\n");
        phy_status = netGetPHYStatus();
        if(1 == phy_status)
        {
            LOGD("PHY disconnected\n");
            net_connected_flag = 0;
        }
        else
        {
            LOGD("PHY connected, status : %d\n", phy_status);
        }
    }
    if(int_status & GINT_STAT_SOCK0)
    {
        LOGD("Socket0 INT\n");
        netSocketInterrupt(0);                                     /* 处理socket 0中断*/
    }
    if(int_status & GINT_STAT_SOCK1)                                
    {
        LOGD("Socket1 INT\n");
        netSocketInterrupt(1);                                     /* 处理socket 1中断*/
    }
    if(int_status & GINT_STAT_SOCK2)                                
    {
        LOGD("Socket2 INT\n");
        netSocketInterrupt(2);                                     /* 处理socket 2中断*/
    }
    if(int_status & GINT_STAT_SOCK3)                                
    {
        LOGD("Socket3 INT\n");
        netSocketInterrupt(3);                                     /* 处理socket 3中断*/
    }
    if(int_status & GINT_STAT_SOCK4)
    {
        LOGD("Socket4 INT\n");
        netSocketInterrupt(4);                                     /* 处理socket 4中断*/
    }
    if(int_status & GINT_STAT_SOCK5)                                
    {
        LOGD("Socket5 INT\n");
        netSocketInterrupt(5);                                     /* 处理socket 5中断*/
    }
    if(int_status & GINT_STAT_SOCK6)                                
    {
        LOGD("Socket6 INT\n");
        netSocketInterrupt(6);                                     /* 处理socket 6中断*/
    }
    if(int_status & GINT_STAT_SOCK7)                                
    {
        LOGD("Socket7 INT\n");
        netSocketInterrupt(7);                                     /* 处理socket 7中断*/
    }
}

void ch395::netSendTask()
{
    int wr_size;
    uint8_t buf[CH395_SEND_BUF_LEN];

    if(send_busy_flag) {
        LOGD("Send busy\n");
        return;
    }

    if(!netCheckConnected()) {
        LOGD("Not CCCONNECTED\n");
        return;
    }

    wr_size = getRingDataSize(ring_wr_buf);

    if(wr_size > CH395_SEND_BUF_LEN) {
        wr_size = CH395_SEND_BUF_LEN;
    }

    wr_size = getDataFromRingBuf(ring_wr_buf, buf, wr_size);
    if(wr_size > 0)
    {
        LOGD("WR size : %d\n", wr_size);
        LOGD("Send:  ");
        for(int i = 0; i < wr_size; i++) {
            LOGD(" %x ", buf[i]);
        }
    }
    if(0 == wr_size) {
        return;
    }

    netSendBuf(0, buf, wr_size);
    send_busy_flag = 1;
}

