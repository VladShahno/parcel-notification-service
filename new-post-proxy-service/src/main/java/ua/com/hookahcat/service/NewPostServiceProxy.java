package ua.com.hookahcat.service;

import java.util.List;
import ua.com.hookahcat.model.request.DocumentListRequest;
import ua.com.hookahcat.model.request.DocumentsStatusRequest;
import ua.com.hookahcat.model.response.CheckPossibilityCreateReturnResponse;
import ua.com.hookahcat.model.response.data.DocumentDataResponse;
import ua.com.hookahcat.model.response.data.DocumentListDataResponse;
import ua.com.hookahcat.model.response.DocumentListResponse;
import ua.com.hookahcat.model.response.DocumentsStatusResponse;
import ua.com.hookahcat.model.response.ParcelReturnResponse;

public interface NewPostServiceProxy {

    DocumentsStatusResponse getDocumentsStatus(DocumentsStatusRequest documentsStatusRequest);

    DocumentListResponse getDocumentList(DocumentListRequest documentListRequest);

    List<DocumentListDataResponse> getArrivedParcelsForLastMonth(String apiKey);

    List<DocumentDataResponse> getUnreceivedParcels(String apiKey, String senderPhoneNumber,
        String maxStorageDays);

    ParcelReturnResponse createParcelReturnOrderToAddress(String apiKey, String documentNumber);

    ParcelReturnResponse createParcelReturnOrderToWarehouse(String apiKey, String documentNumber,
        String returnAddressRef);

    CheckPossibilityCreateReturnResponse checkPossibilityCreateReturnOrder(String apiKey,
        String documentNumber);

    byte[] getUnReceivedParcelsCsv(String maxStorageDays);
}
