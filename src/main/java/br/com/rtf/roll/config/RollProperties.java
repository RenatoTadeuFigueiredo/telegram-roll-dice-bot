package br.com.rtf.roll.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "roll")
@Getter
@Setter
public class RollProperties {

  private String token;
  private String username;
}
