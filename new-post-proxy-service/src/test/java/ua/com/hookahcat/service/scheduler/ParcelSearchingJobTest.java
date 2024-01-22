package ua.com.hookahcat.service.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ua.com.hookahcat.common.Constants.Patterns.DATE_PATTERN;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ua.com.hookahcat.configuration.CsvProperties;
import ua.com.hookahcat.configuration.NovaPoshtaApiProperties;
import ua.com.hookahcat.csvsdk.service.CsvService;
import ua.com.hookahcat.model.response.CheckPossibilityCreateReturnData;
import ua.com.hookahcat.model.response.CheckPossibilityCreateReturnResponse;
import ua.com.hookahcat.model.response.DocumentDataResponse;
import ua.com.hookahcat.model.response.ParcelReturnResponse;
import ua.com.hookahcat.notification.configuration.EmailNotificationProperties;
import ua.com.hookahcat.notification.service.EmailNotificationService;
import ua.com.hookahcat.service.NewPostServiceProxy;

@ExtendWith(SpringExtension.class)
class ParcelSearchingJobTest {

    @Mock
    private EmailNotificationService emailNotificationService;

    @Mock
    private CsvProperties csvProperties;

    @Mock
    private NovaPoshtaApiProperties novaPoshtaApiProperties;

    @Mock
    private EmailNotificationProperties emailNotificationProperties;

    @Mock
    private CsvService csvService;

    @Mock
    private NewPostServiceProxy newPostServiceProxy;

    @InjectMocks
    private ParcelSearchingJob parcelSearchingJob;

    @Test
    void testSearchUnReceivedParcels() {
        when(csvProperties.getResponseHeaders()).thenReturn(Arrays.asList("header1", "header2"));
        when(csvProperties.getResponseFields()).thenReturn(Arrays.asList("field1", "field2"));
        when(csvService.exportData(any(), any(), any())).thenReturn("mockedCsvData".getBytes());

        parcelSearchingJob.searchUnReceivedParcels();

        verify(emailNotificationService, times(1)).sendEmailNotification(any());
    }

    @Test
    void testCreateParcelReturnOrderToWarehouse() {
        when(novaPoshtaApiProperties.getApiKey()).thenReturn("mockedApiKey");
        when(novaPoshtaApiProperties.getSenderPhoneNumber()).thenReturn("mockedPhoneNumber");
        when(newPostServiceProxy.getUnreceivedParcels("mockedApiKey", "mockedPhoneNumber", 9L))
            .thenReturn(List.of(stubDocumentDataResponse()));
        when(newPostServiceProxy.checkPossibilityCreateReturnOrder("mockedApiKey",
            "mockNumber")).thenReturn(CheckPossibilityCreateReturnResponse.builder()
            .success(true)
            .data(List.of(CheckPossibilityCreateReturnData.builder()
                .ref("mockRef")
                .build()))
            .build());
        when(newPostServiceProxy.createParcelReturnOrderToWarehouse("mockedApiKey", "mockNumber",
            "mockRef")).thenReturn(ParcelReturnResponse.builder().build());

        parcelSearchingJob.createParcelReturnOrderToWarehouse();

        verify(newPostServiceProxy, times(1)).checkPossibilityCreateReturnOrder("mockedApiKey",
            "mockNumber");
        verify(newPostServiceProxy, times(1)).createParcelReturnOrderToWarehouse("mockedApiKey",
            "mockNumber",
            "mockRef");
    }

    @Test
    void testCreateParcelReturnOrderToWarehouse_NoUnreceivedParcels() {
        when(newPostServiceProxy.getUnreceivedParcels(anyString(), anyString(),
            anyLong())).thenReturn(Collections.emptyList());

        parcelSearchingJob.createParcelReturnOrderToWarehouse();

        verify(newPostServiceProxy, times(0)).checkPossibilityCreateReturnOrder(anyString(),
            anyString());
    }

    @Test
    void testGetUnReceivedParcelsCsv() {
        when(novaPoshtaApiProperties.getApiKey()).thenReturn("mockedApiKey");
        when(novaPoshtaApiProperties.getSenderPhoneNumber()).thenReturn("mockedPhoneNumber");
        when(newPostServiceProxy.getUnreceivedParcels("mockedApiKey", "mockedPhoneNumber", 5L))
            .thenReturn(List.of(stubDocumentDataResponse()));
        when(csvProperties.getResponseHeaders()).thenReturn(Arrays.asList("header1", "header2"));
        when(csvProperties.getResponseFields()).thenReturn(Arrays.asList("field1", "field2"));
        when(csvService.exportData(any(), any(), any())).thenReturn("mockedCsvData".getBytes());

        byte[] result = parcelSearchingJob.getUnReceivedParcelsCsv(5);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testShouldReturnNoUnReceivedParcelsCsv() {
        when(csvService.exportData(any(), any(), any())).thenReturn(new byte[0]);

        byte[] result = parcelSearchingJob.getUnReceivedParcelsCsv(5);

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testCreateCsvFile() {
        when(csvService.exportData(any(), any(), any())).thenReturn("mockedCsvData".getBytes());

        File file = ParcelSearchingJob.createCsvFile("mockedCsvData".getBytes());

        assertNotNull(file);
        assertTrue(file.exists());
        assertEquals("Not received parcels [" + LocalDate.now()
            .format(DateTimeFormatter.ofPattern(DATE_PATTERN)) + "].csv", file.getName());
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
