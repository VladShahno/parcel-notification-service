package ua.com.hookahcat.service.impl;

import static ua.com.hookahcat.common.Constants.CalledMethods.GET_DOCUMENT_LIST;
import static ua.com.hookahcat.common.Constants.CalledMethods.GET_STATUS_DOCUMENTS;
import static ua.com.hookahcat.common.Constants.MAX_STORAGE_DAYS;
import static ua.com.hookahcat.common.Constants.ModelsNames.INTERNET_DOCUMENT;
import static ua.com.hookahcat.common.Constants.ModelsNames.TRACKING_DOCUMENT;
import static ua.com.hookahcat.common.Constants.ONE;
import static ua.com.hookahcat.common.Constants.Patterns.DATE_PATTERN;
import static ua.com.hookahcat.common.Constants.Patterns.DATE_TIME_PATTERN;
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
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ua.com.hookahcat.configuration.NovaPoshtaApiProperties;
import ua.com.hookahcat.model.request.DocumentListMethodProperties;
import ua.com.hookahcat.model.request.DocumentListRequest;
import ua.com.hookahcat.model.request.DocumentsStatusRequest;
import ua.com.hookahcat.model.request.TrackingDocument;
import ua.com.hookahcat.model.request.TrackingDocumentMethodProperties;
import ua.com.hookahcat.model.response.DocumentDataResponse;
import ua.com.hookahcat.model.response.DocumentListDataResponse;
import ua.com.hookahcat.model.response.DocumentListResponse;
import ua.com.hookahcat.model.response.DocumentsStatusResponse;
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

        return post(novaPoshtaApiProperties.getBaseUrl(), Map.of(), statusRequest,
            DocumentsStatusResponse.class);
    }

    @Override
    public DocumentListResponse getDocumentList(DocumentListRequest documentListRequest) {
        documentListRequest.setCalledMethod(GET_DOCUMENT_LIST);
        documentListRequest.setModelName(INTERNET_DOCUMENT);

        return post(novaPoshtaApiProperties.getBaseUrl(), Map.of(), documentListRequest,
            DocumentListResponse.class);
    }

    @Override
    public List<DocumentListDataResponse> getArrivedParcelsForLastMonth(String apiKey) {
        var documentList = collectAllDataForLastMonth(apiKey);

        return filterNotReceivedParcels(documentList);
    }

    @Override
    public List<DocumentDataResponse> getUnreceivedParcels(String apiKey,
        String senderPhoneNumber) {
        var arrivedParcelsForLastMonth = getArrivedParcelsForLastMonth(apiKey);

        return filterUnreceivedParcels(arrivedParcelsForLastMonth, senderPhoneNumber);
    }

    @Override
    protected void buildHeadersForRequest(HttpHeaders httpHeaders) {
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

        List<DocumentListDataResponse> documentListRequests = new ArrayList<>();

        while (true) {
            documentRequest.getMethodProperties().setPage(String.valueOf(page));

            var response = getDocumentList(documentRequest);

            page++;

            if (response.getData().isEmpty()) {
                break;
            }
            documentListRequests.addAll(response.getData());
        }
        return documentListRequests;
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
        String senderPhoneNumber) {
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

        return filterParcelsByMaxStorageDays(fullParcelsData.getData());
    }

    private List<DocumentDataResponse> filterParcelsByMaxStorageDays(
        List<DocumentDataResponse> fullParcelsData) {
        var todayDate = LocalDate.now();

        return fullParcelsData.stream()
            .filter(documentDataResponse -> {
                var deliveryDate = documentDataResponse.getActualDeliveryDate();
                if (Objects.nonNull(deliveryDate)) {
                    var actualDeliveryDateTime = LocalDateTime.parse(deliveryDate,
                        DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
                    var actualDeliveryDateFormatted = actualDeliveryDateTime.toLocalDate()
                        .format(DateTimeFormatter.ofPattern(DATE_PATTERN));

                    return todayDate.minusDays(MAX_STORAGE_DAYS)
                        .isAfter(LocalDate.parse(actualDeliveryDateFormatted, DateTimeFormatter.ofPattern(DATE_PATTERN)));
                }
                return false;
            })
            .toList();
    }
}
