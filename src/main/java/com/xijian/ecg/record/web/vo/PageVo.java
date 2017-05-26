package com.xijian.ecg.record.web.vo;

/**
 * Created by 邵伟 on 2017/5/19 0019.
 */
public class PageVo {

    private Integer pageNo;

    private Integer pageSize;

    @Override
    public String toString() {
        return "PageVo{" +
                "pageNo=" + pageNo +
                ", pageSize=" + pageSize +
                '}';
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
