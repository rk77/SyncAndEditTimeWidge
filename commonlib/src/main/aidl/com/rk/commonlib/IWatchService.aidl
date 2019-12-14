// IWatchService.aidl
package com.rk.commonlib;

// Declare any non-default types here with import statements

interface IWatchService {
    int getPid();
    void setBinder(IBinder client);
}
