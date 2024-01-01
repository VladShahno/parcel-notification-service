package hookahcat.com.ua.service.impl;

import static hookahcat.com.ua.common.Constants.CalledMethods.GET_DOCUMENT_LIST;
import static hookahcat.com.ua.common.Constants.CalledMethods.GET_STATUS_DOCUMENTS;
import static hookahcat.com.ua.common.Constants.ModelsNames.INTERNET_DOCUMENT;
import static hookahcat.com.ua.common.Constants.ModelsNames.TRACKING_DOCUMENT;
import static hookahcat.com.ua.common.Constants.ONE;
import static hookahcat.com.ua.common.Constants.Patterns.DATE_TIME_PATTERN;
import static hookahcat.com.ua.common.Constants.StateNames.ARRIVED;
import static hookahcat.com.ua.common.Constants.StateNames.ARRIVED_PARCEL_LOCKER;
import static hookahcat.com.ua.common.Constants.ZERO;

import hookahcat.com.ua.configuration.NovaPoshtaApiProperties;
import hookahcat.com.ua.model.request.DocumentListMethodProperties;
import hookahcat.com.ua.model.request.DocumentListRequest;
import hookahcat.com.ua.model.request.DocumentsStatusRequest;
import hookahcat.com.ua.model.response.DocumentListDataResponse;
import hookahcat.com.ua.model.response.DocumentListResponse;
import hookahcat.com.ua.model.response.DocumentsStatusResponse;
import hookahcat.com.ua.service.NewPostServiceProxy;
import hookahcat.com.ua.service.ProxyService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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
    protected void buildHeadersForRequest(HttpHeaders httpHeaders) {
    }

    private List<DocumentListDataResponse> collectAllDataForLastMonth(String apiKey) {
        int page = 1;

        var todayLocalDate = LocalDate.now();
        var todayFormatted = todayLocalDate.format(
            DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
        var lastMonthFormatted = todayLocalDate.minusMonths(Integer.parseInt(ONE))
            .format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));

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
            .calledMethod(GET_DOCUMENT_LIST)
            .modelName(INTERNET_DOCUMENT)
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
        return ARRIVED.equalsIgnoreCase(stateName) || ARRIVED_PARCEL_LOCKER.equalsIgnoreCase(
            stateName);
    }
}
