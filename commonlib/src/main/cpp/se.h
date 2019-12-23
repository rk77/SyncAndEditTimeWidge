#ifndef  __SE_H__
#define  __SE_H__
#include <stdint.h>
#include <pthread.h>
//#include <semaphore.h>
#include <linux/spi/spidev.h>

#define BUF_SIZE 1024

typedef struct
{
    uint8_t *data;
    int length;//有效数据长度
}READ_BUFFER;//SPI读缓冲buffer

typedef struct
{
    int spi_fd;
    uint8_t spi_mode, spi_halfword;//Spi模式/字模式
    uint32_t spi_speed;//Spi速度
    struct spi_ioc_transfer tr;//SPI设备操作结构体
    uint8_t init_flag;
    uint8_t read_daemon_exit_flag;

    uint8_t *send_buf;//spi发送buf
    uint8_t *recv_buf;//spi接收buf
    READ_BUFFER buffer;//spi读缓存buffer

    int start_read_flag;//发起数据交互后，开始接收返回数据，置此标记
    int send_time_out;//发送接口超时时间，单位ms
    int recv_time_out;//接收接口超时时间，单位ms
}SPI_PARAM;

class SecurityUnit {
public:
    int Init();
    int DeInit();
    int ClearSendCache();
    int ClearRecvCache();
    int SpiConfig(int mode, int speed, int halfword);
    int Config(int baudrate, int databits, int parity, int stopbits, int blockmode);
    int SendData(char *buf, int offset, int count);
    int RecvData(char *buf, int offset, int count);
    int SetTimeOut(int direction, int timeout);
    int GetVersion();
    static void * read_daemon_func(void* arg);
    static SecurityUnit* getInstance();

private:
    pthread_t tid;
    pthread_attr_t attr;

    static pthread_mutex_t mutex;
    static SecurityUnit* m_instance;

    SPI_PARAM spi;

    void chip_select(int value);
    void chip_power_on();
    void chip_power_off();

    SecurityUnit();
    ~SecurityUnit();
};

SecurityUnit* SecurityUnit::getInstance()
{
    if(NULL == m_instance) {
        mutex = PTHREAD_MUTEX_INITIALIZER;
        pthread_mutex_lock(&mutex);
        m_instance = new SecurityUnit();
        pthread_mutex_unlock(&mutex);
    }

    return m_instance;
}

SecurityUnit::SecurityUnit()
{
    memset(&spi, 0, sizeof(spi));
    spi.spi_fd   = -1;
    spi.spi_mode = 3;
    spi.spi_speed  = 1000000;
    spi.spi_halfword   = 0;
    spi.send_buf = (uint8_t*)malloc(BUF_SIZE);
    spi.recv_buf = (uint8_t*)malloc(BUF_SIZE);
    spi.buffer.data   = (uint8_t*)malloc(BUF_SIZE);
	spi.buffer.length = 0;
    spi.send_time_out = 1000;//单位ms
    spi.recv_time_out = 1000;//单位ms
    spi.start_read_flag = 0;
    spi.init_flag = 1;
    spi.read_daemon_exit_flag = 0;
}

SecurityUnit::~SecurityUnit()
{
    free(spi.send_buf);
    free(spi.recv_buf);
    free(spi.buffer.data);
    close(spi.spi_fd);
}

#endif
