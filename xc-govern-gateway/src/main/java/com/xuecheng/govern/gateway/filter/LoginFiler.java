package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.govern.gateway.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.jwt.Jwt;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginFiler extends ZuulFilter {

    @Autowired
    AuthService authService;

    @Override
    public String filterType() {
        //四种类型：pre、routing、post、errpor
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        //上下文对象
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();
        //查询身份令牌
        String access_token = authService.getTokenFromCookie(request);
        if (access_token == null) {
            //拒绝访问
            this.access_denied();
        }
        //查询jwt令牌
        String jwt = authService.getJwtFromHeader(request);
        if (jwt == null) {
            //拒绝访问
            this.access_denied();
        }
        return null;
    }

    //拒绝访问
    private void access_denied(){
        RequestContext currentContext = RequestContext.getCurrentContext();
        currentContext.setSendZuulResponse(false);//拒绝访问
        //设置响应内容
        ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
        String jsonString = JSON.toJSONString(responseResult);
        //设置状态码
        currentContext.setResponseBody(jsonString);
        //设置状态码
        currentContext.setResponseStatusCode(200);

        HttpServletResponse response = currentContext.getResponse();
        response.setContentType("application/json;charset=utf-8");
    }
}
