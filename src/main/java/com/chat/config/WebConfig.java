package com.chat.config;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  @Value("${server.file-path.server.icon}")
  private String filePathServerIcon;
  @Value("${server.file-path.base}")
  private String filePathBase;
  @Value("${server.file-path.chat.audio}")
  private String filePathChatAudio;
  @Value("${server.file-path.chat.image}")
  private String filePathChatImage;
  @Value("${server.file-path.chat.text}")
  private String filePathChatText;
  @Value("${server.file-path.chat.video}")
  private String filePathChatVideo;
  @Value("${server.file-path.chat.application.json}")
  private String filePathChatJson;
  @Value("${server.file-path.chat.application.pdf}")
  private String filePathChatPdf;
  @Value("${server.file-path.chat.application.zip}")
  private String filePathChatApplicationZip;
  @Value("${server.file-path.user.image.avatar}")
  private String filePathUserImageAvatar;

  private final Map<String, String> filePaths = new HashMap<>();

  @PostConstruct
  public void initFilePaths() {
    filePaths.put("icon", filePathServerIcon);
    filePaths.put("audio", filePathChatAudio);
    filePaths.put("image", filePathChatImage);
    filePaths.put("text", filePathChatText);
    filePaths.put("video", filePathChatVideo);
    filePaths.put("json", filePathChatJson);
    filePaths.put("pdf", filePathChatPdf);
    filePaths.put("zip", filePathChatApplicationZip);
    filePaths.put("avatar", filePathUserImageAvatar);
  }

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
    // 파일 경로를 반복문으로 처리하여 중복 코드 제거
    filePaths.forEach((key, path) ->
        registry.addResourceHandler(path + "**")
            .addResourceLocations("file:" + filePathBase + path)
    );
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