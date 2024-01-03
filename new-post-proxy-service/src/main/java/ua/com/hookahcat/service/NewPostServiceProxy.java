package ua.com.hookahcat.service;

import ua.com.hookahcat.model.request.DocumentListRequest;
import ua.com.hookahcat.model.request.DocumentsStatusRequest;
import ua.com.hookahcat.model.response.DocumentDataResponse;
import ua.com.hookahcat.model.response.DocumentListDataResponse;
import ua.com.hookahcat.model.response.DocumentListResponse;
import ua.com.hookahcat.model.response.DocumentsStatusResponse;
import java.util.List;

public interface NewPostServiceProxy {

    DocumentsStatusResponse getDocumentsStatus(DocumentsStatusRequest documentsStatusRequest);

    DocumentListResponse getDocumentList(DocumentListRequest documentListRequest);

    List<DocumentListDataResponse> getArrivedParcelsForLastMonth(String apiKey);

    List<DocumentDataResponse> getUnreceivedParcels(String apiKey, String senderPhoneNumber);
}
