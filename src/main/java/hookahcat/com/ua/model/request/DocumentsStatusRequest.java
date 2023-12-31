package hookahcat.com.ua.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DocumentsStatusRequest {

    @JsonProperty("apiKey")
    private String apiKey;

    @JsonProperty("modelName")
    private String modelName;

    @JsonProperty("calledMethod")
    private String calledMethod;

    @JsonProperty("methodProperties")
    private TrackingDocumentMethodProperties methodProperties;
}
