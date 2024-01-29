package ua.com.hookahcat.service.scheduler;

import static net.logstash.logback.argument.StructuredArguments.kv;
import static ua.com.hookahcat.util.Constants.DOCUMENT_NUMBER;
import static ua.com.hookahcat.util.Constants.FileNames.NOT_RECEIVED_PARCELS;
import static ua.com.hookahcat.util.Constants.RETURN_ADDRESS_REF;
import static ua.com.hookahcat.util.NewPostUtils.createCsvFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ua.com.hookahcat.configuration.NovaPoshtaApiProperties;
import ua.com.hookahcat.model.response.data.DocumentDataResponse;
import ua.com.hookahcat.notification.configuration.EmailNotificationProperties;
import ua.com.hookahcat.notification.model.EmailNotificationData;
import ua.com.hookahcat.notification.service.EmailNotificationService;
import ua.com.hookahcat.service.NewPostServiceProxy;
import ua.com.hookahcat.telegram.bot.service.HookahCatTelegramBotService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParcelSearchingJob {

    private final EmailNotificationService emailNotificationService;
    private final NovaPoshtaApiProperties novaPoshtaApiProperties;
    private final EmailNotificationProperties emailNotificationProperties;
    private final NewPostServiceProxy newPostServiceProxy;
    private final HookahCatTelegramBotService hookahCatTelegramBotService;

    @Value("${scheduled.max-storage-days-before-return-order}")
    private String maxStorageDaysBeforeReturnOrder;

    @Value("${scheduled.max-storage-days-before-notification}")
    private String maxStorageDaysBeforeNotification;

    @Scheduled(cron = "${scheduled.parcel-search-job}")
    public void searchUnReceivedParcels() {
        log.info("Start scheduler for searching not received parcels...");

        var exportedParcelsData = newPostServiceProxy.getUnReceivedParcelsCsv(
            maxStorageDaysBeforeNotification);
        sendEmailWithNotReceivedParcelsData(exportedParcelsData,
            emailNotificationProperties.getNotReceivedParcelsSubject(),
            emailNotificationProperties.getNotReceivedParcelsMessage());
    }

    @Scheduled(cron = "${scheduled.parcel-return-order-job}")
    public void createParcelReturnOrderToWarehouse() {
        log.info("Start scheduler for creating parcel return order to warehouse...");

        var unreceivedParcelsData = getUnreceivedParcelsByMaxStorageDays(
            maxStorageDaysBeforeReturnOrder);
        var apiKey = novaPoshtaApiProperties.getApiKey();

        if (CollectionUtils.isNotEmpty(unreceivedParcelsData)) {
            List<String> documentNumbers = new ArrayList<>();

            unreceivedParcelsData.parallelStream()
                .forEach(documentDataResponse -> {
                    var documentNumber = documentDataResponse.getNumber();
                    var checkPossibilityCreateReturnResponse = newPostServiceProxy.checkPossibilityCreateReturnOrder(
                        apiKey, documentNumber);
                    if (checkPossibilityCreateReturnResponse.isSuccess()) {
                        var returnAddressRef = checkPossibilityCreateReturnResponse.getData().get(0)
                            .getRef();
                        var isSuccess = newPostServiceProxy.createParcelReturnOrderToWarehouse(
                            apiKey, documentNumber, returnAddressRef).isSuccess();

                        if (isSuccess) {
                            documentNumbers.add(documentNumber);
                        }

                        log.info("Return order to warehouse successfully created for {} and {}",
                            kv(DOCUMENT_NUMBER, documentNumber),
                            kv(RETURN_ADDRESS_REF, returnAddressRef));
                    }
                });
            sendEmailForCreatedReturnParcelsRequest(documentNumbers);
            hookahCatTelegramBotService.sendMessageWithNotReceivedParcelsNumbers(documentNumbers);
        } else {
            log.info("Parcels that can be returned not found");
        }
    }

    private void sendEmailForCreatedReturnParcelsRequest(List<String> documentNumbers) {
        if (CollectionUtils.isNotEmpty(documentNumbers)) {
            emailNotificationService.sendEmailNotification(prepareEmailNotificationData(null, null,
                emailNotificationProperties.getReturnedParcelsSubject(),
                emailNotificationProperties.getReturnedParcelsMessage() + documentNumbers));
        }
    }

    private void sendEmailWithNotReceivedParcelsData(byte[] exportedParcelsData, String subject,
        String message) {
        if (Objects.nonNull(exportedParcelsData) && exportedParcelsData.length > 0) {
            emailNotificationService.sendEmailNotification(
                prepareEmailNotificationData(exportedParcelsData, NOT_RECEIVED_PARCELS, subject,
                    message));
        }
    }

    private EmailNotificationData prepareEmailNotificationData(byte[] exportedParcelsData,
        String fileName, String subject, String message) {
        return EmailNotificationData.builder()
            .recipient(emailNotificationProperties.getRecipient())
            .sender(emailNotificationProperties.getSender())
            .subject(subject)
            .message(message)
            .file(createCsvFile(exportedParcelsData, fileName))
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
