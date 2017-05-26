package com.xijian.ecg.record.web.util;

import com.xijian.ecg.record.web.domain.RedisDomain;
import com.xijian.ecg.record.web.result.Response;
import com.xijian.ecg.record.web.vo.PageVo;

import java.util.List;

/**
 * Created by 邵伟 on 2017/5/19 0019.
 */
public class PageUtil {

    public final static<T> Response<T> page(List<T> list, PageVo pageVo){
        Response<T> rsp = new Response<T>();
        int size = list.size();
        int begin = (pageVo.getPageNo() - 1) * pageVo.getPageSize();
        int end = pageVo.getPageNo() * pageVo.getPageSize();
        rsp.setRows(list.subList(begin,end > size ? size : end));
        rsp.setTotal(size / pageVo.getPageSize() + 1);
        return rsp;
    }
}
