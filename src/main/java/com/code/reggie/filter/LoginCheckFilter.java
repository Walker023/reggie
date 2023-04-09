package com.code.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.code.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    // 用于匹配路径
    public static final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private static final String[] urls = new String[]{
      "/employee/login",
      "/employee/logout",
      "/employee/register",
      "/backend/**",
      "front/**"
    };

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        // 获取请求路径
        String uri = request.getRequestURI();
        // 如果是登录或者注册请求，直接放行
        if(check(uri)){
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        // 判断是否登录
        Object employee = request.getSession().getAttribute("employee");
        if(employee == null){
            response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
            return;
        }
        // 放行
        filterChain.doFilter(servletRequest, servletResponse);
    }
    // 判断是否是登录或者注册请求
    public boolean check(String requestURI){
        for (String url : urls) {
            if(antPathMatcher.match(url, requestURI)){
                return true;
            }
        }
        return false;
    }
}
