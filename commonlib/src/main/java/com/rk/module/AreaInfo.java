package com.rk.module;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

public class AreaInfo extends LitePalSupport {
    private int id;
    private String terminalAddr;
    private String mountPosition;

    public String getTerminalAddr() {
        return terminalAddr;
    }

    public void setTerminalAddr(String terminalAddr) {
        this.terminalAddr = terminalAddr;
    }

    public String getMountPosition() {
        return mountPosition;
    }

    public void setMountPosition(String mountPosition) {
        this.mountPosition = mountPosition;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
