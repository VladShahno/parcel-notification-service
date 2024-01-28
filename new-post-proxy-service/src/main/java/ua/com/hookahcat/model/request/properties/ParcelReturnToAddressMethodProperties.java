package ua.com.hookahcat.model.request.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParcelReturnToAddressMethodProperties {

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

    @JsonProperty("RecipientSettlement")
    private String recipientSettlement;

    @JsonProperty("RecipientSettlementStreet")
    private String recipientSettlementStreet;

    @JsonProperty("BuildingNumber")
    private String buildingNumber;

    @JsonProperty("NoteAddressRecipient")
    private String noteAddressRecipient;
}
