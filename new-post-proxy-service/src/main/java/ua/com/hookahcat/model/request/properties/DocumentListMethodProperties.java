package ua.com.hookahcat.model.request.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentListMethodProperties {

    @JsonProperty("DateTimeFrom")
    private String dateTimeFrom;

    @JsonProperty("DateTimeTo")
    private String dateTimeTo;

    @JsonProperty("Page")
    private String page;

    @JsonProperty("GetFullList")
    private String getFullList;

    @JsonProperty("DateTime")
    private String dateTime;
}
