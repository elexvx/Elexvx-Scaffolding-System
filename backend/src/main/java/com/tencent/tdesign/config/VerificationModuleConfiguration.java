package com.tencent.tdesign.config;

import com.tencent.tdesign.service.EmailSenderService;
import com.tencent.tdesign.service.SmsSenderService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VerificationModuleConfiguration {

  @Bean
  @ConditionalOnProperty(prefix = "tdesign.modules.sms", name = "enabled", havingValue = "true", matchIfMissing = true)
  @ConditionalOnClass(name = {
    "com.aliyuncs.DefaultAcsClient",
    "com.tencentcloudapi.sms.v20210111.SmsClient"
  })
  public SmsSenderService smsSenderService() {
    return new SmsSenderService();
  }

  @Bean
  @ConditionalOnProperty(prefix = "tdesign.modules.email", name = "enabled", havingValue = "true", matchIfMissing = true)
  @ConditionalOnClass(name = {
    "org.springframework.mail.javamail.JavaMailSenderImpl",
    "jakarta.mail.internet.MimeMessage"
  })
  public EmailSenderService emailSenderService() {
    return new EmailSenderService();
  }
}
