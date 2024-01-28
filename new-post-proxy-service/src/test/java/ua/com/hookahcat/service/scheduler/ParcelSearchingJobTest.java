package ua.com.hookahcat.service.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ua.com.hookahcat.util.Constants.Patterns.DATE_PATTERN;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ua.com.hookahcat.configuration.NovaPoshtaApiProperties;
import ua.com.hookahcat.model.response.data.CheckPossibilityCreateReturnData;
import ua.com.hookahcat.model.response.CheckPossibilityCreateReturnResponse;
import ua.com.hookahcat.model.response.data.DocumentDataResponse;
import ua.com.hookahcat.model.response.data.ParcelReturnDataResponse;
import ua.com.hookahcat.model.response.ParcelReturnResponse;
import ua.com.hookahcat.notification.configuration.EmailNotificationProperties;
import ua.com.hookahcat.notification.service.EmailNotificationService;
import ua.com.hookahcat.service.NewPostServiceProxy;
import ua.com.hookahcat.telegram.bot.service.HookahCatTelegramBotService;

@ExtendWith(SpringExtension.class)
class ParcelSearchingJobTest {

    @Mock
    private EmailNotificationService emailNotificationService;

    @Mock
    private NovaPoshtaApiProperties novaPoshtaApiProperties;

    @Mock
    private EmailNotificationProperties emailNotificationProperties;

    @Mock
    private HookahCatTelegramBotService hookahCatTelegramBotService;

    @Mock
    private NewPostServiceProxy newPostServiceProxy;

    @InjectMocks
    private ParcelSearchingJob parcelSearchingJob;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(parcelSearchingJob, "maxStorageDaysBeforeReturnOrder", "7");
        ReflectionTestUtils.setField(parcelSearchingJob, "maxStorageDaysBeforeNotification", "4");
    }

    @Test
    void testSearchUnReceivedParcels() {
        when(newPostServiceProxy.getUnReceivedParcelsCsv("4")).thenReturn(
            ("ЕН,ПІБ отримувача з накладної,Номер телефону отримувача,Місто отримувача,Фактична дата доставки,Сума зворотної доставки,Поточне значення суми грошового переказу,Вартість доставки,Дата початку платного зберігання,Сума оплати по ЕН,Дата повернення вантажу,Дата початку зберігання (після не отримання),Оголошена вартість,Вартість зберігання\\n\"\n"
                + Arrays.toString(
                "+ \"20450854620708,Мостицький Роман,380505559309,Херсон,2024-01-12 17:12:12,350,350.000,70,2024-01-20 17:12:12,467,,2024-01-20,350,20".getBytes(
                    StandardCharsets.UTF_8))).getBytes());

        parcelSearchingJob.searchUnReceivedParcels();

        verify(emailNotificationService, times(1)).sendEmailNotification(any());
    }

    @Test
    void testCreateParcelReturnOrderToWarehouse() {
        when(novaPoshtaApiProperties.getApiKey()).thenReturn("mockedApiKey");
        when(novaPoshtaApiProperties.getSenderPhoneNumber()).thenReturn("mockedPhoneNumber");
        when(newPostServiceProxy.getUnreceivedParcels("mockedApiKey", "mockedPhoneNumber", "7"))
            .thenReturn(List.of(stubDocumentDataResponse()));
        when(newPostServiceProxy.checkPossibilityCreateReturnOrder("mockedApiKey",
            "mockNumber")).thenReturn(CheckPossibilityCreateReturnResponse.builder()
            .success(true)
            .data(List.of(CheckPossibilityCreateReturnData.builder()
                .ref("mockRef")
                .build()))
            .build());
        when(newPostServiceProxy.createParcelReturnOrderToWarehouse("mockedApiKey", "mockNumber",
            "mockRef")).thenReturn(ParcelReturnResponse.builder()
            .success(true)
            .data(List.of(ParcelReturnDataResponse.builder()
                .number("mockNumber")
                .ref("mockRef")
                .build()))
            .build());

        parcelSearchingJob.createParcelReturnOrderToWarehouse();

        verify(newPostServiceProxy, times(1)).checkPossibilityCreateReturnOrder("mockedApiKey",
            "mockNumber");
        verify(newPostServiceProxy, times(1)).createParcelReturnOrderToWarehouse("mockedApiKey",
            "mockNumber", "mockRef");
        verify(emailNotificationService, times(1)).sendEmailNotification(any());
        verify(hookahCatTelegramBotService, times(1)).sendMessageWithNotReceivedParcelsNumbers(
            List.of("mockNumber"));
    }

    @Test
    void testCreateParcelReturnOrderToWarehouseWhenNoUnreceivedParcels() {
        when(newPostServiceProxy.getUnreceivedParcels(anyString(), anyString(),
            anyString())).thenReturn(Collections.emptyList());

        parcelSearchingJob.createParcelReturnOrderToWarehouse();

        verify(newPostServiceProxy, times(0)).checkPossibilityCreateReturnOrder(anyString(),
            anyString());
        verify(emailNotificationService, times(0)).sendEmailNotification(any());

    }

    private DocumentDataResponse stubDocumentDataResponse() {
        return DocumentDataResponse.builder()
            .number("mockNumber")
            .actualDeliveryDate(
                LocalDate.now().minusDays(5)
                    .format(DateTimeFormatter.ofPattern(DATE_PATTERN)))
            .build();
    }
}
