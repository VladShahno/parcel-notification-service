package ua.com.hookahcat.service.impl;

import static net.logstash.logback.argument.StructuredArguments.kv;
import static ua.com.hookahcat.util.Constants.CalledMethods.CHECK_POSSIBILITY_CREATE_RETURN;
import static ua.com.hookahcat.util.Constants.CalledMethods.GET_DOCUMENT_LIST;
import static ua.com.hookahcat.util.Constants.CalledMethods.GET_STATUS_DOCUMENTS;
import static ua.com.hookahcat.util.Constants.CalledMethods.SAVE;
import static ua.com.hookahcat.util.Constants.DATE_TIME_FROM;
import static ua.com.hookahcat.util.Constants.DATE_TIME_TO;
import static ua.com.hookahcat.util.Constants.DOCUMENT_NUMBER;
import static ua.com.hookahcat.util.Constants.MAX_STORAGE_DAYS;
import static ua.com.hookahcat.util.Constants.ModelsNames.ADDITIONAL_SERVICE;
import static ua.com.hookahcat.util.Constants.ModelsNames.INTERNET_DOCUMENT;
import static ua.com.hookahcat.util.Constants.ModelsNames.TRACKING_DOCUMENT;
import static ua.com.hookahcat.util.Constants.ONE;
import static ua.com.hookahcat.util.Constants.ORDER_TYPE_CARGO_RETURN;
import static ua.com.hookahcat.util.Constants.PAGE;
import static ua.com.hookahcat.util.Constants.PAYMENT_METHOD_CASH;
import static ua.com.hookahcat.util.Constants.PHONE_NUMBER;
import static ua.com.hookahcat.util.Constants.Patterns.DATE_PATTERN;
import static ua.com.hookahcat.util.Constants.Patterns.DATE_TIME_PATTERN;
import static ua.com.hookahcat.util.Constants.RETURN_ADDRESS_REF;
import static ua.com.hookahcat.util.Constants.STATUS;
import static ua.com.hookahcat.util.Constants.StateNames.ARRIVED;
import static ua.com.hookahcat.util.Constants.ZERO;

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
import ua.com.hookahcat.configuration.CsvProperties;
import ua.com.hookahcat.configuration.NovaPoshtaApiProperties;
import ua.com.hookahcat.csvsdk.service.CsvService;
import ua.com.hookahcat.model.request.properties.CheckPossibilityCreateReturnProperties;
import ua.com.hookahcat.model.request.CheckPossibilityCreateReturnRequest;
import ua.com.hookahcat.model.request.properties.DocumentListMethodProperties;
import ua.com.hookahcat.model.request.DocumentListRequest;
import ua.com.hookahcat.model.request.DocumentsStatusRequest;
import ua.com.hookahcat.model.request.properties.ParcelReturnToAddressMethodProperties;
import ua.com.hookahcat.model.request.ParcelReturnToAddressRequest;
import ua.com.hookahcat.model.request.properties.ParcelReturnToWarehouseProperties;
import ua.com.hookahcat.model.request.ParcelReturnToWarehouseRequest;
import ua.com.hookahcat.model.request.properties.TrackingDocument;
import ua.com.hookahcat.model.request.properties.TrackingDocumentMethodProperties;
import ua.com.hookahcat.model.response.CheckPossibilityCreateReturnResponse;
import ua.com.hookahcat.model.response.data.DocumentDataResponse;
import ua.com.hookahcat.model.response.data.DocumentListDataResponse;
import ua.com.hookahcat.model.response.DocumentListResponse;
import ua.com.hookahcat.model.response.DocumentsStatusResponse;
import ua.com.hookahcat.model.response.ParcelReturnResponse;
import ua.com.hookahcat.service.NewPostServiceProxy;
import ua.com.hookahcat.service.ProxyService;

@Service
@Slf4j
public class NewPostServiceProxyImpl extends ProxyService implements NewPostServiceProxy {

    public NewPostServiceProxyImpl(NovaPoshtaApiProperties novaPoshtaApiProperties,
        WebClient webClient, CsvProperties csvProperties, CsvService csvService) {
        super(webClient);
        this.novaPoshtaApiProperties = novaPoshtaApiProperties;
        this.csvProperties = csvProperties;
        this.csvService = csvService;
    }

    private final NovaPoshtaApiProperties novaPoshtaApiProperties;
    private final CsvProperties csvProperties;
    private final CsvService csvService;


    @Override
    public DocumentsStatusResponse getDocumentsStatus(DocumentsStatusRequest statusRequest) {
        statusRequest.setCalledMethod(GET_STATUS_DOCUMENTS);
        statusRequest.setModelName(TRACKING_DOCUMENT);

        log.info("Getting document status for {}",
            kv(DOCUMENT_NUMBER, statusRequest.getMethodProperties().getDocuments().stream()
                .map(TrackingDocument::getDocumentNumber)
                .toList()));

        return post(novaPoshtaApiProperties.getBaseUrl(), Map.of(), statusRequest,
            DocumentsStatusResponse.class);
    }

    @Override
    public DocumentListResponse getDocumentList(DocumentListRequest documentListRequest) {
        documentListRequest.setCalledMethod(GET_DOCUMENT_LIST);
        documentListRequest.setModelName(INTERNET_DOCUMENT);

        log.info("Getting document list from {} to {} from {}",
            kv(DATE_TIME_FROM, documentListRequest.getMethodProperties().getDateTimeFrom()),
            kv(DATE_TIME_TO, documentListRequest.getMethodProperties().getDateTimeTo()),
            kv(PAGE, documentListRequest.getMethodProperties().getPage()));

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
        String documentNumber, String returnAddressRef) {
        log.info("Creating parcel return order to warehouse for {} and {}",
            kv(DOCUMENT_NUMBER, documentNumber), kv(RETURN_ADDRESS_REF, returnAddressRef));

        return post(novaPoshtaApiProperties.getBaseUrl(), Map.of(),
            createParcelReturnToWarehouseRequest(apiKey, documentNumber, returnAddressRef),
            ParcelReturnResponse.class);
    }

    @Override
    public byte[] getUnReceivedParcelsCsv(String maxStorageDays) {
        var unreceivedParcelsData = getUnreceivedParcelsByMaxStorageDays(maxStorageDays);

        if (CollectionUtils.isNotEmpty(unreceivedParcelsData)) {
            return csvService.exportData(unreceivedParcelsData,
                csvProperties.getResponseHeaders(), csvProperties.getResponseFields());
        }
        return new byte[0];
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
        String documentNumber, String returnAddressRef) {
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
                .returnAddressRef(returnAddressRef)
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

    private List<DocumentDataResponse> getUnreceivedParcelsByMaxStorageDays(String maxStorageDays) {
        var unreceivedParcelsData = getUnreceivedParcels(novaPoshtaApiProperties.getApiKey(),
            novaPoshtaApiProperties.getSenderPhoneNumber(), maxStorageDays);
        log.info("Found {} unreceived parcels", unreceivedParcelsData.size());
        return unreceivedParcelsData;
    }
}
