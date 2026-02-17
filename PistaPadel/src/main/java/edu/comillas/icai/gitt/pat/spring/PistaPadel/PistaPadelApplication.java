package edu.comillas.icai.gitt.pat.spring.PistaPadel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PistaPadelApplication {

	public static void main(String[] args) {
		SpringApplication.run(PistaPadelApplication.class, args);
	}
}
