package com.worketa.worketa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan("com.worketa")
@EntityScan("com.worketa")
@EnableJpaRepositories("com.worketa")
@EnableJpaAuditing
@EnableScheduling
public class WorketaApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorketaApplication.class, args);
	}

}
