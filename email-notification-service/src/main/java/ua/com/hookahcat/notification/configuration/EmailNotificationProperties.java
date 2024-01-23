package ua.com.hookahcat.notification.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("email-notification")
public class EmailNotificationProperties {

    private String notReceivedParcelsSubject;
    private String returnedParcelsSubject;
    private String recipient;
    private String sender;
    private String notReceivedParcelsMessage;
    private String returnedParcelsMessage;
}
