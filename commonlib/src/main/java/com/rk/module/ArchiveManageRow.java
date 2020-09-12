package com.rk.module;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

public class ArchiveManageRow extends LitePalSupport {
    @Column(unique = true, defaultValue = "unknown")
    private int configSerialNum;

    private String commAddress;
    private int baudRateIndex;
    private int protocolTypeIndex;
    private int portIndex;
    private String commPassword;
    private int rateCnt;
    private int userType;
    private int lineConnectTypeIndex;
    private int rateVoltage;
    private int rateCurrent;

    private String collectorAddress;
    private String assetNum;
    private int voltageTransRatio;
    private int currentTransRatio;

    public int getConfigSerialNum() {
        return configSerialNum;
    }

    public void setConfigSerialNum(int configSerialNum) {
        this.configSerialNum = configSerialNum;
    }

    public String getCommAddress() {
        return commAddress;
    }

    public void setCommAddress(String commAddress) {
        this.commAddress = commAddress;
    }

    public int getBaudRateIndex() {
        return baudRateIndex;
    }

    public void setBaudRateIndex(int baudRateIndex) {
        this.baudRateIndex = baudRateIndex;
    }

    public int getProtocolTypeIndex() {
        return protocolTypeIndex;
    }

    public void setProtocolTypeIndex(int protocolTypeIndex) {
        this.protocolTypeIndex = protocolTypeIndex;
    }

    public int getPortIndex() {
        return portIndex;
    }

    public void setPortIndex(int portIdx) {
        this.portIndex = portIdx;
    }

    public String getCommPassword() {
        return commPassword;
    }

    public void setCommPassword(String commPassword) {
        this.commPassword = commPassword;
    }

    public int getRateCnt() {
        return rateCnt;
    }

    public void setRateCnt(int rateCnt) {
        this.rateCnt = rateCnt;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public int getLineConnectTypeIndex() {
        return lineConnectTypeIndex;
    }

    public void setLineConnectTypeIndex(int lineConnectTypeIndex) {
        this.lineConnectTypeIndex = lineConnectTypeIndex;
    }

    public int getRateVoltage() {
        return rateVoltage;
    }

    public void setRateVoltage(int rateVoltage) {
        this.rateVoltage = rateVoltage;
    }

    public int getRateCurrent() {
        return rateCurrent;
    }

    public void setRateCurrent(int rateCurrent) {
        this.rateCurrent = rateCurrent;
    }

    public String getCollectorAddress() {
        return collectorAddress;
    }

    public void setCollectorAddress(String collectorAddress) {
        this.collectorAddress = collectorAddress;
    }

    public String getAssetNum() {
        return assetNum;
    }

    public void setAssetNum(String assetNum) {
        this.assetNum = assetNum;
    }

    public int getVoltageTransRatio() {
        return voltageTransRatio;
    }

    public void setVoltageTransRatio(int voltageTransRatio) {
        this.voltageTransRatio = voltageTransRatio;
    }

    public int getCurrentTransRatio() {
        return currentTransRatio;
    }

    public void setCurrentTransRatio(int currentTransRatio) {
        this.currentTransRatio = currentTransRatio;
    }
}
