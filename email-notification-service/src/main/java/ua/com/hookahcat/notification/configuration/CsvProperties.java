package ua.com.hookahcat.notification.configuration;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "csv")
public class CsvProperties {

    private List<String> responseHeaders;
    private List<String> responseFields;
    private int exportCsvLimit;
}
