#ifndef  __NET_H__
#define  __NET_H__

#include <pthread.h>
#include "common.h"
#include "ch395.h"
#include "pic32.h"

typedef enum
{
    ETH_CH395,
    ETH_PIC32
}ETH_TYPE;

typedef union
{
    ch395 *ch395_instance;
    pic32 *pic32_instance;
}ETH_INSTANCE;

class eth
{
public:
    void setLocalIp(uint32_t local_ip);
    void setSubMask(uint32_t mask);
    void setGateway(uint32_t gateway);
    void setProtocolType(uint8_t protocol_type);
    void setNetMode(uint8_t mode);
    void setNetPort(uint16_t port);
    void setRemoteIp(uint32_t remote_ip);

    uint32_t getLocalIp();
    uint32_t getSubMask();
    uint32_t getGateway();
    uint8_t getProtocolType();
    uint8_t getNetMode();
    uint16_t getNetPort();
    uint32_t getRemoteIp();

    int netOpen();
    void netClose();
    int netIsOpen();
    int netCheckConnected();
    int netWrite(void *buf, int buf_len);
    int netRead(void *buf, int buf_len);

    int netReadVersion(int v[]);
    int netUpgrade(void *path, void *pwd);

    static eth* getInstance();


private:
    eth();
    ~eth();
    static pthread_mutex_t mutex;
    static eth* m_instance;
    static int ch395_or_pic32();
    ETH_TYPE eth_type;
    ETH_INSTANCE eth_instance;
};

eth* eth::getInstance()
{
    if(NULL == m_instance) {
        mutex = PTHREAD_MUTEX_INITIALIZER;
        pthread_mutex_lock(&mutex);
        m_instance = new eth();
        pthread_mutex_unlock(&mutex);
    }
    return m_instance;
}

#endif
