package ua.com.hookahcat.csvsdk.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ua.com.hookahcat.csvsdk.service.CsvService;
import ua.com.hookahcat.csvsdk.service.MessageService;

@Configuration
public class CsvConfiguration {

    @Bean
    public MessageService messageService(MessageSource messageSource) {
        return new MessageService(messageSource);
    }

    @Bean
    public CsvService csvService(MessageService messageService) {
        return new CsvService(messageService);
    }
}
