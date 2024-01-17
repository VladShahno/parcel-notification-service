package ua.com.hookahcat.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckPossibilityCreateReturnProperties {

    @JsonProperty("Number")
    private String number;
}
