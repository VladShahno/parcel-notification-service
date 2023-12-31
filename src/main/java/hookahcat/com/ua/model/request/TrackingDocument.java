package hookahcat.com.ua.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TrackingDocument {

    @JsonProperty("DocumentNumber")
    private String documentNumber;

    @JsonProperty("Phone")
    private String phone;
}
