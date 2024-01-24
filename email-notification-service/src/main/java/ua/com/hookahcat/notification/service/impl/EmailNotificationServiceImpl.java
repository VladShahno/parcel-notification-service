package ua.com.hookahcat.notification.service.impl;

import static net.logstash.logback.argument.StructuredArguments.kv;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import ua.com.hookahcat.notification.model.EmailNotificationData;
import ua.com.hookahcat.notification.service.EmailNotificationService;
import ua.com.hookahcat.reststarter.exception.InternalErrorException;

@Service
@Slf4j
@AllArgsConstructor
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private final JavaMailSender javaMailSender;

    @Override
    public void sendEmailNotification(EmailNotificationData emailNotificationData) {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper;
        var file = emailNotificationData.getFile();

        try {
            mimeMessageHelper = new MimeMessageHelper(message, true);
            mimeMessageHelper.setTo(emailNotificationData.getRecipient());
            mimeMessageHelper.setFrom(emailNotificationData.getSender());
            mimeMessageHelper.setSubject(emailNotificationData.getSubject());
            mimeMessageHelper.setText(emailNotificationData.getMessage());
            if (Objects.nonNull(file)) {
                mimeMessageHelper.addAttachment(file.getName(), file);
            }
        } catch (MessagingException e) {
            throw new InternalErrorException(e.getMessage());
        }
        log.info("Sending new notification to {}",
            kv("email", emailNotificationData.getRecipient()));
        javaMailSender.send(message);
    }
}
