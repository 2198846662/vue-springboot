package com.example.demo.interceptor;

import com.example.demo.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    private boolean isPublicApi(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        if (!"GET".equalsIgnoreCase(method)) {
            return false;
        }

        // 景点公开接口
        if ("/api/scenery".equals(uri)) {
            return true;
        }
        if (uri.matches("^/api/scenery/\\d+$")) {
            return true;
        }
        if (uri.matches("^/api/scenery/\\d+/comments$")) {
            return true;
        }
        if (uri.matches("^/api/scenery/\\d+/favorite/count$")) {
            return true;
        }
        if (uri.matches("^/api/scenery/\\d+/rating/summary$")) {
            return true;
        }

        // 分类公开接口
        if ("/api/categories".equals(uri)) {
            return true;
        }
        return uri.matches("^/api/categories/\\d+$");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (isPublicApi(request)) {
            return true;
        }

        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (JwtUtil.validateToken(token)) {
                request.setAttribute("userId", JwtUtil.getUserId(token));
                request.setAttribute("username", JwtUtil.getUsername(token));
                return true;
            }
        }

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("message", "未登录或token无效");
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
        return false;
    }
}
