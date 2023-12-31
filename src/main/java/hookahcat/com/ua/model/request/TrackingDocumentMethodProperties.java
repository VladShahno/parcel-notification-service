package hookahcat.com.ua.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class TrackingDocumentMethodProperties {

    @JsonProperty("Documents")
    private List<TrackingDocument> documents;

}
