package com.xijian.ecg.record.web.controller;

import com.xijian.ecg.record.web.domain.AlermItem;
import com.xijian.ecg.record.web.domain.ClientDomain;
import com.xijian.ecg.record.web.domain.SlowLogDomain;
import com.xijian.ecg.record.web.result.Response;
import com.xijian.ecg.record.web.result.ResponseData;
import com.xijian.ecg.record.web.service.AlermManager;
import com.xijian.ecg.record.web.service.FeatureManager;
import com.xijian.ecg.record.web.util.ResponseUtil;
import com.xijian.ecg.record.web.vo.PageVo;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by 邵伟 on 2017/5/19 0019.
 */
public class RedisHostHandler extends AbstractHandler{

    private FeatureManager manager;

    public RedisHostHandler(FeatureManager featureManager){
        this.manager = featureManager;
        this.manager.start();
    }

    @Override
    public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        String action = httpServletRequest.getParameter("action");
        try{
            if(action.equals("queryList")){
                PageVo vo = new PageVo();
                Integer pageNo = Integer.valueOf(httpServletRequest.getParameter("page"));
                Integer pagesize = Integer.valueOf(httpServletRequest.getParameter("rows"));
                vo.setPageNo(pageNo);
                vo.setPageSize(pagesize);
                Response rsp = manager.queryRedisHostList(vo);
                ResponseUtil.write(httpServletResponse,rsp);
            }
            else if(action.equals("addHost")){
                String ip = httpServletRequest.getParameter("ip");
                int port = Integer.valueOf(httpServletRequest.getParameter("port"));
                String passwd = httpServletRequest.getParameter("passwd");
                Assert.hasLength(ip,"ip不能为空");
                Assert.isTrue(port > 0,"port必须大于0");
                manager.addRedisHost(ip,port,passwd);
                Response rsp = new Response();
                ResponseUtil.write(httpServletResponse,rsp);
            }
            else if(action.equals("removeHost")){
                String ip = httpServletRequest.getParameter("ip");
                int port = Integer.valueOf(httpServletRequest.getParameter("port"));
                Assert.hasLength(ip,"ip不能为空");
                Assert.isTrue(port > 0,"port必须大于0");
                manager.removeRedisHost(ip,port);
                Response rsp = new Response();
                ResponseUtil.write(httpServletResponse,rsp);
            }else if(action.equals("querySlowLog")){
                String ip = httpServletRequest.getParameter("ip");
                int port = Integer.valueOf(httpServletRequest.getParameter("port"));
                PageVo vo = new PageVo();
                Integer pageNo = Integer.valueOf(httpServletRequest.getParameter("page"));
                Integer pagesize = Integer.valueOf(httpServletRequest.getParameter("rows"));
                vo.setPageNo(pageNo);
                vo.setPageSize(pagesize);
                Response<SlowLogDomain> rsp = manager.querySlowLog(ip,port,vo);
                ResponseUtil.write(httpServletResponse,rsp);
            }else if(action.equals("queryClientList")){
                String ip = httpServletRequest.getParameter("ip");
                int port = Integer.valueOf(httpServletRequest.getParameter("port"));
                PageVo vo = new PageVo();
                Integer pageNo = Integer.valueOf(httpServletRequest.getParameter("page"));
                Integer pagesize = Integer.valueOf(httpServletRequest.getParameter("rows"));
                vo.setPageNo(pageNo);
                vo.setPageSize(pagesize);
                Response<ClientDomain> rsp = manager.queryClientList(ip,port,vo);
                ResponseUtil.write(httpServletResponse,rsp);
            }else if(action.equals("queryMetrics")){
                String ip = httpServletRequest.getParameter("ip");
                int port = Integer.valueOf(httpServletRequest.getParameter("port"));
                String metric = httpServletRequest.getParameter("metric");
                ResponseData<Object> rsp = manager.queryMetrics(ip,port,metric);
                ResponseUtil.write(httpServletResponse,rsp);
            }else if(action.equals("queryMetricList")){
                String ip = httpServletRequest.getParameter("ip");
                int port = Integer.valueOf(httpServletRequest.getParameter("port"));
                Response<String> rsp = manager.queryMetrics(ip,port);
                ResponseUtil.write(httpServletResponse,rsp);
            }else if(action.equals("queryConfigList")){
                ResponseData rsp = manager.queryConfigList();
                ResponseUtil.write(httpServletResponse,rsp);
            }else if(action.equals("addConfig")){
                String key = httpServletRequest.getParameter("key");
                String value = httpServletRequest.getParameter("value");
                Response rsp = manager.adddConfig(key,value);
                ResponseUtil.write(httpServletResponse,rsp);
            }else if(action.equals("removeConfig")){
                String key = httpServletRequest.getParameter("key");
                Response rsp = manager.removeConfig(key);
                ResponseUtil.write(httpServletResponse,rsp);
            }else if(action.equals("queryAlermList")){
                PageVo vo = new PageVo();
                Integer pageNo = Integer.valueOf(httpServletRequest.getParameter("page"));
                Integer pagesize = Integer.valueOf(httpServletRequest.getParameter("rows"));
                vo.setPageNo(pageNo);
                vo.setPageSize(pagesize);
                Response<AlermItem> rsp = manager.queryAlermList(vo);
                ResponseUtil.write(httpServletResponse,rsp);
            }else if(action.equals("addAlermList")){
                String json = httpServletRequest.getParameter("json");
                Response rsp = manager.addAlerms(json);
                ResponseUtil.write(httpServletResponse,rsp);
            }else{
                Response rsp = new Response();
                rsp.setMsg("没有该请求");
                ResponseUtil.write(httpServletResponse,rsp);
            }
        }catch (Exception e){
            Response rsp = new Response();
            rsp.setMsg(e.getMessage());
            ResponseUtil.write(httpServletResponse,rsp);
        }
    }
}
