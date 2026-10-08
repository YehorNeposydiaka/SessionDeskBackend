package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SessionDeskBackendApp {

    public static void main(String[] args) {
        SpringApplication.run(SessionDeskBackendApp.class, args);
    }
}