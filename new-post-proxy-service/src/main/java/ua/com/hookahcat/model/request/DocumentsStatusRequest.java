package ua.com.hookahcat.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ua.com.hookahcat.model.request.properties.TrackingDocumentMethodProperties;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DocumentsStatusRequest extends BaseApiRequest<TrackingDocumentMethodProperties> {

}
