package ua.com.hookahcat.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
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
