package com.rk.module;

import org.litepal.crud.LitePalSupport;

public class LineToolBoxInfo extends LitePalSupport {
    private String address;
    private String name;
    private int areaId;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }
}
