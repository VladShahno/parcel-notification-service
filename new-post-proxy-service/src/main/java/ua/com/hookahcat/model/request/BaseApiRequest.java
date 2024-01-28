package ua.com.hookahcat.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseApiRequest<T> {

    @JsonProperty("apiKey")
    private String apiKey;

    @JsonProperty("modelName")
    @Schema(hidden = true)
    private String modelName;

    @JsonProperty("calledMethod")
    @Schema(hidden = true)
    private String calledMethod;

    @JsonProperty("methodProperties")
    private T methodProperties;
}
