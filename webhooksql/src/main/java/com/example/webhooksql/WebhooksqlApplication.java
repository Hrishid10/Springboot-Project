package com.example.webhooksql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@SpringBootApplication
public class WebhooksqlApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(WebhooksqlApplication.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", "9095")); 
        app.run(args);
    }
}
