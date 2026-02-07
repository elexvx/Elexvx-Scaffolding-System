package com.tencent.tdesign;

import com.anji.captcha.config.AjCaptchaServiceAutoConfiguration;
import com.anji.captcha.properties.AjCaptchaProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@Import(AjCaptchaServiceAutoConfiguration.class)
@EnableConfigurationProperties(AjCaptchaProperties.class)
@MapperScan("com.tencent.tdesign.mapper")
public class TDesignApplication {
  public static void main(String[] args) {
    SpringApplication.run(TDesignApplication.class, args);
  }
}
