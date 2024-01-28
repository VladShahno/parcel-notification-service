package ua.com.hookahcat.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ua.com.hookahcat.model.response.data.DocumentDataResponse;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DocumentsStatusResponse extends BaseApiResponse<DocumentDataResponse> {

    @JsonProperty("info")
    private List<String> info;
}
