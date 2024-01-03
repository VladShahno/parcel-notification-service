package ua.com.hookahcat.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrackingDocumentMethodProperties {

    @JsonProperty("Documents")
    private List<TrackingDocument> documents;

}
