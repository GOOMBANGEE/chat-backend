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
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String REFRESH_TOKEN = "Refresh-Token";

  @Value("#{'${server.cors-urls}'.split(',')}")
  private List<String> corsUrlList;
  @Value("${server.image-path.base}")
  private String imagePathBase;
  @Value("${server.image-path.avatar}")
  private String imagePathAvatar;

  @Override
  public void addCorsMappings(@NonNull CorsRegistry registry) {
    for (String url : corsUrlList) {
      registry.addMapping("/**")
          .allowedOrigins(url)
          .allowedMethods("GET", "POST", "PUT", "DELETE")
          .allowCredentials(true);
    }
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/image/avatar/**") // 이 경로를 통해 이미지에 접근시
        .addResourceLocations("file:" + imagePathBase + imagePathAvatar); // 실제 이미지 저장 경로접근
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
    config.addExposedHeader(AUTHORIZATION_HEADER);
    config.addExposedHeader(REFRESH_TOKEN);
    return new CorsFilter(source);
  }
}