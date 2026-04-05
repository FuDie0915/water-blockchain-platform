package com.water.platform.base.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * @description: 请求拦截校验
 **/
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    // 注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
                    StpUtil.checkLogin();
                }))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/loginOrRegister",
                        "/water/data/collect",
                        "/profile/upload/**",
                        "/static/**",
                        "/v3/api-docs",
                        "/swagger-ui/**",
                        "/error/**",
                        "/water/data/collect",
                        "/common/indexData",
                        "/aircraft/issues/collect",
                        "/doc.html");
    }
}

