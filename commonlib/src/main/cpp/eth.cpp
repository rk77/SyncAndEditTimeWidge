#include <stdio.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <linux/spi/spidev.h>
#include "eth.h"
#include "ch395.h"
#include "pic32.h"

eth* eth::m_instance = NULL;
pthread_mutex_t eth::mutex;

int eth::ch395_or_pic32()
{
    int ver[20] = {0};

    if(!pic32::getInstance()->netReadVersion(ver))
    {
        LOGD("This chip is pic32\n");
        return ETH_PIC32;//PIC32
    }
    else
    {
        LOGD("This chip is ch395\n");
        delete pic32::getInstance();
        return ETH_CH395;//CH395
    }
}

eth::eth()
{
    eth_type = (ETH_TYPE)ch395_or_pic32();
    if(ETH_CH395 == eth_type)
    {
        eth_instance.ch395_instance = ch395::getInstance();
    }
    else
    {
        eth_instance.pic32_instance = pic32::getInstance();
    }
}

eth::~eth()
{
    if(ETH_CH395 == eth_type)
    {
        delete eth_instance.ch395_instance;
    }
    else
    {
        delete eth_instance.pic32_instance;
    }
}

void eth::setLocalIp(uint32_t local_ip)
{
    if(ETH_CH395 == eth_type)
    {
        return eth_instance.ch395_instance->setLocalIp(local_ip);
    }
    else
    {
        return eth_instance.pic32_instance->setLocalIp(local_ip);
    }
}

void eth::setSubMask(uint32_t mask)
{
    if(ETH_CH395 == eth_type)
    {
        return eth_instance.ch395_instance->setSubMask(mask);
    }
    else
    {
        return eth_instance.pic32_instance->setSubMask(mask);
    }
}

void eth::setGateway(uint32_t gateway)
{
    if(ETH_CH395 == eth_type)
    {
        return eth_instance.ch395_instance->setGateway(gateway);
    }
    else
    {
        return eth_instance.pic32_instance->setGateway(gateway);
    }
}

void eth::setProtocolType(uint8_t protocol_type)
{
    if(ETH_CH395 == eth_type)
    {
        return eth_instance.ch395_instance->setProtocolType(protocol_type);
    }
    else
    {
        return eth_instance.pic32_instance->setProtocolType(protocol_type);
    }
}

void eth::setNetMode(uint8_t mode)
{
    if(ETH_CH395 == eth_type)
    {
        return eth_instance.ch395_instance->setNetMode(mode);
    }
    else
    {
        return eth_instance.pic32_instance->setNetMode(mode);
    }
}

void eth::setNetPort(uint16_t port)
{
    if(ETH_CH395 == eth_type)
    {
        return eth_instance.ch395_instance->setNetPort(port);
    }
    else
    {
        return eth_instance.pic32_instance->setNetPort(port);
    }
}

void eth::setRemoteIp(uint32_t remote_ip)
{
    if(ETH_CH395 == eth_type)
    {
        return eth_instance.ch395_instance->setRemoteIp(remote_ip);
    }
    else
    {
        return eth_instance.pic32_instance->setRemoteIp(remote_ip);
    }
}

uint32_t eth::getLocalIp()
{
    if(ETH_CH395 == eth_type)
    {
        return eth_instance.ch395_instance->getLocalIp();
    }
    else
    {
        return eth_instance.pic32_instance->getLocalIp();
    }
}

uint32_t eth::getSubMask()
{
    if(ETH_CH395 == eth_type)
    {
        return eth_instance.ch395_instance->getSubMask();
    }
    else
    {
        return eth_instance.pic32_instance->getSubMask();
    }
}

uint32_t eth::getGateway()
{
    if(ETH_CH395 == eth_type)
    {
        return eth_instance.ch395_instance->getGateway();
    }
    else
    {
        return eth_instance.pic32_instance->getGateway();
    }
}

uint8_t eth::getProtocolType()
{
    if(ETH_CH395 == eth_type)
    {
        return eth_instance.ch395_instance->getProtocolType();
    }
    else
    {
        return eth_instance.pic32_instance->getProtocolType();
    }
}

uint8_t eth::getNetMode()
{
    if(ETH_CH395 == eth_type)
    {
        return eth_instance.ch395_instance->getNetMode();
    }
    else
    {
        return eth_instance.pic32_instance->getNetMode();
    }
}

uint16_t eth::getNetPort()
{
    if(ETH_CH395 == eth_type)
    {
        return eth_instance.ch395_instance->getNetPort();
    }
    else
    {
        return eth_instance.pic32_instance->getNetPort();
    }
}

uint32_t eth::getRemoteIp()
{
    if(ETH_CH395 == eth_type)
    {
        return eth_instance.ch395_instance->getRemoteIp();
    }
    else
    {
        return eth_instance.pic32_instance->getRemoteIp();
    }
}
int eth::netOpen()
{
    if(ETH_CH395 == eth_type)
    {
        return eth_instance.ch395_instance->netOpen();
    }
    else
    {
        return eth_instance.pic32_instance->netOpen();
    }
}

void eth::netClose()
{
    if(ETH_CH395 == eth_type)
    {
        return eth_instance.ch395_instance->netClose();
    }
    else
    {
        return eth_instance.pic32_instance->netClose();
    }
}

int eth::netIsOpen()
{
    if(ETH_CH395 == eth_type)
    {
        return eth_instance.ch395_instance->netIsOpen();
    }
    else
    {
        return eth_instance.pic32_instance->netIsOpen();
    }
}

int eth::netCheckConnected()
{
    if(ETH_CH395 == eth_type)
    {
        return eth_instance.ch395_instance->netCheckConnected();
    }
    else
    {
        return eth_instance.pic32_instance->netCheckConnected();
    }
}

int eth::netWrite(void *buf, int buf_len)
{
    if(ETH_CH395 == eth_type)
    {
        return eth_instance.ch395_instance->netWrite(buf, buf_len);
    }
    else
    {
        return eth_instance.pic32_instance->netWrite(buf, buf_len);
    }
}

int eth::netRead(void *buf, int buf_len)
{
    if(ETH_CH395 == eth_type)
    {
        return eth_instance.ch395_instance->netRead(buf, buf_len);
    }
    else
    {
        return eth_instance.pic32_instance->netRead(buf, buf_len);
    }
}

int eth::netReadVersion(int v[])
{
    if(ETH_CH395 == eth_type)
    {
        return eth_instance.ch395_instance->netReadVersion(v);
    }
    else
    {
        return eth_instance.pic32_instance->netReadVersion(v);
    }
}

int eth::netUpgrade(void *path, void *pwd)
{
    if(ETH_CH395 == eth_type)
    {
        return eth_instance.ch395_instance->netUpgrade(path, pwd);
    }
    else
    {
        return eth_instance.pic32_instance->netUpgrade(path, pwd);
    }
}
