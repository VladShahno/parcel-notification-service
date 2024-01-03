package ua.com.hookahcat.model.response;

import java.util.List;
import lombok.Data;

@Data
public class DocumentsStatusResponse {

    private boolean success;
    private List<DocumentDataResponse> data;
    private List<String> errors;
    private List<String> info;
    private List<String> messageCodes;
    private List<String> errorCodes;
    private List<String> warningCodes;
    private List<String> infoCodes;
}
