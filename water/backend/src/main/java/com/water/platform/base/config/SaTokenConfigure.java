package com.water.platform.base.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @description: 请求拦截校验
 * SaInterceptor 仅负责从 satoken header 读取 token 并注入 Sa-Token 会话上下文，
 * 具体的角色权限校验由 @AuthCheck + AuthInterceptor AOP 完成。
 **/
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    // 注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
                    // 仅建立会话上下文，不做强制登录检查
                    // 强制登录 + 角色校验由 @AuthCheck 注解 + AuthInterceptor 负责
                }))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/captcha",
                        "/user/register/farmers",
                        "/user/register/manager",
                        "/user/login/admin",
                        "/user/login/farmers",
                        "/user/login/manager",
                        "/water/data/collect",
                        "/announcement/list",
                        "/profile/upload/**",
                        "/static/**",
                        "/v3/api-docs",
                        "/swagger-ui/**",
                        "/error/**",
                        "/common/indexData");
    }
}
