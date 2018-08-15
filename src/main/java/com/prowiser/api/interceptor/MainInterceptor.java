package com.prowiser.api.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MainInterceptor implements HandlerInterceptor {
    private static Logger log = LoggerFactory.getLogger(MainInterceptor.class);
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        String url = request.getRequestURI();
        //校验参数中的sign  防止重复提交
        String sign = request.getParameter("sign");
        if(sign==null && url.startsWith("/send/msg")){
            log.info("sign为空");
            response.setCharacterEncoding("utf-8");
            response.getWriter().write("sign为空");
            return false;
        }

        log.info("request sign = "+sign);
        String _sign = (String) request.getSession().getAttribute("sign");
        log.info("session sign = "+_sign);
        if(sign!=null && _sign!=null && sign.equals(_sign)){
            log.info("重复提交");
            response.setCharacterEncoding("utf-8");
            response.getWriter().write("请不要重复提交");
            return false;
        }
        request.getSession().setAttribute("sign",sign);
        return true;
    }


    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o,
                           ModelAndView modelAndView) throws Exception {

    }


    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                Object o, Exception e) throws Exception {

    }

}
