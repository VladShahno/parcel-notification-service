package ua.com.hookahcat.model.response.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckPossibilityCreateReturnData {

    @JsonProperty("NonCash")
    private boolean nonCash;

    @JsonProperty("City")
    private String city;

    @JsonProperty("Counterparty")
    private String counterparty;

    @JsonProperty("ContactPerson")
    private String contactPerson;

    @JsonProperty("Address")
    private String address;

    @JsonProperty("Phone")
    private String phone;

    @JsonProperty("Ref")
    private String ref;
}
