package ua.com.hookahcat.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ua.com.hookahcat.common.Constants.CalledMethods.CHECK_POSSIBILITY_CREATE_RETURN;
import static ua.com.hookahcat.common.Constants.CalledMethods.GET_DOCUMENT_LIST;
import static ua.com.hookahcat.common.Constants.CalledMethods.GET_STATUS_DOCUMENTS;
import static ua.com.hookahcat.common.Constants.CalledMethods.SAVE;
import static ua.com.hookahcat.common.Constants.ModelsNames.ADDITIONAL_SERVICE;
import static ua.com.hookahcat.common.Constants.ModelsNames.INTERNET_DOCUMENT;
import static ua.com.hookahcat.common.Constants.ModelsNames.TRACKING_DOCUMENT;
import static ua.com.hookahcat.common.Constants.ONE;
import static ua.com.hookahcat.common.Constants.ORDER_TYPE_CARGO_RETURN;
import static ua.com.hookahcat.common.Constants.PAYMENT_METHOD_CASH;
import static ua.com.hookahcat.common.Constants.Patterns.DATE_PATTERN;
import static ua.com.hookahcat.common.Constants.StateNames.ARRIVED;
import static ua.com.hookahcat.common.Constants.ZERO;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import ua.com.hookahcat.configuration.NovaPoshtaApiProperties;
import ua.com.hookahcat.configuration.NovaPoshtaApiProperties.ReturnOrder;
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
import ua.com.hookahcat.service.impl.NewPostServiceProxyImpl;

@ExtendWith(SpringExtension.class)
public class NewPostServiceProxyImplTest {

    private WireMockServer wireMockServer;

    @LocalServerPort
    private String port;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private NewPostServiceProxyImpl newPostServiceProxy;

    private static final String MOCK_API_KEY = "mockApiKey";
    private static final String DOCUMENT_NUMBER = "documentNumber";
    private static final String SUB_TYPE_RETURN_REASON = "subTypeReturnReason";
    private static final String RETURN_REASON = "returnReason";
    private static final String SENDER_PHONE_NUMBER = "phoneNum";
    private static final String BUILDING_NUMBER = "1";
    private static final String RECIPIENT_SETTLEMENT = "recipientSettlement";
    private static final String RECIPIENT_SETTLEMENT_STREET = "recipientSettlementStreet";

    @BeforeEach
    public void setup() {
        wireMockServer = new WireMockServer();
        wireMockServer.start();

        var novaPoshtaApiProperties = new NovaPoshtaApiProperties();
        novaPoshtaApiProperties.setBaseUrl("http://localhost:" + wireMockServer.port());
        var returnOrder = new ReturnOrder();
        returnOrder.setReturnReason(RETURN_REASON);
        returnOrder.setReturnSubtypeReason(SUB_TYPE_RETURN_REASON);
        returnOrder.setBuildingNumber(BUILDING_NUMBER);
        returnOrder.setRecipientSettlement(RECIPIENT_SETTLEMENT);
        returnOrder.setRecipientSettlementStreet(RECIPIENT_SETTLEMENT_STREET);
        novaPoshtaApiProperties.setApiKey(MOCK_API_KEY);
        novaPoshtaApiProperties.setSenderPhoneNumber(SENDER_PHONE_NUMBER);
        novaPoshtaApiProperties.setReturnOrder(returnOrder);

        var webClient = WebClient.builder()
            .baseUrl(novaPoshtaApiProperties.getBaseUrl())
            .build();

        newPostServiceProxy = new NewPostServiceProxyImpl(novaPoshtaApiProperties, webClient);
    }

    @AfterEach
    public void teardown() {
        wireMockServer.stop();
    }

    @Test
    public void testGetDocumentsStatus() throws JsonProcessingException {
        var documentsStatusRequest = stubDocumentsStatusRequest();
        stubRequestToNovaPoshta(documentsStatusRequest, "valid_document_status_response.json");

        var response = newPostServiceProxy.getDocumentsStatus(documentsStatusRequest);

        assertNotNull(response);
        assertEquals("20450854620708", response.getData().get(0).getNumber());
        assertEquals("2024-01-12 17:12:12", response.getData().get(0).getActualDeliveryDate());
        assertEquals("Прибув у відділення", response.getData().get(0).getStatus());
    }

    @Test
    public void testGetDocumentList() throws JsonProcessingException {
        var documentListRequest = stubDocumentListRequest();
        stubRequestToNovaPoshta(documentListRequest, "valid_document_list_response.json");

        var response = newPostServiceProxy.getDocumentList(documentListRequest);

        assertNotNull(response);
        assertEquals("20450854620708", response.getData().get(0).getIntDocNumber());
        assertEquals("c8c0ae72-b7af-11ee-a60f-48df37b921db", response.getData().get(0).getRef());
        assertEquals(2, response.getData().size());
        assertEquals("Прибув у відділення", response.getData().get(0).getStateName());
    }

    @Test
    public void testGetArrivedParcelsForLastMonth() throws JsonProcessingException {
        var documentListRequest = stubDocumentListRequest();
        stubRequestToNovaPoshta(documentListRequest, "valid_document_list_response.json");
        documentListRequest.getMethodProperties().setPage("2");
        stubRequestToNovaPoshta(documentListRequest, "valid_document_list_empty_response.json");

        var response = newPostServiceProxy.getArrivedParcelsForLastMonth(MOCK_API_KEY);

        assertNotNull(response);
        assertEquals("20450854620708", response.get(0).getIntDocNumber());
        assertEquals(1, response.size());
        assertTrue(response.stream()
            .allMatch(documentListDataResponse -> documentListDataResponse.getStateName()
                .equalsIgnoreCase(ARRIVED)));
    }

    @Test
    public void testGetUnreceivedParcels() throws JsonProcessingException {
        var maxDaysStorage = "4";
        var documentListRequest = stubDocumentListRequest();
        var documentsStatusRequest = stubDocumentsStatusRequest();

        stubRequestToNovaPoshta(documentListRequest, "valid_document_list_response.json");
        documentListRequest.getMethodProperties().setPage("2");
        stubRequestToNovaPoshta(documentListRequest, "valid_document_list_empty_response.json");
        stubRequestToNovaPoshta(documentsStatusRequest, "valid_document_status_response.json");

        var response = newPostServiceProxy.getUnreceivedParcels(MOCK_API_KEY, SENDER_PHONE_NUMBER,
            maxDaysStorage);

        assertNotNull(response);
        assertEquals("20450854620708", response.get(0).getNumber());
        assertEquals(1, response.size());
        assertTrue(response.stream()
            .allMatch(documentListDataResponse -> documentListDataResponse.getStatus()
                .equalsIgnoreCase(ARRIVED)));
    }

    @Test
    public void testCreateParcelReturnOrderToAddress() throws JsonProcessingException {
        var parcelReturnToAddressRequest = stubParcelReturnToAddressRequest();
        stubRequestToNovaPoshta(parcelReturnToAddressRequest, "valid_parcel_return_response.json");

        var parcelReturnOrderToAddressResponse = newPostServiceProxy.createParcelReturnOrderToAddress(
            MOCK_API_KEY, DOCUMENT_NUMBER);

        assertNotNull(parcelReturnOrderToAddressResponse);
        assertEquals(DOCUMENT_NUMBER,
            parcelReturnOrderToAddressResponse.getData().get(0).getNumber());
        assertTrue(parcelReturnOrderToAddressResponse.isSuccess());
    }

    @Test
    public void testCreateParcelReturnOrderToWarehouse() throws JsonProcessingException {
        var checkPossibilityCreateReturnRequest = stubCheckPossibilityCreateReturnRequest();
        var parcelReturnToWarehouseRequest = stubParcelReturnToWarehouseRequest();
        stubRequestToNovaPoshta(checkPossibilityCreateReturnRequest,
            "valid_check_possibility_create_return_order_response.json");
        stubRequestToNovaPoshta(parcelReturnToWarehouseRequest,
            "valid_parcel_return_response.json");

        var checkPossibilityCreateReturnOrderResponse = newPostServiceProxy.checkPossibilityCreateReturnOrder(
            MOCK_API_KEY, DOCUMENT_NUMBER);
        var parcelReturnOrderToWarehouseResponse = newPostServiceProxy.createParcelReturnOrderToWarehouse(
            MOCK_API_KEY, DOCUMENT_NUMBER,
            checkPossibilityCreateReturnOrderResponse.getData().get(0).getRef());

        assertNotNull(parcelReturnOrderToWarehouseResponse);
        assertEquals(DOCUMENT_NUMBER,
            parcelReturnOrderToWarehouseResponse.getData().get(0).getNumber());
        assertTrue(parcelReturnOrderToWarehouseResponse.isSuccess());
    }

    @Test
    public void testCheckPossibilityCreateReturnOrder() throws JsonProcessingException {
        var checkPossibilityCreateReturnRequest = stubCheckPossibilityCreateReturnRequest();
        stubRequestToNovaPoshta(checkPossibilityCreateReturnRequest,
            "valid_check_possibility_create_return_order_response.json");

        var response = newPostServiceProxy.checkPossibilityCreateReturnOrder(MOCK_API_KEY,
            DOCUMENT_NUMBER);

        assertNotNull(response);
        assertEquals("f0dda27e-b911-11ee-a60f-48df37b921db", response.getData().get(0).getRef());
        assertTrue(response.isSuccess());
    }

    private void stubRequestToNovaPoshta(Object request, String responseFileName)
        throws JsonProcessingException {
        stubFor(post(urlPathEqualTo("/"))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(request)))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile(responseFileName)));
    }

    private DocumentsStatusRequest stubDocumentsStatusRequest() {
        return DocumentsStatusRequest.builder()
            .calledMethod(GET_STATUS_DOCUMENTS)
            .modelName(TRACKING_DOCUMENT)
            .methodProperties(TrackingDocumentMethodProperties.builder()
                .documents(List.of(TrackingDocument.builder()
                    .documentNumber("20450854620708")
                    .phone(SENDER_PHONE_NUMBER)
                    .build()))
                .build())
            .build();
    }

    private DocumentListRequest stubDocumentListRequest() {
        var todayLocalDate = LocalDate.now();
        var todayFormatted = todayLocalDate.format(
            DateTimeFormatter.ofPattern(DATE_PATTERN));
        var lastMonthFormatted = todayLocalDate.minusMonths(Integer.parseInt(ONE))
            .format(DateTimeFormatter.ofPattern(DATE_PATTERN));

        return DocumentListRequest.builder()
            .apiKey(MOCK_API_KEY)
            .modelName(INTERNET_DOCUMENT)
            .calledMethod(GET_DOCUMENT_LIST)
            .methodProperties(DocumentListMethodProperties.builder()
                .page(ONE)
                .dateTimeFrom(lastMonthFormatted)
                .dateTimeTo(todayFormatted)
                .getFullList(ZERO)
                .build())
            .build();
    }

    private CheckPossibilityCreateReturnRequest stubCheckPossibilityCreateReturnRequest() {
        return CheckPossibilityCreateReturnRequest.builder()
            .apiKey(MOCK_API_KEY)
            .modelName(ADDITIONAL_SERVICE)
            .calledMethod(CHECK_POSSIBILITY_CREATE_RETURN)
            .methodProperties(CheckPossibilityCreateReturnProperties.builder()
                .number(DOCUMENT_NUMBER)
                .build())
            .build();
    }

    private ParcelReturnToWarehouseRequest stubParcelReturnToWarehouseRequest() {
        return ParcelReturnToWarehouseRequest.builder()
            .apiKey(MOCK_API_KEY)
            .modelName(ADDITIONAL_SERVICE)
            .calledMethod(SAVE)
            .methodProperties(ParcelReturnToWarehouseProperties.builder()
                .intDocNumber(DOCUMENT_NUMBER)
                .paymentMethod(PAYMENT_METHOD_CASH)
                .reason(RETURN_REASON)
                .subtypeReason(SUB_TYPE_RETURN_REASON)
                .orderType(ORDER_TYPE_CARGO_RETURN)
                .recipientWarehouse("f0dda27e-b911-11ee-a60f-48df37b921db")
                .build())
            .build();
    }

    private ParcelReturnToAddressRequest stubParcelReturnToAddressRequest() {
        return ParcelReturnToAddressRequest.builder()
            .apiKey(MOCK_API_KEY)
            .modelName(ADDITIONAL_SERVICE)
            .calledMethod(SAVE)
            .methodProperties(ParcelReturnToAddressMethodProperties.builder()
                .intDocNumber(DOCUMENT_NUMBER)
                .buildingNumber(BUILDING_NUMBER)
                .recipientSettlement(RECIPIENT_SETTLEMENT)
                .recipientSettlementStreet(RECIPIENT_SETTLEMENT_STREET)
                .reason(RETURN_REASON)
                .subtypeReason(SUB_TYPE_RETURN_REASON)
                .orderType(ORDER_TYPE_CARGO_RETURN)
                .paymentMethod(PAYMENT_METHOD_CASH)
                .build())
            .build();
    }
}
