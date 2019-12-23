#ifndef  __PIC32__H__
#define  __PIC32__H__
#include <pthread.h>
#include "base.h"
#include "common.h"

#define BFM_CTRL_REQUEST      0x01
#define BFM_CTRL_WRITE_HALF_1 0x02
#define BFM_CTRL_WRITE_HALF_2 0x03
#define BFM_CTRL_EXEC         0x04

typedef struct
{
    int state;
    int op_addr;
    int addr;
    char data[2048];
    int data_len;
}UP_FSM_INFO;

typedef enum
{
    UP_START,
    UP_HALF_1,
    UP_HALF_2,
    UP_DONE
}UP_STATE;

class pic32: public base
{
public:
    int netOpen();
    void netClose();
    int netIsOpen();
    int netCheckConnected();
    int netWrite(void *buf, int buf_len);
    int netRead(void *buf, int buf_len);
    int netReadVersion(int v[]);
    int netUpgrade(void *path, void *pwd);
    static void *spiConfigFunc(void *arg);
    static pic32* getInstance();
    pic32();
    ~pic32();

private:
    int netInit();
    void netGpioPowerOn();
    void netGpioPowerOff();    
    void netCsOn();
    void netCsOff();
    void decodeEcryptData(char *data, int len, const char *pwd);
    int upRequest();
    char asciiToNum(char a);
    int upWrite(UP_FSM_INFO *fsm, int type);
    void upExec(UP_FSM_INFO *fsm);
    int upFsm(UP_FSM_INFO *fsm, int addr, char *data, int len);
    uint8_t config_changed;
    static pthread_mutex_t mutex;
    static pic32* m_instance;
};

#endif
