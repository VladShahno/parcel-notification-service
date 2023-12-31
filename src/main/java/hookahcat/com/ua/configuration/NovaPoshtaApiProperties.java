package hookahcat.com.ua.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "novaposhta-api")
@Data
public class NovaPoshtaApiProperties {

    private String baseUrl;
}
