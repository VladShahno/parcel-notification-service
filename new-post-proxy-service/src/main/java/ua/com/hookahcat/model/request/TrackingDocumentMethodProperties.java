package ua.com.hookahcat.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingDocumentMethodProperties {

    @JsonProperty("Documents")
    private List<TrackingDocument> documents;

}
