package ua.com.hookahcat.model.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ua.com.hookahcat.model.response.data.CheckPossibilityCreateReturnData;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CheckPossibilityCreateReturnResponse extends
    BaseApiResponse<CheckPossibilityCreateReturnData> {

}
