package ua.com.hookahcat.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ua.com.hookahcat.model.response.data.ParcelReturnDataResponse;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ParcelReturnResponse extends BaseApiResponse<ParcelReturnDataResponse> {

    @JsonProperty("info")
    private List<String> infoCodes;
}
