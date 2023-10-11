package com.min.analysis.inspector;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DoubleSlashInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String requestURI = request.getRequestURI();
        String correctedURI = requestURI.replaceAll("//+", "/");
        if (!requestURI.equals(correctedURI)) {
            response.sendRedirect(correctedURI);
            return false;
        }
        return true;
    }
}