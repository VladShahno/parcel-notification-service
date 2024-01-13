package ua.com.hookahcat.telegram.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableConfigurationProperties
@ComponentScan(basePackages = "ua.com.hookahcat")
public class BotMainApp {

    public static void main(String[] args) {
        SpringApplication.run(BotMainApp.class, args);
    }
}