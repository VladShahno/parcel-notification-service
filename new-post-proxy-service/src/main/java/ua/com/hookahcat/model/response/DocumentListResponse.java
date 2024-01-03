package ua.com.hookahcat.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class DocumentListResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("data")
    private List<DocumentListDataResponse> data;

    @JsonProperty("errors")
    private List<String> errors;

    @JsonProperty("warnings")
    private List<String> warnings;

    @JsonProperty("messageCodes")
    private List<String> messageCodes;

    @JsonProperty("errorCodes")
    private List<String> errorCodes;

    @JsonProperty("warningCodes")
    private List<String> warningCodes;

    @JsonProperty("infoCodes")
    private List<String> infoCodes;

}
