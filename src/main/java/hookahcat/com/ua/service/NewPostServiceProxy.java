package hookahcat.com.ua.service;

import hookahcat.com.ua.model.request.DocumentListRequest;
import hookahcat.com.ua.model.request.DocumentsStatusRequest;
import hookahcat.com.ua.model.response.DocumentListDataResponse;
import hookahcat.com.ua.model.response.DocumentListResponse;
import hookahcat.com.ua.model.response.DocumentsStatusResponse;
import java.util.List;

public interface NewPostServiceProxy {

    DocumentsStatusResponse getDocumentsStatus(DocumentsStatusRequest documentsStatusRequest);

    DocumentListResponse getDocumentList(DocumentListRequest documentListRequest);

    List<DocumentListDataResponse> getArrivedParcelsForLastMonth(String apiKey);
}
