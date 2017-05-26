package com.xijian.ecg.record.web.domain;

import java.io.Serializable;

/**
 * Created by 邵伟 on 2017/5/19 0019.
 */
public class ClientDomain implements Serializable{

    private final static long serialVersionUID = 1L;

    //ip port
    private String addr;

    //以秒计算的已连接时长
    private long age;

    //以秒计算的空闲时长
    private long idle;

    //db
    private int db;

    //最近一次执行的命令
    private String cmd;

    /**
     * O: 客户端是 MONITOR 模式下的附属节点（slave）
     S: 客户端是一般模式下（normal）的附属节点
     M: 客户端是主节点（master）
     x: 客户端正在执行事务
     b: 客户端正在等待阻塞事件
     i: 客户端正在等待 VM I/O 操作（已废弃）
     d: 一个受监视（watched）的键已被修改， EXEC 命令将失败
     c: 在将回复完整地写出之后，关闭链接
     u: 客户端未被阻塞（unblocked）
     U: 通过Unix套接字连接的客户端
     r: 客户端是只读模式的集群节点
     A: 尽可能快地关闭连接
     N: 未设置任何 flag
     */
    private String flag;

    public void setData(String data){
       String[] ss = data.split("=");
       if(null != ss && ss.length == 2){
           if("addr".equals(ss[0])){
               setAddr(ss[1]);
           }
           if("age".equals(ss[0])){
               setAge(Long.valueOf(ss[1]));
           }
           if("idle".equals(ss[0])){
               setIdle(Long.valueOf(ss[1]));
           }
           if("flags".equals(ss[0])){
               setFlag(ss[1]);
           }
           if("db".equals(ss[0])){
               setDb(Integer.valueOf(ss[1]));
           }
           if("cmd".equals(ss[0])){
               setCmd(ss[1]);
           }
       }
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }

    public long getIdle() {
        return idle;
    }

    public void setIdle(long idle) {
        this.idle = idle;
    }

    public int getDb() {
        return db;
    }

    public void setDb(int db) {
        this.db = db;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }
}
