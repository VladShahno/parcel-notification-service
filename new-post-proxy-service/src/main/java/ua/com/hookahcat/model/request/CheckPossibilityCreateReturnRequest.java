package ua.com.hookahcat.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ua.com.hookahcat.model.request.properties.CheckPossibilityCreateReturnProperties;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CheckPossibilityCreateReturnRequest extends
    BaseApiRequest<CheckPossibilityCreateReturnProperties> {

}
