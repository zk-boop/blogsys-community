package com.zk.projectboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.beans.factory.annotation.Value;

/**
 * Session配置类
 * 配置Session和Cookie的相关属性
 */
@Configuration
public class SessionConfig {

    private final boolean secureCookie;
    private final String sameSite;

    public SessionConfig(
            @Value("${server.servlet.session.cookie.secure:false}") boolean secureCookie,
            @Value("${server.servlet.session.cookie.same-site:Lax}") String sameSite) {
        this.secureCookie = secureCookie;
        this.sameSite = sameSite;
    }

    /**
     * 配置Cookie序列化器
     * 设置Cookie的路径、安全性、过期时间等属性
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("BLOGSESSION"); // 设置Cookie名称
        serializer.setCookiePath("/"); // 设置Cookie路径
        serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$"); // 允许子域名共享
        serializer.setCookieMaxAge(3600); // Cookie过期时间(秒)
        serializer.setUseHttpOnlyCookie(true); // 启用HttpOnly，防止XSS攻击
        serializer.setUseSecureCookie(secureCookie);
        serializer.setSameSite(sameSite);
        return serializer;
    }
}
