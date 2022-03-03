package com.rk.module;

import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.List;

public class LineToolBoxInfo extends LitePalSupport {
    private int id;
    private String address;
    private String name;
    private int areaId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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
