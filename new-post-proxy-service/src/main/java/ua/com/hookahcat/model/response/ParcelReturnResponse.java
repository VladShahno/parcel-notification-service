package ua.com.hookahcat.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParcelReturnResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("data")
    private List<ParcelReturnDataResponse> data;

    @JsonProperty("errors")
    private List<String> errors;

    @JsonProperty("warnings")
    private List<String> warnings;

    @JsonProperty("info")
    private List<String> info;

    @JsonProperty("messageCodes")
    private List<String> messageCodes;

    @JsonProperty("errorCodes")
    private List<String> errorCodes;

    @JsonProperty("warningCodes")
    private List<String> warningCodes;

    @JsonProperty("infoCodes")
    private List<String> infoCodes;
}
