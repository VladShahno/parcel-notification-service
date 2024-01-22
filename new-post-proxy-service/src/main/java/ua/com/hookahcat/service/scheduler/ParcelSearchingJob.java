package ua.com.hookahcat.service.scheduler;

import static net.logstash.logback.argument.StructuredArguments.kv;
import static ua.com.hookahcat.common.Constants.DOCUMENT_NUMBER;
import static ua.com.hookahcat.common.Constants.Patterns.DATE_PATTERN;
import static ua.com.hookahcat.common.Constants.RECIPIENT_WAREHOUSE;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ua.com.hookahcat.configuration.CsvProperties;
import ua.com.hookahcat.configuration.NovaPoshtaApiProperties;
import ua.com.hookahcat.csvsdk.service.CsvService;
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

    @Value("${scheduled.max-storage-days-before-return-order}")
    private String maxStorageDaysBeforeReturnOrder;

    @Value("${scheduled.max-storage-days-before-notification}")
    private String maxStorageDaysBeforeNotification;

    @Scheduled(cron = "${scheduled.parcel-search-job}")
    public void searchUnReceivedParcels() {
        log.info("Start scheduler for searching not received parcels...");
        var exportedParcelsData = getUnReceivedParcelsCsv(maxStorageDaysBeforeNotification);
        sendEmailWithNotReceivedParcels(exportedParcelsData);
    }

    @Scheduled(cron = "${scheduled.parcel-return-order-job}")
    public void createParcelReturnOrderToWarehouse() {
        log.info("Start scheduler for creating parcel return order to warehouse...");

        var unreceivedParcelsData = getUnreceivedParcelsByMaxStorageDays(
            maxStorageDaysBeforeReturnOrder);
        var apiKey = novaPoshtaApiProperties.getApiKey();

        if (CollectionUtils.isNotEmpty(unreceivedParcelsData)) {
            unreceivedParcelsData.parallelStream()
                .forEach(documentDataResponse -> {
                    var documentNumber = documentDataResponse.getNumber();
                    var recipientWarehouse = newPostServiceProxy.checkPossibilityCreateReturnOrder(
                        apiKey, documentNumber);
                    if (recipientWarehouse.isSuccess()) {
                        var warehouseRef = recipientWarehouse.getData().get(0).getRef();
                        newPostServiceProxy.createParcelReturnOrderToWarehouse(apiKey,
                            documentNumber,
                            warehouseRef);
                        log.info("Return order to warehouse successfully created for {} and {}",
                            kv(DOCUMENT_NUMBER, documentNumber),
                            kv(RECIPIENT_WAREHOUSE, warehouseRef));
                    }
                });
        }
        log.info("Parcels that can be returned not found");
    }


    public byte[] getUnReceivedParcelsCsv(String maxStorageDays) {
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

    private List<DocumentDataResponse> getUnreceivedParcelsByMaxStorageDays(String maxStorageDays) {
        var unreceivedParcelsData = newPostServiceProxy.getUnreceivedParcels(
            novaPoshtaApiProperties.getApiKey(),
            novaPoshtaApiProperties.getSenderPhoneNumber(), maxStorageDays);
        log.info("Found {} unreceived parcels", unreceivedParcelsData.size());
        return unreceivedParcelsData;
    }
}
