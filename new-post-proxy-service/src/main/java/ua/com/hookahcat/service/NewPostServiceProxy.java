package ua.com.hookahcat.service;

import java.util.List;
import ua.com.hookahcat.model.request.DocumentListRequest;
import ua.com.hookahcat.model.request.DocumentsStatusRequest;
import ua.com.hookahcat.model.response.CheckPossibilityCreateReturnResponse;
import ua.com.hookahcat.model.response.DocumentDataResponse;
import ua.com.hookahcat.model.response.DocumentListDataResponse;
import ua.com.hookahcat.model.response.DocumentListResponse;
import ua.com.hookahcat.model.response.DocumentsStatusResponse;
import ua.com.hookahcat.model.response.ParcelReturnResponse;

public interface NewPostServiceProxy {

    DocumentsStatusResponse getDocumentsStatus(DocumentsStatusRequest documentsStatusRequest);

    DocumentListResponse getDocumentList(DocumentListRequest documentListRequest);

    List<DocumentListDataResponse> getArrivedParcelsForLastMonth(String apiKey);

    List<DocumentDataResponse> getUnreceivedParcels(String apiKey, String senderPhoneNumber,
        long maxStorageDays);

    ParcelReturnResponse createParcelReturnOrder(String apiKey, String documentNumber);

    CheckPossibilityCreateReturnResponse checkPossibilityCreateReturnOrder(String apiKey,
        String documentNumber);
}
