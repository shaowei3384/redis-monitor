package com.xijian.ecg.record.web.result;

import java.util.List;

/**
 * Created by 邵伟 on 2017/5/19 0019.
 */
public class Response<T> {

    private Integer code = 0;

    private String msg;

    private Integer total = 0;

    private List<T> rows;

    @Override
    public String toString() {
        return "Response{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", total=" + total +
                ", rows=" + rows == null ? "[]" : rows.toString() +
                '}';
    }

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

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }
}
