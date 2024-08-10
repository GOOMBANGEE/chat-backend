package com.chat.service;

import com.chat.exception.MailException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MailService {

  private final JavaMailSender javaMailSender;

  @Async("threadPoolTaskExecutor")
  public void sendEmail(String toEmail, String title, String text) {
    SimpleMailMessage emailForm = createEmailForm(toEmail, title, text);
    try {
      javaMailSender.send(emailForm);
    } catch (Exception e) {
      throw new MailException("MAIL:SEND_MAIL_FAIL");
    }
  }

  public SimpleMailMessage createEmailForm(String toEmail, String subject, String text) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(toEmail);
    message.setSubject(subject);
    message.setText(text);
    return message;
  }
}
