package ua.com.hookahcat.telegram.bot.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "telegram-bot.hookahcat-pns")
@Data
public class TelegramBotProperties {

    private String botToken;
    private String botUsername;

}
