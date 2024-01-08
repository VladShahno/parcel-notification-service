package ua.com.hookahcat.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentsStatusRequest {

    @JsonProperty("apiKey")
    private String apiKey;

    @JsonProperty("modelName")
    @Schema(hidden = true)
    private String modelName;

    @JsonProperty("calledMethod")
    @Schema(hidden = true)
    private String calledMethod;

    @JsonProperty("methodProperties")
    private TrackingDocumentMethodProperties methodProperties;
}
