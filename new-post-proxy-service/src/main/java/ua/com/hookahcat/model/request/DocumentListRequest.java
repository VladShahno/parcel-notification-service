package ua.com.hookahcat.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ua.com.hookahcat.model.request.properties.DocumentListMethodProperties;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DocumentListRequest extends BaseApiRequest<DocumentListMethodProperties> {

}
