package com.xijian.ecg.record.web.domain;

import java.io.Serializable;

/**
 * Created by 邵伟 on 2017/5/19 0019.
 */
public class KeySpaceDomain implements Serializable{

    private final static long serialVersionUID = 1L;

    private Integer keys;

    private Integer expires;

    private Long avgTtl;

    @Override
    public String toString() {
        return "KeySpaceDomain{" +
                "keys=" + keys +
                ", expires=" + expires +
                ", avgTtl=" + avgTtl +
                '}';
    }

    public void setData(String data){
        String[] datas = data.split(",");
        if(null != datas){
            for(String da : datas){
                String[] ds = da.split("=");
                if(null != ds && ds.length == 2){
                    if("keys".equals(ds[0])){
                        setKeys(Integer.valueOf(ds[1]));
                    }
                    if("expires".equals(ds[0])){
                        setExpires(Integer.valueOf(ds[1]));
                    }
                    if("avg_ttl".equals(ds[0])){
                        setAvgTtl(Long.valueOf(ds[1]));
                    }
                }
            }
        }
    }

    public Integer getKeys() {
        return keys;
    }

    public void setKeys(Integer keys) {
        this.keys = keys;
    }

    public Integer getExpires() {
        return expires;
    }

    public void setExpires(Integer expires) {
        this.expires = expires;
    }

    public Long getAvgTtl() {
        return avgTtl;
    }

    public void setAvgTtl(Long avgTtl) {
        this.avgTtl = avgTtl;
    }
}
