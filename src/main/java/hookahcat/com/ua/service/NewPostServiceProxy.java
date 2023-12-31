package hookahcat.com.ua.service;

import hookahcat.com.ua.model.request.DocumentListRequest;
import hookahcat.com.ua.model.request.DocumentsStatusRequest;
import hookahcat.com.ua.model.response.DocumentListResponse;
import hookahcat.com.ua.model.response.DocumentsStatusResponse;

public interface NewPostServiceProxy {

    DocumentsStatusResponse getDocumentsStatus(DocumentsStatusRequest documentsStatusRequest);

    DocumentListResponse getDocumentList(DocumentListRequest documentListRequest);
}
