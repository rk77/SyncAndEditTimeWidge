#include <jni.h>
#include <sys/timerfd.h>
#include<android/log.h>
#include "newbusprotocol.cpp"
//#include "spi_to_net.cpp"
#include "lora.cpp"
#include "eth.h"
#include "eth.cpp"

#include "se.h"
#include "se.cpp"

#include "base.cpp"
#include "ch395.cpp"
#include "pic32.cpp"

#include "ttyusb.cpp"

#include <android/log.h>
#define   LOG_TAG    "LOG_TEST"
#define   LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define   LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define   LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_readTest(JNIEnv *env, jclass type,jbyteArray array,jint len) {
    unsigned char* buffer = (unsigned char *) new char[len];
    int length = read485(buffer);
    if(length > 0){
        (*env).SetByteArrayRegion(array, 0, length, (jbyte*)buffer);
    }
    return length;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_writeTest(JNIEnv *env, jclass type,jbyteArray array,jint len) {
    unsigned char* buffer = (unsigned char *) new char[len];
    int length = write485(buffer,len);
    if(length > 0){
        (*env).SetByteArrayRegion(array, 0, length, (jbyte*)buffer);
    }
    return length;
}

//打开串口
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_open(JNIEnv *env, jclass type,jint baudrate) {
    int i = tryOpenTty();
    LOGE("%d", i);
    return i;
}

//尝试打开串口
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_tryOpenTty(JNIEnv *env, jclass type) {
    int i = tryOpenTty();
    LOGE("%d", i);
    return i;
}

//打开串口
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_ttyClose(JNIEnv *env, jclass type) {
    ttyClose();
    return 1;
}



//读取485
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_read485(JNIEnv *env, jclass type,jbyteArray array,jint len) {
    unsigned char* buffer = (unsigned char *) new char[len];
    int length = read485(buffer);
    LOGE("485读取长度  %d", length);
    if(length > 0){
        (*env).SetByteArrayRegion(array, 0, length, (jbyte*)buffer);
    }
    return length;
}

//写入485
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_write485(JNIEnv *env, jclass type,jbyteArray array,jint len) {
    unsigned char* bBuffer=(unsigned char*)(env)->GetByteArrayElements(array, 0);
    return write485(bBuffer,len);
}



//写入管理通道
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_writeMGR(JNIEnv *env, jclass type,jbyteArray array,jint len) {
    unsigned char* bBuffer=(unsigned char*)(env)->GetByteArrayElements(array, 0);
    return writeMGR(bBuffer,len);
}

//读取管理通道
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_readMGR(JNIEnv *env, jclass type,jbyteArray array,jint len) {
    unsigned char* buffer = (unsigned char *) new char[len];
    int length = readMGR(buffer);
    LOGE("length=%d" , length);
    if(length > 0){
        (*env).SetByteArrayRegion(array, 0, length, (jbyte*)buffer);
    }
    return length;
}


//写入红外
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_writeIR(JNIEnv *env, jclass type,jbyteArray array,jint len) {
    unsigned char* bBuffer=(unsigned char*)(env)->GetByteArrayElements(array, 0);
    return writeIR(bBuffer,len);
}

//读取红外
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_readIR(JNIEnv *env, jclass type,jbyteArray array,jint len) {
    unsigned char* buffer = (unsigned char *) new char[len];
    int length = readIR(buffer);
    LOGE("length=%d" , length);
    if(length > 0){
        (*env).SetByteArrayRegion(array, 0, length, (jbyte*)buffer);
    }
    return length;
}


//写入NFC
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_writeNFC(JNIEnv *env, jclass type,jbyteArray array,jint len) {
    unsigned char* bBuffer=(unsigned char*)(env)->GetByteArrayElements(array, 0);
    return writeNFC(bBuffer,len);
}

//读取NFC
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_readNFC(JNIEnv *env, jclass type,jbyteArray array,jint len) {
    unsigned char* buffer = (unsigned char *) new char[len];
    int length = readNFC(buffer);
    LOGE("length=%d" , length);
    if(length > 0){
        (*env).SetByteArrayRegion(array, 0, length, (jbyte*)buffer);
    }
    return length;
}


//网口相关net_gpio_power_on
//打开网口
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_openEth(JNIEnv *env, jclass type) {
    //int i = netOpen();
    int i = eth::getInstance()->netOpen();
    LOGE("打开网口 %d", i);
    return i;
}

/*
* @author gaofengx
* create on 2019/11/11 11:11
* description: 检查网口是否已经打开
*/

extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_netIsOpen(JNIEnv *env, jclass type) {
    //int i = netIsOpen();
    int i = eth::getInstance()->netIsOpen();
    LOGE("TST, 检查网口是否已经打开 %d", i);
    return i;
}
/*
* @author gaofengx
* create on 2019/11/11 11:11
* description: 读取网口连接状态
 * 1 连接成功
 * 0 未连接
 * -1 错误
*/
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_netCheckConnected(JNIEnv *env, jclass type) {
    //int i = netCheckConnected();
    int i = eth::getInstance()->netCheckConnected();
    LOGE("TST 网络连接状态 %d", i);
    return i;
}


//关闭网口
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_closeEth(JNIEnv *env, jclass type) {
    //netClose();
    eth::getInstance()->netClose();
    LOGE("TST 关闭网口 ");
    return 0;
}

//设置本机工作模式
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_setNetMode(JNIEnv *env, jclass type,jint mode) {
    //setNetMode(mode);
    eth::getInstance()->setNetMode(mode);
    LOGE("设置本机工作模式 mode = %d",mode);
    return 0;
}

//设置协议类型
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_setProtocolType(JNIEnv *env, jclass type,jint protocol_type) {
    //setProtocolType(protocol_type);
    eth::getInstance()->setProtocolType(protocol_type);
    LOGE("设置协议类型 protocol_type = %d",protocol_type);
    return 0;
}

//设置IP地址
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_setLocalIp(JNIEnv *env, jclass type,jint ip) {
    //setLocalIp(ip);
    eth::getInstance()->setLocalIp(ip);
    LOGE("设置IP地址 ip = %d",ip);
    return 0;
}

//设置子网掩码
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_setSubMask(JNIEnv *env, jclass type,jint mask) {
    //setSubMask(mask);
    eth::getInstance()->setSubMask(mask);
    LOGE("设置子网掩码 mask = %d",mask);
    return 0;
}

//设置默认网关
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_setGateway(JNIEnv *env, jclass type,jint gateway) {
    //setGateway(gateway);
    eth::getInstance()->setGateway(gateway);
    LOGE("设置默认网关 gateway = %d",gateway);
    return 0;
}

//设置端口
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_setNetPort(JNIEnv *env, jclass type,jint port) {
    //setNetPort(port);
    eth::getInstance()->setNetPort(port);
    LOGE("设置端口 port = %d",port);
    return 0;
}

//设置远程IP地址
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_setRemoteIp(JNIEnv *env, jclass type,jint remote_ip) {
    //setRemoteIp(remote_ip);
    eth::getInstance()->setRemoteIp(remote_ip);
    LOGE("设置远程IP地址 remote_ip = %d",remote_ip);
    return 0;
}

//读取本地IP地址
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_getLocalIp(JNIEnv *env, jclass type) {
    //return getLocalIp();
    return eth::getInstance()->getLocalIp();
}

//读取子网掩码
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_getSubMask(JNIEnv *env, jclass type) {
    //return getSubMask();
    return eth::getInstance()->getSubMask();
}

//读取网关地址
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_getGateway(JNIEnv *env, jclass type) {
    //return getGateway();
    return eth::getInstance()->getGateway();
}

//获取协议类型
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_getProtocolType(JNIEnv *env, jclass type) {
    //return getProtocolType();
    return eth::getInstance()->getProtocolType();
}

//获取本机工作模式
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_getNetMode(JNIEnv *env, jclass type) {
    //return getNetMode();
    return eth::getInstance()->getNetMode();
}

//获取端口
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_getNetPort(JNIEnv *env, jclass type) {
    //return getNetPort();
    return eth::getInstance()->getNetPort();
}

//获取远端IP地址
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_getRemoteIp(JNIEnv *env, jclass type) {
    //return getRemoteIp();
    return eth::getInstance()->getRemoteIp();
}

//读取网口数据
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_readEth(JNIEnv *env, jclass type,jbyteArray array,jint len) {
    unsigned char* buffer = (unsigned char *) new char[len];
    //int length = netRead(buffer,1024);
    int length = eth::getInstance()->netRead(buffer, 1024);
    if(length > 0){
        (*env).SetByteArrayRegion(array, 0, length, (jbyte*)buffer);
    }
    return length;
}

//写入网口数据
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_writeEth(JNIEnv *env, jclass type,jbyteArray array,jint len) {
    unsigned char* bBuffer=(unsigned char*)(env)->GetByteArrayElements(array, 0);
    //return netWrite(bBuffer,len);
    return eth::getInstance()->netWrite(bBuffer, len);
}




//LoRa相关
//打开LoRa
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_loraOpen(JNIEnv *env, jclass type) {
    int i = loraOpen();
    LOGE("打开LoRa %d", i);
    return i;
}


//关闭LoRa
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_loraClose(JNIEnv *env, jclass type) {
    loraClose();
    LOGE("关闭LoRa ");
    return 0;
}


//读取LoRa数据
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_loraRead(JNIEnv *env, jclass type,jbyteArray array,jint len) {
    char* buffer = (char *) new char[len];
    int length = LoraRead(buffer,len);
    if(length > 0){
        (*env).SetByteArrayRegion(array, 0, length, (jbyte*)buffer);
    }
    return length;
}

//写入LoRa数据
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_loraWrite(JNIEnv *env, jclass type,jbyteArray array,jint len) {
     char* bBuffer=( char*)(env)->GetByteArrayElements(array, 0);
    return LoraWrite(bBuffer,len);
}

//配置LoRa通信参数
// LoRaSetFrequency
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_loraSetFrequency(JNIEnv *env, jclass type,jint freq) {
    LoRaSetFrequency(freq);
    LOGE(" freq = %d",freq);
    return 0;
}


//LoRaSetPower
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_loraSetPower(JNIEnv *env, jclass type,jint power) {
    LoRaSetPower(power);
    LOGE(" power = %d",power);
    return 0;
}


//LoRaSetBandWidth
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_loraSetBandWidth(JNIEnv *env, jclass type,jint bw) {
    LoRaSetBandWidth(bw);
    LOGE(" bw = %d",bw);
    return 0;
}

//LoRaSetSpreadingFactor
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_loraSetSpreadingFactor(JNIEnv *env, jclass type,jint factor) {
    LoRaSetSpreadingFactor(factor);
    LOGE(" factor = %d",factor);
    return 0;
}


//LoRaSetErrorCoding
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_loraSetErrorCoding(JNIEnv *env, jclass type,jint value) {
    LoRaSetErrorCoding(value);
    LOGE(" value = %d",value);
    return 0;
}

//LoRaSetPacketCrcOn
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_loraSetPacketCrcOn(JNIEnv *env, jclass type,jint value) {
    LoRaSetPacketCrcOn(value);
    LOGE(" value = %d",value);
    return 0;
}


//LoRaSetPreambleLength
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_loraSetPreambleLength(JNIEnv *env, jclass type,jint value) {
    LoRaSetPreambleLength(value);
    LOGE(" value = %d",value);
    return 0;
}


//LoRaSetImplicitHeaderOn
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_loraSetImplicitHeaderOn(JNIEnv *env, jclass type,jint value) {
    LoRaSetImplicitHeaderOn(value);
    LOGE(" value = %d",value);
    return 0;
}


//LoRaSetPayloadLength
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_loraSetPayloadLength(JNIEnv *env, jclass type,jint value) {
    LoRaSetPayloadLength(value);
    LOGE(" value = %d",value);
    return 0;
}


//LoRaSetPaRamp
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_loraSetPaRamp(JNIEnv *env, jclass type,jint value) {
    LoRaSetPaRamp(value);
    LOGE(" value = %d",value);
    return 0;
}


//LoRaSetLowDatarateOptimize
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_loraSetLowDatarateOptimize(JNIEnv *env, jclass type,jint value) {
    LoRaSetLowDatarateOptimize(value);
    LOGE(" value = %d",value);
    return 0;
}

//LoRaSetSymbTimeOut
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_loraSetSymbTimeOut(JNIEnv *env, jclass type,jint value) {
    LoRaSetSymbTimeOut(value);
    LOGE(" value = %d",value);
    return 0;
}

//读取带宽
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_loraGetBandWidth(JNIEnv *env, jclass type) {
    return LoRaGetBandWidth();
//    return 0;
}

//读取扩频因子
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_loraGetSpreadingFactor(JNIEnv *env, jclass type) {
    return LoRaGetSpreadingFactor();
//    return 0;
}

//读取中心频率
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_loraGetFrequency(JNIEnv *env, jclass type) {
    return LoRaGetFrequency();
//    return 0;
}



//网口升级相关
//读取网口芯片版本号 netReadVersion

extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_netReadVersion(JNIEnv *env, jclass type, jintArray bytesArr_) {
    jint* buffer = new jint[2];
    //jint result = netReadVersion(buffer);
    jint result = eth::getInstance()->netReadVersion(buffer);
    LOGE("读取网口芯片程序结果  result=%d" , result);
    if(result > -1){
        (*env).SetIntArrayRegion(bytesArr_, 0, 2, (jint *)buffer);
    }
    return result;
}

//升级网口芯片程序 netUpgrade

extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_netUpgrade(JNIEnv *env, jclass type, jstring path,jstring pwd) {
    void *strContent = (void *) env->GetStringUTFChars(path, JNI_FALSE);
    void *strPwd = (void *) env->GetStringUTFChars(path, JNI_FALSE);
    //jint result = netUpgrade(strContent);
    jint result = eth::getInstance()->netUpgrade(strContent, strPwd);
    LOGE("升级网口芯片程序结果  result=%d" , result);

    return result;
}



//升级F+芯片程序 netUpgrade
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_upgradeFpuls(JNIEnv *env, jclass type,jstring path_) {
    char *strContent = (char *) env->GetStringUTFChars(path_, JNI_FALSE);
    jint result = upgrade_fplus(strContent);
    LOGE("升级F+芯片程序   result=%d" , result);
    return result;
}


// Security Unit
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_setDefaultPara(JNIEnv *env, jclass type) {
    jint result = SecurityUnit::getInstance()->SpiConfig(0, 1000000, 0);
    LOGE("JniMethods, setDefaultPara, result=%d" , result);
    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_initSecurityUnit(JNIEnv *env, jclass type) {
    jint result = SecurityUnit::getInstance()->getInstance()->Init();
    LOGE("JniMethods, initSecurityUnit, result=%d" , result);
    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_deinitSecurityUnit(JNIEnv *env, jclass type) {
    jint result = SecurityUnit::getInstance()->getInstance()->DeInit();
    LOGE("JniMethods, deinitSecurityUnit, result=%d" , result);
    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_writeSecurityUnit(JNIEnv *env, jclass type,jbyteArray array,jint len) {
    //unsigned char* buffer = (unsigned char *) new char[len];
    char* bBuffer=(char*)(env)->GetByteArrayElements(array, 0);
    return SecurityUnit::getInstance()->SendData(bBuffer, 0, len);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_readSecurityUnit(JNIEnv *env, jclass type,jbyteArray array,jint len) {
    char* buffer = (char *) new char[len];
    int length = SecurityUnit::getInstance()->RecvData(buffer, 0, 1024);
    if(length > 0){
        (*env).SetByteArrayRegion(array, 0, length, (jbyte*)buffer);
    }
    return length;
}

//载波口 打开
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_ttyUSBOpen(JNIEnv *env, jclass type,jint baud) {
    int i = ttyUSBOpen(baud);
    LOGE("载波口 %d", i);
    return i;
}

//载波口 关闭
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_ttyUSBClose(JNIEnv *env, jclass type) {
    int i = ttyUSBClose();
    LOGE("载波口 %d", i);
    return i;
}


//载波口  发送数据
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_ttyUSBWrite(JNIEnv *env, jclass type,jbyteArray array,jint len) {
    unsigned char* bBuffer=(unsigned char*)(env)->GetByteArrayElements(array, 0);
    return ttyUSBWrite(bBuffer,len);
}


//载波口 读取数据
extern "C"
JNIEXPORT jint JNICALL
Java_com_rk_commonmodule_jni_JniMethods_ttyUSBRead(JNIEnv *env, jclass type,jbyteArray array,jint len) {
    unsigned char* buffer = (unsigned char *) new char[len];
    int length = ttyUSBRead(buffer,1024);
    if(length > 0){
        (*env).SetByteArrayRegion(array, 0, length, (jbyte*)buffer);
    }
    return length;
}
