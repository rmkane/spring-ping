package org.acme.ping.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.acme.ping", "org.acme.security"})
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}