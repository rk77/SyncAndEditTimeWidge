#ifndef  __COMMON__H__
#define  __COMMON__H__
#include <stdint.h>
#include<android/log.h>

//#define ADB_DEBUG
#ifndef ADB_DEBUG
#define LOG_TAG "ETH"
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


#define SPI_BUF_SIZE 4096
#define RING_BUF_SIZE 4096

#define SPI_DEV "/dev/spidev5.0"
#define SPI_MODE (1)
#define SPI_BITS (8)
#define SPI_SPEED (3000000)


typedef enum
{
    TCP,
    UDP
}PROTOTYPE;

typedef enum
{
    SERVER,
    CLIENT
}CSMODE;

typedef union
{
    uint32_t Val;
    uint16_t w[2];
    uint8_t  v[4];
}IPV4_ADDR;

typedef struct
{
    IPV4_ADDR local_ip;
    IPV4_ADDR sub_mask;
    IPV4_ADDR gateway;
    uint8_t  protocol_type; //0: TCP, 1: UDP
    uint8_t  mode;          //0: server mode, 1: client mode
    uint16_t port;          //if server, the port is local port; if client, the port is remote port
    IPV4_ADDR remote_ip;     //client mode only
}NET_CONFIG;

#endif
