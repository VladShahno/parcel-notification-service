package ua.com.hookahcat.service.impl;

import static net.logstash.logback.argument.StructuredArguments.kv;
import static ua.com.hookahcat.common.Constants.CalledMethods.CHECK_POSSIBILITY_CREATE_RETURN;
import static ua.com.hookahcat.common.Constants.CalledMethods.GET_DOCUMENT_LIST;
import static ua.com.hookahcat.common.Constants.CalledMethods.GET_STATUS_DOCUMENTS;
import static ua.com.hookahcat.common.Constants.CalledMethods.SAVE;
import static ua.com.hookahcat.common.Constants.DATE_TIME_FROM;
import static ua.com.hookahcat.common.Constants.DATE_TIME_TO;
import static ua.com.hookahcat.common.Constants.DOCUMENT_NUMBER;
import static ua.com.hookahcat.common.Constants.MAX_STORAGE_DAYS;
import static ua.com.hookahcat.common.Constants.ModelsNames.ADDITIONAL_SERVICE;
import static ua.com.hookahcat.common.Constants.ModelsNames.INTERNET_DOCUMENT;
import static ua.com.hookahcat.common.Constants.ModelsNames.TRACKING_DOCUMENT;
import static ua.com.hookahcat.common.Constants.ONE;
import static ua.com.hookahcat.common.Constants.ORDER_TYPE_CARGO_RETURN;
import static ua.com.hookahcat.common.Constants.PAYMENT_METHOD_CASH;
import static ua.com.hookahcat.common.Constants.PHONE_NUMBER;
import static ua.com.hookahcat.common.Constants.Patterns.DATE_PATTERN;
import static ua.com.hookahcat.common.Constants.Patterns.DATE_TIME_PATTERN;
import static ua.com.hookahcat.common.Constants.RECIPIENT_WAREHOUSE;
import static ua.com.hookahcat.common.Constants.STATUS;
import static ua.com.hookahcat.common.Constants.StateNames.ARRIVED;
import static ua.com.hookahcat.common.Constants.ZERO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ua.com.hookahcat.configuration.NovaPoshtaApiProperties;
import ua.com.hookahcat.model.request.CheckPossibilityCreateReturnProperties;
import ua.com.hookahcat.model.request.CheckPossibilityCreateReturnRequest;
import ua.com.hookahcat.model.request.DocumentListMethodProperties;
import ua.com.hookahcat.model.request.DocumentListRequest;
import ua.com.hookahcat.model.request.DocumentsStatusRequest;
import ua.com.hookahcat.model.request.ParcelReturnToAddressMethodProperties;
import ua.com.hookahcat.model.request.ParcelReturnToAddressRequest;
import ua.com.hookahcat.model.request.ParcelReturnToWarehouseProperties;
import ua.com.hookahcat.model.request.ParcelReturnToWarehouseRequest;
import ua.com.hookahcat.model.request.TrackingDocument;
import ua.com.hookahcat.model.request.TrackingDocumentMethodProperties;
import ua.com.hookahcat.model.response.CheckPossibilityCreateReturnResponse;
import ua.com.hookahcat.model.response.DocumentDataResponse;
import ua.com.hookahcat.model.response.DocumentListDataResponse;
import ua.com.hookahcat.model.response.DocumentListResponse;
import ua.com.hookahcat.model.response.DocumentsStatusResponse;
import ua.com.hookahcat.model.response.ParcelReturnResponse;
import ua.com.hookahcat.service.NewPostServiceProxy;
import ua.com.hookahcat.service.ProxyService;

@Service
@Slf4j
public class NewPostServiceProxyImpl extends ProxyService implements NewPostServiceProxy {

    public NewPostServiceProxyImpl(NovaPoshtaApiProperties novaPoshtaApiProperties,
        WebClient webClient) {
        super(webClient);
        this.novaPoshtaApiProperties = novaPoshtaApiProperties;
    }

    private final NovaPoshtaApiProperties novaPoshtaApiProperties;

    @Override
    public DocumentsStatusResponse getDocumentsStatus(DocumentsStatusRequest statusRequest) {
        statusRequest.setCalledMethod(GET_STATUS_DOCUMENTS);
        statusRequest.setModelName(TRACKING_DOCUMENT);

        log.info("Getting document status for {}",
            kv(DOCUMENT_NUMBER, statusRequest.getMethodProperties().getDocuments()));

        return post(novaPoshtaApiProperties.getBaseUrl(), Map.of(), statusRequest,
            DocumentsStatusResponse.class);
    }

    @Override
    public DocumentListResponse getDocumentList(DocumentListRequest documentListRequest) {
        documentListRequest.setCalledMethod(GET_DOCUMENT_LIST);
        documentListRequest.setModelName(INTERNET_DOCUMENT);

        log.info("Getting document list from {} to {}",
            kv(DATE_TIME_FROM, documentListRequest.getMethodProperties().getDateTimeFrom()),
            kv(DATE_TIME_TO, documentListRequest.getMethodProperties().getDateTimeTo()));

        return post(novaPoshtaApiProperties.getBaseUrl(), Map.of(), documentListRequest,
            DocumentListResponse.class);
    }

    @Override
    public List<DocumentListDataResponse> getArrivedParcelsForLastMonth(String apiKey) {
        var documentList = collectAllDataForLastMonth(apiKey);

        log.info("Getting document list for last month with {}", kv(STATUS, ARRIVED));

        return filterNotReceivedParcels(documentList);
    }

    @Override
    public List<DocumentDataResponse> getUnreceivedParcels(String apiKey,
        String senderPhoneNumber, String maxStorageDays) {
        var arrivedParcelsForLastMonth = getArrivedParcelsForLastMonth(apiKey);

        log.info("Getting unreceived parcels for {} with {}", kv(PHONE_NUMBER, senderPhoneNumber),
            kv(MAX_STORAGE_DAYS, maxStorageDays));

        return filterUnreceivedParcels(arrivedParcelsForLastMonth, senderPhoneNumber,
            maxStorageDays);
    }

    @Override
    public ParcelReturnResponse createParcelReturnOrderToAddress(String apiKey,
        String documentNumber) {
        log.info("Creation return order for {} to a new address",
            kv(DOCUMENT_NUMBER, documentNumber));

        return post(novaPoshtaApiProperties.getBaseUrl(), Map.of(),
            createParcelReturnToAddressRequest(apiKey, documentNumber), ParcelReturnResponse.class);
    }

    @Override
    public ParcelReturnResponse createParcelReturnOrderToWarehouse(String apiKey,
        String documentNumber, String recipientWarehouse) {
        log.info("Creating parcel return order to warehouse for {} and {}",
            kv(DOCUMENT_NUMBER, documentNumber), kv(RECIPIENT_WAREHOUSE, recipientWarehouse));

        return post(novaPoshtaApiProperties.getBaseUrl(), Map.of(),
            createParcelReturnToWarehouseRequest(apiKey, documentNumber, recipientWarehouse),
            ParcelReturnResponse.class);
    }

    @Override
    public CheckPossibilityCreateReturnResponse checkPossibilityCreateReturnOrder(String apiKey,
        String documentNumber) {
        var returnRequest = CheckPossibilityCreateReturnRequest.builder()
            .apiKey(apiKey)
            .modelName(ADDITIONAL_SERVICE)
            .calledMethod(CHECK_POSSIBILITY_CREATE_RETURN)
            .methodProperties(CheckPossibilityCreateReturnProperties.builder()
                .number(documentNumber)
                .build())
            .build();

        log.info("Checking possibility to return {}", kv(DOCUMENT_NUMBER, documentNumber));

        return post(novaPoshtaApiProperties.getBaseUrl(), Map.of(), returnRequest,
            CheckPossibilityCreateReturnResponse.class);
    }

    private List<DocumentListDataResponse> collectAllDataForLastMonth(String apiKey) {
        int page = 1;

        var todayLocalDate = LocalDate.now();
        var todayFormatted = todayLocalDate.format(
            DateTimeFormatter.ofPattern(DATE_PATTERN));
        var lastMonthFormatted = todayLocalDate.minusMonths(Integer.parseInt(ONE))
            .format(DateTimeFormatter.ofPattern(DATE_PATTERN));

        var documentRequest = generateDocumentListRequest(apiKey, lastMonthFormatted,
            todayFormatted);

        List<DocumentListDataResponse> documentListDataResponses = new ArrayList<>();

        while (true) {
            documentRequest.getMethodProperties().setPage(String.valueOf(page));

            var response = getDocumentList(documentRequest);

            page++;

            if (response.getData().isEmpty()) {
                break;
            }
            documentListDataResponses.addAll(response.getData());
        }
        return documentListDataResponses;
    }

    private DocumentListRequest generateDocumentListRequest(String apiKey, String dateFrom,
        String dateTo) {
        return DocumentListRequest.builder()
            .apiKey(apiKey)
            .methodProperties(DocumentListMethodProperties.builder()
                .getFullList(ZERO)
                .dateTimeFrom(dateFrom)
                .dateTimeTo(dateTo)
                .page(ONE)
                .build())
            .build();
    }

    private List<DocumentListDataResponse> filterNotReceivedParcels(
        List<DocumentListDataResponse> documentListDataResponses) {
        return documentListDataResponses.stream()
            .filter(this::isArrived)
            .toList();
    }

    private boolean isArrived(DocumentListDataResponse response) {
        var stateName = response.getStateName();
        return ARRIVED.equalsIgnoreCase(stateName);
    }

    private List<DocumentDataResponse> filterUnreceivedParcels(
        List<DocumentListDataResponse> arrivedParcelsData,
        String senderPhoneNumber,
        String maxStorageDays) {
        if (CollectionUtils.isEmpty(arrivedParcelsData)) {
            return Collections.emptyList();
        }

        var methodProperties = TrackingDocumentMethodProperties.builder()
            .documents(arrivedParcelsData.stream()
                .map(documentListDataResponse -> TrackingDocument.builder()
                    .documentNumber(documentListDataResponse.getIntDocNumber())
                    .phone(senderPhoneNumber)
                    .build())
                .toList())
            .build();

        var documentStatusRequest = DocumentsStatusRequest.builder()
            .methodProperties(methodProperties)
            .build();

        // TODO novaposhta allows to view up to 100 elements at the same time, need to implement cyclic request processing in the future
        var fullParcelsData = getDocumentsStatus(documentStatusRequest);

        return filterParcelsByMaxStorageDays(fullParcelsData.getData(), maxStorageDays);
    }

    private List<DocumentDataResponse> filterParcelsByMaxStorageDays(
        List<DocumentDataResponse> fullParcelsData, String maxStorageDays) {
        var todayDate = LocalDate.now();

        return fullParcelsData.stream()
            .filter(documentDataResponse -> {
                var deliveryDate = documentDataResponse.getActualDeliveryDate();
                if (Objects.nonNull(deliveryDate)) {
                    var actualDeliveryDateTime = LocalDateTime.parse(deliveryDate,
                        DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
                    var actualDeliveryDateFormatted = actualDeliveryDateTime.toLocalDate()
                        .format(DateTimeFormatter.ofPattern(DATE_PATTERN));

                    return todayDate.minusDays(Long.parseLong(maxStorageDays))
                        .isAfter(LocalDate.parse(actualDeliveryDateFormatted,
                            DateTimeFormatter.ofPattern(DATE_PATTERN)));
                }
                return false;
            })
            .toList();
    }

    private ParcelReturnToWarehouseRequest createParcelReturnToWarehouseRequest(String apiKey,
        String documentNumber, String recipientWarehouse) {
        var returnOrderProperties = novaPoshtaApiProperties.getReturnOrder();

        return ParcelReturnToWarehouseRequest.builder()
            .apiKey(apiKey)
            .modelName(ADDITIONAL_SERVICE)
            .calledMethod(SAVE)
            .methodProperties(ParcelReturnToWarehouseProperties.builder()
                .intDocNumber(documentNumber)
                .paymentMethod(PAYMENT_METHOD_CASH)
                .reason(returnOrderProperties.getReturnReason())
                .subtypeReason(returnOrderProperties.getReturnSubtypeReason())
                .orderType(ORDER_TYPE_CARGO_RETURN)
                .recipientWarehouse(recipientWarehouse)
                .build())
            .build();
    }

    private ParcelReturnToAddressRequest createParcelReturnToAddressRequest(String apiKey,
        String documentNumber) {
        var returnOrderProperties = novaPoshtaApiProperties.getReturnOrder();

        return ParcelReturnToAddressRequest.builder()
            .apiKey(apiKey)
            .modelName(ADDITIONAL_SERVICE)
            .calledMethod(SAVE)
            .methodProperties(ParcelReturnToAddressMethodProperties.builder()
                .intDocNumber(documentNumber)
                .buildingNumber(returnOrderProperties.getBuildingNumber())
                .recipientSettlement(returnOrderProperties.getRecipientSettlement())
                .recipientSettlementStreet(returnOrderProperties.getRecipientSettlementStreet())
                .reason(returnOrderProperties.getReturnReason())
                .subtypeReason(returnOrderProperties.getReturnSubtypeReason())
                .orderType(ORDER_TYPE_CARGO_RETURN)
                .paymentMethod(PAYMENT_METHOD_CASH)
                .build())
            .build();
    }
}
