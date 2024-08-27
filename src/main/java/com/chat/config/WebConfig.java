package com.chat.config;

import java.util.List;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("#{'${server.cors-urls}'.split(',')}")
  private List<String> corsUrlList;

  @Override
  public void addCorsMappings(@NonNull CorsRegistry registry) {
    for (String url : corsUrlList) {
      registry.addMapping("/**")
          .allowedOrigins(url)
          .allowedMethods("GET", "POST", "PUT", "DELETE")
          .allowCredentials(true);
    }
  }

  @Bean
  public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    for (String url : corsUrlList) {
      config.addAllowedOrigin(url);
    }
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }
}