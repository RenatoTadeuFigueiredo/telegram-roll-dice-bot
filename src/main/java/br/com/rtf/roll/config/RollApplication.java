package br.com.rtf.roll.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;

@SpringBootApplication(scanBasePackages = {
    "br.com.rtf.roll"
})
public class RollApplication {

  public static void main(String[] args) {
    ApiContextInitializer.init();

    SpringApplication.run(RollApplication.class, args);
  }

}
