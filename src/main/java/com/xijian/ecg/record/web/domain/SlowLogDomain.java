package com.xijian.ecg.record.web.domain;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by 邵伟 on 2017/5/19 0019.
 */
public class SlowLogDomain implements Serializable{

    private final static long serialVersionUID = 1L;

    private long id;
    private long timeStamp;
    private long executionTime;
    private List<String> args;
    private String datetime;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SlowLogDomain{")
                .append("id=").append(id)
                .append(",timeStimep=").append(timeStamp)
                .append(",executiontime=").append(executionTime)
                .append(",datetime=").append(datetime)
                .append(",args=").append(args == null ? "[]" : args.toString())
                .append("}");
        return sb.toString();
    }

    private void parseTime(){
        Date date = new Date(timeStamp * 1000);
        datetime = DateFormatUtils.format(date,"yyyy-MM-dd HH:mm:ss");
    }
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
        parseTime();
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }
}
