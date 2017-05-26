package com.xijian.ecg.record.web.util;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by 邵伟 on 2017/5/19 0019.
 */
public class ResponseUtil {

    public final static void write(HttpServletResponse httpServletResponse,Object data){
        try{
            httpServletResponse.setCharacterEncoding("UTF-8");
            httpServletResponse.setContentType("application/json");
            httpServletResponse.setHeader("Access-Control-Allow-Origin","*");
            httpServletResponse.getWriter().write(JacksonUtil.toJson(data));
            httpServletResponse.getWriter().flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
