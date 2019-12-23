/********************************** (C) COPYRIGHT *********************************
* File Name       : se.cpp
* Author          : Wang Lei
* Version         : V1.1
* Date            : 2019/8/30
* Description     :
* 
**********************************************************************************/

#include     <stdio.h>
#include     <stdlib.h>
#include     <unistd.h>
#include     <sys/types.h>
#include     <sys/stat.h>
#include     <fcntl.h>
#include     <errno.h>
#include     <pthread.h>
#include     <string.h>
#include     <stdint.h>
#include     <sched.h>
#include     <time.h>
#include     <sys/ioctl.h>
#include     <android/log.h>
#include     "se.h"

//#define ADB_DEBUG
#ifndef ADB_DEBUG
#define LOG_TAG "SE"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG ,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG ,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,LOG_TAG ,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG ,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,LOG_TAG,__VA_ARGS__) // 定义LOGF类型
#else
#define LOGD(...) printf(__VA_ARGS__)
#define LOGI(...) printf(__VA_ARGS__)
#define LOGW(...) printf(__VA_ARGS__)
#define LOGE(...) printf(__VA_ARGS__)
#define LOGF(...) printf(__VA_ARGS__)
#endif

#define NOTUSED(V)         ((void) V)

/*时间宏，单位us*/
//#define INSTRUCT_QUERY_TIME   1000000
#define INSTRUCT_QUERY_PERIOD 20

static const char *spi_dev =  "/dev/spidev6.0";

SecurityUnit* SecurityUnit::m_instance = NULL;
pthread_mutex_t SecurityUnit::mutex;

void * SecurityUnit::read_daemon_func(void* arg)
{
    int  loop, ret;
    char LRC2;
    struct timespec tm1, tm2;
    int delay_nanosecond;
    NOTUSED(arg);

    while(1)
    {
        if(!getInstance()->spi.init_flag)
        {
            getInstance()->spi.read_daemon_exit_flag = 1;
            LOGD("Exit daemon\n");
            return NULL;
        }
        if(-1 == getInstance()->spi.spi_fd) {
            LOGD("Start daemon!\n");
            usleep(10000);
            continue;
        }

        if(getInstance()->spi.start_read_flag)
        {
            LOGD("Start read!!!\n");
            pthread_mutex_lock(&getInstance()->mutex);
            memset(getInstance()->spi.buffer.data, 0, BUF_SIZE);
            getInstance()->chip_select(1);
            clock_gettime(CLOCK_MONOTONIC, &tm1);
            do{
                clock_gettime(CLOCK_MONOTONIC, &tm2);
                delay_nanosecond = (tm2.tv_sec - tm1.tv_sec)*1000000000 + tm2.tv_nsec - tm1.tv_nsec;
                if(delay_nanosecond > (1000000 * getInstance()->spi.recv_time_out)) {
                    LOGE("Read time out\n");
                    getInstance()->chip_select(0);
                    getInstance()->spi.start_read_flag = 0;
                    pthread_mutex_unlock(&getInstance()->mutex);
                    break;
                }
                getInstance()->spi.send_buf[0] = 0;
                getInstance()->spi.tr.len = 1;
                ret = ioctl(getInstance()->spi.spi_fd, SPI_IOC_MESSAGE(1), &getInstance()->spi.tr);
                if(-1 == ret)
                {
                    LOGE("Read fd:%d,  ret:%d, errno:%d\n", getInstance()->spi.spi_fd, ret, errno);
                    return NULL;
                }
                usleep(INSTRUCT_QUERY_PERIOD);
                if(getInstance()->spi.recv_buf[0]) {
                    //LOGD("RECV[0] : %x\n", getInstance()->spi.recv_buf[0]);
                }
            } while(0x55 != getInstance()->spi.recv_buf[0]);

            if(!getInstance()->spi.start_read_flag) {
                continue;
            }

            memset(getInstance()->spi.send_buf, 0, 4);
            getInstance()->spi.tr.len = 4;
            ioctl(getInstance()->spi.spi_fd, SPI_IOC_MESSAGE(1), &getInstance()->spi.tr);
            memcpy(getInstance()->spi.buffer.data, getInstance()->spi.recv_buf, 4);

            if(0x90 == getInstance()->spi.recv_buf[0] && 0x00 == getInstance()->spi.recv_buf[1])
            {
                getInstance()->spi.tr.len = (uint32_t)(getInstance()->spi.recv_buf[2] << 8) + getInstance()->spi.recv_buf[3] + 1;//不要漏掉LRC2
                ret = ioctl(getInstance()->spi.spi_fd, SPI_IOC_MESSAGE(1), &getInstance()->spi.tr);
                LOGD("Recv length: %d\n", ret);
                for(loop = 0; loop < ret; loop++) {
                    LOGD(" 0x%x ", getInstance()->spi.recv_buf[loop]);
                }
                LOGD("\n");

                memcpy(&getInstance()->spi.buffer.data[4], getInstance()->spi.recv_buf, getInstance()->spi.tr.len);
                LRC2 = getInstance()->spi.buffer.data[0];
                for(loop = 1; loop < ret+3; loop++) {
                    LRC2 ^= getInstance()->spi.buffer.data[loop];
                }

                LRC2 = ~LRC2;
                LOGD("LRC2 : 0x%x\n", LRC2);
                if(LRC2 == getInstance()->spi.buffer.data[loop]) {
                    getInstance()->spi.buffer.length = 4 + getInstance()->spi.tr.len;
                }
            }
            getInstance()->chip_select(0);
            getInstance()->spi.start_read_flag = 0;
            pthread_mutex_unlock(&getInstance()->mutex);
        }
        else
        {
            usleep(300);
        }
    }

    LOGD("Daemon end\n");
    return NULL;
}

int SecurityUnit::Init()
{
    int ret;
    uint8_t bits;

    LOGD("Init\n");
    mutex = PTHREAD_MUTEX_INITIALIZER;
    chip_power_on();
    spi.init_flag = 1;
    spi.read_daemon_exit_flag = 0;
    if(0 == spi.spi_halfword) {
        bits = 8;
    }
    else if(1 == spi.spi_halfword) {
        bits = 16;
    }

    if (spi.spi_fd > 0) {
        close(spi.spi_fd);
    }

    spi.spi_fd = open(spi_dev, O_RDWR | O_NOCTTY | O_NDELAY);
    if (spi.spi_fd < 0) {
        LOGE("open %s failed\n", spi_dev);
        return spi.spi_fd;
    }
    LOGD("SPI FD : %d\n", spi.spi_fd);

    ret = ioctl(spi.spi_fd, SPI_IOC_WR_MODE, &spi.spi_mode);
    if (ret == -1) {
        LOGE("set spi write mode failed\n");
        close(spi.spi_fd);
        return ret;
    }
    ret = ioctl(spi.spi_fd, SPI_IOC_RD_MODE, &spi.spi_mode);
    if (ret == -1) {
        LOGE("set spi read mode failed\n");
        close(spi.spi_fd);
        return ret;
    }
    ret = ioctl(spi.spi_fd, SPI_IOC_WR_BITS_PER_WORD, &bits);
    if (ret == -1) {
        LOGE("set spi write bit failed\n");
        close(spi.spi_fd);
        return ret;
    }
    ret = ioctl(spi.spi_fd, SPI_IOC_RD_BITS_PER_WORD, &bits);
    if (ret == -1) {
        LOGE("set spi read bit failed\n");
        close(spi.spi_fd);
        return ret;
    }
    ret = ioctl(spi.spi_fd, SPI_IOC_WR_MAX_SPEED_HZ, &spi.spi_speed);
    if (ret == -1) {
        LOGE("set spi write speed failed\n");
        close(spi.spi_fd);
        return ret;
    }
    ret = ioctl(spi.spi_fd, SPI_IOC_RD_MAX_SPEED_HZ, &spi.spi_speed);
    if (ret == -1) {
        LOGE("set spi read speed failed\n");
        close(spi.spi_fd);
        return ret;
    }

    spi.tr.tx_buf = (unsigned long)spi.send_buf;//(unsigned long)send_buf;
    spi.tr.rx_buf = (unsigned long)spi.recv_buf;//(unsigned long)recv_buf;
    spi.tr.len = 0;
    spi.tr.delay_usecs = 0;
    spi.tr.speed_hz = spi.spi_speed;
    spi.tr.bits_per_word = bits;
    pthread_attr_init(&attr);
    pthread_create(&tid, &attr, read_daemon_func, (void*)this);
    sleep(1);

    return 0;
}

int SecurityUnit::DeInit()
{
    LOGD("SPI deinit\n");
    chip_power_off();
    spi.init_flag = 0;
    while(!spi.read_daemon_exit_flag)
    {
        usleep(20);
    }
    close(spi.spi_fd);
    LOGD("SPI deinit ok\n");
    return 0;
}

void SecurityUnit::chip_select(int value)
{
    if(value) {
        system("echo 0 > /sys/class/gpio/gpio22/value");
        usleep(20);
    }
    else {
        usleep(5);
        system("echo 1 > /sys/class/gpio/gpio22/value");
    }
}

void SecurityUnit::chip_power_on()
{
    chip_select(0);
    system("echo 1 > /sys/class/gpio/gpio89/value");
    system("echo 1 > /sys/class/gpio/gpio56/value");
}

void SecurityUnit::chip_power_off()
{
    system("echo 0 > /sys/class/gpio/gpio89/value");
    system("echo 0 > /sys/class/gpio/gpio56/value");
}

int SecurityUnit::ClearSendCache()
{
    return 0;
}
int SecurityUnit::ClearRecvCache()
{
    int lock_ret;

    lock_ret = pthread_mutex_trylock(&mutex);
    if(0 == lock_ret)
    {
        spi.buffer.length    = 0;
        pthread_mutex_unlock(&mutex);
        return 0;
    }
    return -1;
}

int SecurityUnit::Config(int baudrate, int databits, int parity, int stopbits, int blockmode)
{
    NOTUSED(baudrate);
    NOTUSED(databits);
    NOTUSED(parity);
    NOTUSED(stopbits);
    NOTUSED(blockmode);
    exit(1);
    return 0;
}

int SecurityUnit::SendData(char *buf, int offset, int count)
{
    int  loop;
    int  round = 0;
    int  send_count;
    int  lock_ret;
    const int delay_period = 20000;
    char LRC1;

    if(-1 == spi.spi_fd) {
        return 1;
    }

    LOGD("SendData:\n");
    for(loop = 0; loop < count; loop++) {
        LOGD("0x%x ", *(buf+offset+loop));
    }
    LOGD("\n");
    while(1)
    {
        lock_ret = pthread_mutex_trylock(&mutex);
        if(0 == lock_ret)
        {
            LOGD("SendData Lock ok\n");
            spi.send_buf[0] = 0x55;
            memcpy(&spi.send_buf[1], buf+offset, count);
            LRC1 = buf[offset];
            for(loop = 1; loop < count; loop++) {
                LRC1 ^= buf[offset + loop];
            }
            LRC1 = ~LRC1;
            spi.send_buf[count+1] = LRC1;
            spi.tr.len = count + 2;

            chip_select(1);
            send_count = ioctl(spi.spi_fd, SPI_IOC_MESSAGE(1), &spi.tr);
            LOGD("SEND Lock ok , spi:%d  send count %d, errno[%d]\n", spi.spi_fd, send_count, errno);
            if(2+count != send_count) {
                chip_select(0);
                pthread_mutex_unlock(&mutex);
                return 2;//发送异常
            }
            chip_select(0);
            usleep(10);
            spi.start_read_flag = 1;
            pthread_mutex_unlock(&mutex);
            LOGD("SendData Unlock\n");
                return 0;
            }
            else
        {
            LOGD("Lock failed [round:%d, delay_period:%d, timeout:%d]\n", round, delay_period, spi.send_time_out);
            round++;
            usleep(delay_period);
            if(delay_period * round > 1000 * spi.send_time_out) {
                return 2;
            }
        }
    }
}

int SecurityUnit::RecvData(char *buf, int offset, int count)
{
    int loop;
    int round = 0;
    int length;
    int lock_ret;
    const int delay_period = 20000;


    LOGD("Recv data\n");
    while(1)
    {
        if(!spi.buffer.length)
        {
            usleep(delay_period);
            round++;
            if(delay_period * round > 1000 * spi.recv_time_out) {
                return 0;
            }
        }
        else
        {
            lock_ret = pthread_mutex_trylock(&mutex);
            if(0 == lock_ret)
            {
                length = spi.buffer.length > count? count : spi.buffer.length;
                memcpy(buf + offset, spi.buffer.data, length);
                memmove(spi.buffer.data, spi.buffer.data + length, spi.buffer.length - length);
                spi.buffer.length -= length;
                pthread_mutex_unlock(&mutex);
                return length;
            }
            else
            {
                usleep(delay_period);
                round++;
                if(delay_period * round > 1000 * spi.recv_time_out) {
                    return 0;
                }
            }
        }
    };
}

/*********************************************************
*direction : 0 - 发送， 1 - 接收   
*timeout   ：超时时间，单位ms
**********************************************************/
int SecurityUnit::SetTimeOut(int direction, int timeout)
{
    if(0 == direction) {
        spi.send_time_out = timeout;
    }
    else if(1 == direction) {
        spi.recv_time_out = timeout;
    }
    else {
        return -1;
    }

    return 0;
}

int SecurityUnit::GetVersion()
{
    return 0;
}

/******************************************************
**mode     : SPI模式，取值0,1,2,3
**speed    ：SPI时钟，取值0-18M
**halfword ：传输模式，取值0,1           0-8位，1-16位
*******************************************************/
int SecurityUnit::SpiConfig(int mode, int speed, int halfword)
{
    spi.spi_mode = mode;
    spi.spi_speed = speed;
    spi.spi_halfword = halfword;
    return 0;
}

//int main(int argc, char *argv[])
//{
//    int ret;
//    //char data[] = {0x00, 0xA2, 0x01, 0x02, 0x00, 0x01, 0x08};
//    //char data[] = {0x00, 0xA2, 0x01, 0x01, 0x00, 0x01, 0x04};
//    char data[] = {0x00, 0xa0, 0x01, 0x00, 0x00, 0x00};
//    char recv[500] = {0};
//    int len;
//
//    NOTUSED(argc);
//    NOTUSED(argv);
//
//    //ret = SecurityUnit::getInstance()->SpiConfig(0, 1000000, 0);
//    ret = SecurityUnit::getInstance()->Init();
//
//    while(1)
//    {
//        SecurityUnit::getInstance()->SendData(data, 0, sizeof(data));
//        LOGD("Send OK\n");
//        len = SecurityUnit::getInstance()->RecvData(recv, 0, sizeof(recv));
//        if(len > 0)
//        {
//            LOGD("Read LL len : %d\n", len);
//            for(int i=0; i<len; i++) {
//                LOGD(" %x ", recv[i]);
//            }
//            LOGD("\n");
//        }
//        else
//        {
//            LOGD("No data received\n");
//        }
//        sleep(10);
//    }
//
//    return ret;
//}

