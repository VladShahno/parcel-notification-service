package ua.com.hookahcat.model.request.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckPossibilityCreateReturnProperties {

    @JsonProperty("Number")
    private String number;
}
