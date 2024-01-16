package ua.com.hookahcat.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParcelReturnDataResponse {

    @JsonProperty("Number")
    private String number;

    @JsonProperty("Ref")
    private String ref;
}
