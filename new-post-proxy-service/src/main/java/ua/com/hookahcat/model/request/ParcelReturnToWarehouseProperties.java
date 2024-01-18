package ua.com.hookahcat.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParcelReturnToWarehouseProperties {

    @JsonProperty("IntDocNumber")
    private String intDocNumber;

    @JsonProperty("PaymentMethod")
    private String paymentMethod;

    @JsonProperty("Reason")
    private String reason;

    @JsonProperty("SubtypeReason")
    private String subtypeReason;

    @JsonProperty("Note")
    private String note;

    @JsonProperty("OrderType")
    private String orderType;

    @JsonProperty("RecipientWarehouse")
    private String recipientWarehouse;
}
