#ifndef  __BASE__H__
#define  __BASE__H__

#include <linux/spi/spidev.h>
#include "common.h"

class base
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


protected:
    int spi_fd;
    struct spi_ioc_transfer tr;
    NET_CONFIG config;
    uint8_t *send_buf;
    uint8_t *recv_buf;

    base();
    ~base();
};

#endif
