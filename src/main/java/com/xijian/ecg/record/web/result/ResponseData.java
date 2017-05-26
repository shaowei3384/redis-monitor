package com.xijian.ecg.record.web.result;

/**
 * Created by 邵伟 on 2017/5/19 0019.
 */
public class ResponseData<T> {

    private Integer code = 0;

    private String msg;

    private T data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
