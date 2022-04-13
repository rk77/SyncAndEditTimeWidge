package com.rk.commonlib.widge;

public class TestModel extends TreeViewNode {

    public String name;
    public int maginLeft;
    public boolean isSelected;
    public String id;
    public TestModel parent;
    public String code;
    public String type;

    @Override
    public String toString() {
        return "name='" + name + '\'';
    }
}
