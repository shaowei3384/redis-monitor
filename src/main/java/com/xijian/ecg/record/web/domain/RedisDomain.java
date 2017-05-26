package com.xijian.ecg.record.web.domain;

import java.io.Serializable;

/**
 * Created by 邵伟 on 2017/5/18 0018.
 */
public class RedisDomain implements Serializable{

    private final static long serialVersionUID = 1L;

    private String ip;

    private Integer port;

    private String passwd;

    @Override
    public String toString() {
        return "RedisDomain{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", passwd='" + passwd + '\'' +
                '}';
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }
}
