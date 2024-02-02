package com.shuking.pairBackend.config;

import com.shuking.pairBackend.common.GlobalInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 全局拦截器   拦截未登录请求
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new GlobalInterceptor())
                //  放行登录  注册
                .addPathPatterns("/user/**", "/team/**")
                .excludePathPatterns("/user/login", "/user/register");

                /*.excludePathPatterns("/user/login","/user/register")
                //不拦截路由
                .excludePathPatterns("/user/login","/user/register")
                .excludePathPatterns("/doc.html/**","/doc.html#/**")
                .excludePathPatterns("/swagger-ui.html","/swagger-ui/**")
                //让后台文档不可用可以直接注释掉这两个v2 v3
                .excludePathPatterns("/v2/api-docs","/v3/api-docs")    // swagger api json
                .excludePathPatterns("/swagger-resources/configuration/ui")// 用来获取支持的动作
                .excludePathPatterns("/swagger-resources")       // 用来获取api-docs的URI
                .excludePathPatterns("/swagger-resources/configuration/security")    // 安全选项
                .excludePathPatterns("/swagger-resources/**")
                //补充路径，近期在搭建swagger接口文档时，通过浏览器控制台发现该/webjars路径下的文件被拦截，故加上此过滤条件即可。(2020-10-23)
                .excludePathPatterns("/webjars/**")
                .excludePathPatterns("/error");*/
    }

    /**
     * 全局跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowCredentials(true)
                // .allowedOrigins("http://127.0.0.1:5173")
                // 放行哪些域名（必须用 patterns，否则 * 会和 allowCredentials 冲突）
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*");
    }
}
