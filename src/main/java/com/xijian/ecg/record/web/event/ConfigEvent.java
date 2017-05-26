package com.xijian.ecg.record.web.event;

/**
 * Created by 邵伟 on 2017/5/22 0022.
 */
public class ConfigEvent {

    private String name;

    private Object orgValue;

    private Object newValue;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getOrgValue() {
        return orgValue;
    }

    public void setOrgValue(Object orgValue) {
        this.orgValue = orgValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }
}
