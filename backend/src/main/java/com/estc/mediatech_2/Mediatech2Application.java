package com.estc.mediatech_2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Enable scheduled tasks for token cleanup
public class Mediatech2Application {

    public static void main(String[] args) {
        SpringApplication.run(Mediatech2Application.class, args);
    }

}
