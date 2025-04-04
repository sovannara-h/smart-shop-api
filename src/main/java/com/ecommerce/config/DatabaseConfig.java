package com.ecommerce.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {

  @Value("${spring.datasource.url}")
  private String jdbcUrl;

  @Value("${spring.datasource.username}")
  private String username;

  @Value("${spring.datasource.password}")
  private String password;

  @Bean
  public HikariDataSource dataSource() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(jdbcUrl);
    config.setUsername(username);
    config.setPassword(password);

    config.setMaximumPoolSize(20);
    config.setMinimumIdle(5);
    config.setIdleTimeout(300000);
    config.setConnectionTimeout(20000);
    config.setMaxLifetime(1800000);
    config.setLeakDetectionThreshold(10000);

    // Configuration SSL pour Supabase
    config.addDataSourceProperty("ssl", "true");
    config.addDataSourceProperty("sslmode", "require");

    return new HikariDataSource(config);
  }
}
