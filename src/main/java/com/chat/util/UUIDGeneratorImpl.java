package com.chat.util;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UUIDGeneratorImpl implements UUIDGenerator {

  @Override
  public String generateUUID() {
    return UUID.randomUUID().toString();
  }
}
