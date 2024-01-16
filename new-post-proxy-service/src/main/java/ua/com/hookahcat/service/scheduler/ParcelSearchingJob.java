package ua.com.hookahcat.service.scheduler;

import static ua.com.hookahcat.common.Constants.MAX_STORAGE_DAYS_FOUR;
import static ua.com.hookahcat.common.Constants.MAX_STORAGE_DAYS_NINE;
import static ua.com.hookahcat.common.Constants.Patterns.DATE_PATTERN;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
import ua.com.hookahcat.model.request.ParcelReturnMethodProperties;
import ua.com.hookahcat.model.request.ParcelReturnRequest;
import ua.com.hookahcat.model.response.DocumentDataResponse;
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

    //@Scheduled(cron = "${scheduled.parcel-search-job}")
    public void searchUnReceivedParcels() {
        log.info("Start scheduler for searching not received parcels...");
        var exportedParcelsData = getUnReceivedParcelsCsv(MAX_STORAGE_DAYS_FOUR);
        sendEmailWithNotReceivedParcels(exportedParcelsData);
    }

    //@Scheduled(cron = "${scheduled.parcel-search-job}")
    public void createParcelReturnOrder() {
        var unreceivedParcelsData = getUnreceivedParcelsByMaxStorageDays(MAX_STORAGE_DAYS_NINE);

        createParcelReturnRequest(unreceivedParcelsData).forEach(
            newPostServiceProxy::createParcelReturnOrder);
    }

    public byte[] getUnReceivedParcelsCsv(long maxStorageDays) {
        var unreceivedParcelsData = getUnreceivedParcelsByMaxStorageDays(maxStorageDays);

        if (CollectionUtils.isNotEmpty(unreceivedParcelsData)) {
            return csvService.exportData(unreceivedParcelsData,
                csvProperties.getResponseHeaders(), csvProperties.getResponseFields());
        }
        return new byte[0];
    }

    public static File createCsvFile(byte[] exportedParcelsData) {
        var todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN));
        var fileName = "Not received parcels [" + todayDate + "].csv";
        var file = new File(fileName);
        try {
            FileUtils.writeByteArrayToFile(file, exportedParcelsData);
        } catch (IOException e) {
            throw new CustomRuntimeException(e.getMessage());
        }
        return file;
    }

    private void sendEmailWithNotReceivedParcels(byte[] exportedParcelsData) {
        if (Objects.nonNull(exportedParcelsData)) {
            emailNotificationService.sendEmailNotification(
                prepareEmailNotificationData(exportedParcelsData));
        }
    }

    private EmailNotificationData prepareEmailNotificationData(byte[] exportedParcelsData) {
        return EmailNotificationData.builder()
            .recipient(emailNotificationProperties.getRecipient())
            .sender(emailNotificationProperties.getSender())
            .subject(emailNotificationProperties.getSubject())
            .message(emailNotificationProperties.getMessage())
            .file(createCsvFile(exportedParcelsData))
            .build();
    }

    private List<DocumentDataResponse> getUnreceivedParcelsByMaxStorageDays(long maxStorageDays) {
        var unreceivedParcelsData = newPostServiceProxy.getUnreceivedParcels(
            novaPoshtaApiProperties.getApiKey(),
            novaPoshtaApiProperties.getSenderPhoneNumber(), maxStorageDays);
        log.info("Found {} unreceived parcels", unreceivedParcelsData.size());
        return unreceivedParcelsData;
    }

    private List<ParcelReturnRequest> createParcelReturnRequest(
        List<DocumentDataResponse> documentDataResponses) {
        return documentDataResponses.stream()
            .map(documentDataResponse -> ParcelReturnRequest.builder()
                .methodProperties(ParcelReturnMethodProperties.builder()
                    .intDocNumber(documentDataResponse.getNumber())
                    .buildingNumber()
                    .recipientSettlement()
                    .recipientSettlementStreet()
                    .noteAddressRecipient()
                    .reason("Сплив час максимального зберігання")
                    .subtypeReason()
                    .note("Автоматичне повернення товару")
                    .build())
                .build())
            .toList();
    }
}
