package hookahcat.com.ua.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "web.client")
public class WebClientProperties {

    private int httpClientResponseTimeout;
    private int connectionProviderMaxIdleTime;
    private int connectionProviderMaxLifeTime;
    private int connectionProviderPendingAcquireTimeout;
    private int connectionProviderEvictInBackgroundTimeout;
}
