package com.chat;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.chat.exception.MailException;
import com.chat.service.MailService;
import java.util.Arrays;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

  @InjectMocks
  private MailService mailService;

  @Mock
  private JavaMailSender javaMailSender;

  private String testEmail;
  private String testTitle;
  private String testText;

  @BeforeEach
  void setUp() {
    testEmail = "test@test.com";
    testTitle = "test";
    testText = "test";
  }

  @Test
  void testCreateEmailForm() {
    // given
    String toEmail = testEmail;
    String title = testTitle;
    String text = testText;

    // when
    SimpleMailMessage emailForm = mailService.createEmailForm(toEmail, title, text);

    // then
    assertAll(
        () -> assertEquals(toEmail,
            Arrays.stream(Objects.requireNonNull(emailForm.getTo())).findFirst().orElse(null)),
        () -> assertEquals(title, emailForm.getSubject()),
        () -> assertEquals(text, emailForm.getText())
    );
  }

  @Test
  void testSendEmail() {
    // given
    String toEmail = testEmail;
    String title = testTitle;
    String text = testText;
    SimpleMailMessage emailForm = mailService.createEmailForm(toEmail, title, text);

    // when
    mailService.sendEmail(toEmail, title, text);

    // then
    verify(javaMailSender).send(emailForm);
  }

  @Test
  void testSendEmailThrowsException() {
    // given
    String toEmail = testEmail;
    String title = testTitle;
    String text = testText;
    SimpleMailMessage emailForm = mailService.createEmailForm(toEmail, title, text);

    doThrow(new MailException("MAIL:SEND_MAIL_FAIL")).when(javaMailSender).send(emailForm);

    // when
    MailException mailException = assertThrows(MailException.class,
        () -> mailService.sendEmail(toEmail, title, text));

    // then
    assertEquals("MAIL:SEND_MAIL_FAIL", mailException.getId());
  }
}