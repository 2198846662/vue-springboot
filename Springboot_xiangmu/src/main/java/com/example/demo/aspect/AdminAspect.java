package com.example.demo.aspect;

import com.example.demo.annotation.RequireAdmin;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AdminAspect {

    @Autowired
    private UserRepository userRepository;

    @org.aspectj.lang.annotation.Before("@annotation(requireAdmin)")
    public void checkAdmin(RequireAdmin requireAdmin) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("未登录");
        }

        token = token.substring(7);
        Long userId = (Long) request.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException("无法获取用户信息");
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("需要管理员权限");
        }
    }
}