package hookahcat.com.ua.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DocumentListRequest {

    @JsonProperty("apiKey")
    private String apiKey;

    @JsonProperty("modelName")
    private String modelName;

    @JsonProperty("calledMethod")
    private String calledMethod;

    @JsonProperty("methodProperties")
    private DocumentListMethodProperties methodProperties;
}
