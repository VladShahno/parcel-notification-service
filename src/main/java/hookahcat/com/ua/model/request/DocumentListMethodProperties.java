package hookahcat.com.ua.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
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
