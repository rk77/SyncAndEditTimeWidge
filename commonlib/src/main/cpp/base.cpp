#include <stdio.h>
#include "common.h"
#include "base.h"

base::base()
{
    LOGD("Constructor base\n");
    spi_fd = -1;
    send_buf = new uint8_t[SPI_BUF_SIZE];
    recv_buf = new uint8_t[SPI_BUF_SIZE];
    config   ={
        0xc0a86464,     // local ip: 192.168.100.100
        0xffffff00,     // mask: 255.255.255.0
        0xc0a86401,     // gateway: 192.168.100.1
        TCP,            // protocol: TCP
        SERVER,         // mode: server
        9999,           // port: 9999
        0};
}

base::~base()
{
    LOGD("Destructor base\n");
    delete []send_buf;
    delete []recv_buf;
}

void base::setLocalIp(uint32_t local_ip)
{
    if (config.local_ip.Val != local_ip)
    {
        config.local_ip.Val = local_ip;
    }
}

void base::setSubMask(uint32_t mask)
{
    if (config.sub_mask.Val != mask)
    {
        config.sub_mask.Val = mask;
    }
}

void base::setGateway(uint32_t gateway)
{
    if (config.gateway.Val != gateway)
    {
        config.gateway.Val = gateway;
    }
}

void base::setProtocolType(uint8_t protocol_type)
{
    if (config.protocol_type != protocol_type)
    {
        config.protocol_type = protocol_type;
    }
}

void base::setNetMode(uint8_t mode)
{
    if (config.mode != mode)
    {
        config.mode = mode;
    }
}
void base::setNetPort(uint16_t port)
{
    if (config.port != port)
    {
        config.port = port;
    }
}

void base::setRemoteIp(uint32_t remote_ip)
{
    if (config.remote_ip.Val != remote_ip)
    {
        config.remote_ip.Val = remote_ip;
    }
}

uint32_t base::getLocalIp()
{
    return config.local_ip.Val;
}

uint32_t base::getSubMask()
{
    return config.sub_mask.Val;
}

uint32_t base::getGateway()
{
    return config.gateway.Val;
}

uint8_t base::getProtocolType()
{
    return config.protocol_type;
}

uint8_t base::getNetMode()
{
    return config.mode;
}

uint16_t base::getNetPort()
{
    return config.port;
}

uint32_t base::getRemoteIp()
{
    return config.remote_ip.Val;
}


