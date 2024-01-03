package ua.com.hookahcat.service.scheduler;

import static ua.com.hookahcat.common.Constants.Patterns.DATE_TIME_PATTERN;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ua.com.hookahcat.configuration.CsvProperties;
import ua.com.hookahcat.configuration.NovaPoshtaApiProperties;
import ua.com.hookahcat.csvsdk.service.CsvService;
import ua.com.hookahcat.notification.configuration.EmailNotificationProperties;
import ua.com.hookahcat.notification.model.EmailNotificationData;
import ua.com.hookahcat.notification.service.EmailNotificationService;
import ua.com.hookahcat.reststarter.exception.CustomRuntimeException;
import ua.com.hookahcat.service.NewPostServiceProxy;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParcelSearchingJob {

    private final EmailNotificationService emailNotificationService;
    private final CsvProperties csvProperties;
    private final NovaPoshtaApiProperties novaPoshtaApiProperties;
    private final EmailNotificationProperties emailNotificationProperties;
    private final CsvService csvService;
    private final NewPostServiceProxy newPostServiceProxy;

    @Scheduled(cron = "*/30 * * * * *")
    public void searchNotReceivedParcels() {
        log.info("Start scheduler for searching not received parcels...");
        var unreceivedParcelsData = newPostServiceProxy.getUnreceivedParcels(
            novaPoshtaApiProperties.getApiKey(),
            novaPoshtaApiProperties.getSenderPhoneNumber());
        log.info("Found {} unreceived parcels", unreceivedParcelsData.size());

        if (CollectionUtils.isNotEmpty(unreceivedParcelsData)) {
            var exportedParcelsData = csvService.exportData(unreceivedParcelsData,
                csvProperties.getResponseHeaders(), csvProperties.getResponseFields());

            if (Objects.nonNull(exportedParcelsData)) {
                emailNotificationService.sendEmailNotification(
                    prepareEmailNotificationData(exportedParcelsData));
            }
        }
    }

    private EmailNotificationData prepareEmailNotificationData(byte[] exportedParcelsData) {
        return EmailNotificationData.builder()
            .recipient(emailNotificationProperties.getRecipient())
            .sender(emailNotificationProperties.getSender())
            .subject(emailNotificationProperties.getSubject())
            .message(emailNotificationProperties.getMessage())
            .file(generateFile(exportedParcelsData))
            .build();
    }

    private static File generateFile(byte[] exportedParcelsData) {
        var todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
        var fileName = "Not received parcels [" + todayDate + "].csv";
        var file = new File(fileName);
        try {
            FileUtils.writeByteArrayToFile(file, exportedParcelsData);
        } catch (IOException e) {
            throw new CustomRuntimeException(e.getMessage());
        }
        return file;
    }
}
