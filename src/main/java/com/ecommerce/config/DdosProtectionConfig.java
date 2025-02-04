package com.ecommerce.config;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class DdosProtectionConfig {

    @Bean
    public FilterRegistrationBean<Filter> ddosFilter() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        
        registrationBean.setFilter(new OncePerRequestFilter() {
            private Map<String, RequestCount> requestCounts = new ConcurrentHashMap<>();
            
            @Override
            protected void doFilterInternal(HttpServletRequest request, 
                                          HttpServletResponse response, 
                                          FilterChain chain) throws ServletException, IOException {
                String ip = request.getRemoteAddr();
                RequestCount count = requestCounts.computeIfAbsent(ip, 
                    k -> new RequestCount());
                
                if (count.isMaximumReached()) {
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    return;
                }
                
                count.increment();
                chain.doFilter(request, response);
            }
        });
        
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
    
    private static class RequestCount {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long lastResetTime = System.currentTimeMillis();
        
        public boolean isMaximumReached() {
            resetIfNeeded();
            return count.get() > 100; // 100 requêtes max par minute
        }
        
        public void increment() {
            resetIfNeeded();
            count.incrementAndGet();
        }
        
        private void resetIfNeeded() {
            long current = System.currentTimeMillis();
            if (current - lastResetTime > 60000) { // Reset toutes les minutes
                count.set(0);
                lastResetTime = current;
            }
        }
    }
} 