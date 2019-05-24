package br.com.rtf.roll;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;

@SpringBootApplication
public class RollApplication {

	public static void main(String[] args) {
		ApiContextInitializer.init();

		SpringApplication.run(RollApplication.class, args);
	}

}
