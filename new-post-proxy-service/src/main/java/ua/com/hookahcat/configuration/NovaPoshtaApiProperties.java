package ua.com.hookahcat.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "novaposhta-api")
@Data
public class NovaPoshtaApiProperties {

    private String baseUrl;
    private String apiKey;
    private String senderPhoneNumber;
    private ReturnOrder returnOrder = new ReturnOrder();


    @Data
    public static class ReturnOrder {

        private String recipientSettlement;
        private String recipientSettlementStreet;
        private String buildingNumber;
        private String returnReason;
        private String returnSubtypeReason;
    }
}
