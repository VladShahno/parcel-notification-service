package ua.com.hookahcat.notification.service;

import ua.com.hookahcat.notification.model.EmailNotificationData;

public interface EmailNotificationService {

    void sendEmailNotification(EmailNotificationData emailNotificationData);
}
