package com.chat.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FilePathCreator {

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

  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationEvent() throws IOException {

    List<String> pathList = Arrays.asList(
        filePathChatAudio,
        filePathChatImage,
        filePathChatText,
        filePathChatVideo,
        filePathChatJson,
        filePathChatPdf,
        filePathChatApplicationZip,
        filePathUserImageAvatar
    );

    for (String path : pathList) {
      Files.createDirectories(Paths.get(path));
    }
  }
}
