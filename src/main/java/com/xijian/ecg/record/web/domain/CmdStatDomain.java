package com.xijian.ecg.record.web.domain;

import java.io.Serializable;

/**
 * Created by 邵伟 on 2017/5/19 0019.
 */
public class CmdStatDomain implements Serializable{

    private final static long serialVersionUID = 1L;

    //命令名称
    private String cmdName;

    //调用次数
    private Integer calls;

    //调用总耗时毫秒
    private Integer usec;

    //每个命令平均耗时 毫秒
    private Double usecPer;

    @Override
    public String toString() {
        return "CmdStatDomain{" +
                "cmdName='" + cmdName + '\'' +
                ", calls=" + calls +
                ", usec=" + usec +
                ", usecPer=" + usecPer +
                '}';
    }

    public void setData(String key, String val){
        String[] keys = key.split("_");
        setCmdName(keys[1]);
        String[] vals = val.split(",");
        if(null != vals){
            for(String v : vals){
                String[] lines = v.split("=");
                if(null != lines && lines.length == 2){
                    if("calls".equals(lines[0])){
                        setCalls(Integer.valueOf(lines[1]));
                    }
                    if("usec".equals(lines[0])){
                        setUsec(Integer.valueOf(lines[1]));
                    }
                    if("usec_per_call".equals(lines[0])){
                        setUsecPer(Double.valueOf(lines[1]));
                    }
                }
            }
        }
    }

    public String getCmdName() {
        return cmdName;
    }

    public void setCmdName(String cmdName) {
        this.cmdName = cmdName;
    }

    public Integer getCalls() {
        return calls;
    }

    public void setCalls(Integer calls) {
        this.calls = calls;
    }

    public Integer getUsec() {
        return usec;
    }

    public void setUsec(Integer usec) {
        this.usec = usec;
    }

    public Double getUsecPer() {
        return usecPer;
    }

    public void setUsecPer(Double usecPer) {
        this.usecPer = usecPer;
    }
}
