package ua.com.hookahcat.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ua.com.hookahcat.model.request.properties.ParcelReturnToAddressMethodProperties;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ParcelReturnToAddressRequest extends
    BaseApiRequest<ParcelReturnToAddressMethodProperties> {

}
