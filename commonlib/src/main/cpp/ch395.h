#ifndef  __CH395__
#define  __CH395__
#include <pthread.h>
#include "common.h"
#include "base.h"

#define SPI_RING_BUF_SIZE 4096

typedef struct
{
    uint8_t *buf;
    uint8_t *put_cur;
    uint8_t *get_cur;
    uint8_t  full_flag;
    int      buf_size;
    pthread_mutex_t mutex;
}RING_BUF;


class ch395: public base
{
public:
    int netOpen();
    void netClose();
    int netIsOpen();
    int netCheckConnected();
    int netWrite(void *buf, int buf_len);
    int netRead(void *buf, int buf_len);
    int netReadVersion(int v []);
    int netUpgrade(void *path, void *pwd);
    static ch395* getInstance();
    ch395();
    ~ch395();

private:
    static int net_open_flag;
    //static int net_run_flag;
    static int net_connected_flag;
    static int send_busy_flag;
    uint8_t *ring_send_buf;
    uint8_t *ring_recv_buf;
    static pthread_mutex_t mutex;
    static ch395* m_instance;

    //uint8_t ring_send_buf[SPI_RING_BUF_SIZE];
    //uint8_t ring_recv_buf[SPI_RING_BUF_SIZE];
    RING_BUF wr_buf, *ring_wr_buf;
    RING_BUF rd_buf, *ring_rd_buf;

    static void *netDaemonFunc(void *arg);
    void netGpioPowerOn();
    void netGpioPowerOff();
    void netCsOn();
    void netCsOff();
    //int netCheckConn();
    uint8_t netCheckExist(uint8_t data);
    uint8_t netGetCMDStatus();
    uint8_t netGetGlobINTStatus();
    uint16_t netGetGlobINTStatusAll();
    uint8_t netGetSocketINTStatus(uint8_t socket_index);
    int netSetLocalIp(uint32_t local_ip);
    int netSetSubMask(uint32_t mask_ip);
    int netSetGateway(uint32_t gateway);
    int netSetSourcePort(uint8_t socket_index, uint16_t port);
    int netSetRemoteIp(uint8_t socket_index, uint32_t remote);
    int netSetRemotePort(uint8_t socket_index, uint16_t port);
    int netSetProtoType(uint8_t socket_index, uint8_t type);
    int netGetConf(void);
    int netCheckCH395Status(void);
    int netInit();
    int netOpenSocket(uint8_t socket_index);
    int netCloseSocket(uint8_t socket_index);
    int netTCPConnect(uint8_t socket_index);
    int netTCPDisconnect(uint8_t socket_index);
    int netTCPListen(uint8_t socket_index);
    int netPingEn(uint8_t en);    
    int netDHCPEn(uint8_t en);
    uint16_t netGetSocketStatus(uint8_t socket_index);
    uint8_t netGetPHYStatus(void);
    uint16_t netGetRecvLen(uint8_t socket_index);
    uint16_t netGetRecvBuf(uint8_t socket_index, uint16_t read_len, uint8_t *buf);
    int netClearRecvBuf(uint8_t socket_index);
    int netGetUnreachIPPT(uint8_t *buf);
    int netSetKeepliveOnOff(uint8_t socket_index, uint8_t enable);
    int netSetKeepliveCNT(uint8_t cnt);
    int netSetKeepliveIDLE(uint32_t time);
    int netSetKeepliveINTVL(uint32_t time);
    void netKeepliveSet(void);
    int netSetTTL(uint8_t socket_index, uint8_t TTL);
    int netGetRemoteIPP(uint8_t socket_index, uint32_t *ip, uint16_t *port);
    int netSendBuf(uint8_t socket_index, uint8_t *buf, uint16_t length);

    void initRingBuf(void);
    int getRingDataSize(RING_BUF *ring);
    int getRingFreeSize(RING_BUF *ring);
    int putDataToRingBuf(RING_BUF *ring, uint8_t *data, int data_size);
    int getDataFromRingBuf(RING_BUF *ring, uint8_t *buf, int buf_len);

    void netSocketInterrupt(uint8_t socket_index);
    void netGlobalInterrupt(void);
    void netSendTask();
};

#endif
