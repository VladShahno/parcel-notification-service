package ua.com.hookahcat.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DocumentListDataResponse {

    @JsonProperty("Ref")
    private String ref;

    @JsonProperty("DateTime")
    private String dateTime;

    @JsonProperty("PreferredDeliveryDate")
    private String preferredDeliveryDate;

    @JsonProperty("RecipientDateTime")
    private String recipientDateTime;

    @JsonProperty("EWDateCreated")
    private String ewDateCreated;

    @JsonProperty("Weight")
    private String weight;

    @JsonProperty("SeatsAmount")
    private String seatsAmount;

    @JsonProperty("IntDocNumber")
    private String intDocNumber;

    @JsonProperty("Cost")
    private String cost;

    @JsonProperty("CitySender")
    private String citySender;

    @JsonProperty("CityRecipient")
    private String cityRecipient;

    @JsonProperty("SenderAddress")
    private String senderAddress;

    @JsonProperty("RecipientAddress")
    private String recipientAddress;

    @JsonProperty("CostOnSite")
    private String costOnSite;

    @JsonProperty("PayerType")
    private String payerType;

    @JsonProperty("PaymentMethod")
    private String paymentMethod;

    @JsonProperty("AfterpaymentOnGoodsCost")
    private String afterpaymentOnGoodsCost;

    @JsonProperty("PackingNumber")
    private String packingNumber;

    @JsonProperty("RejectionReason")
    private String rejectionReason;

    @JsonProperty("StateId")
    private String stateId;

    @JsonProperty("StateName")
    private String stateName;

}
