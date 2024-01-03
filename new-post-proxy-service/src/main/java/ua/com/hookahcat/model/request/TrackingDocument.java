package ua.com.hookahcat.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrackingDocument {

    @JsonProperty("DocumentNumber")
    private String documentNumber;

    @JsonProperty("Phone")
    private String phone;
}
