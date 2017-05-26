package com.xijian.ecg.record.web.domain;

import java.io.Serializable;

/**
 * Created by 邵伟 on 2017/5/21 0021.
 */
public class AlermItem implements Serializable{

    private final static long serialVersionUID = 1L;

    private String name;

    private Double maxValue;

    private Double currentValue;

    private boolean isNull = true;

    private boolean isUse;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public Double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Double currentValue) {
        this.currentValue = currentValue;
    }

    public boolean getIsNull() {
        return isNull;
    }

    public void setIsNull(boolean aNull) {
        isNull = aNull;
    }

    public boolean getIsUse() {
        return isUse;
    }

    public void setIsUse(boolean use) {
        isUse = use;
    }
}
