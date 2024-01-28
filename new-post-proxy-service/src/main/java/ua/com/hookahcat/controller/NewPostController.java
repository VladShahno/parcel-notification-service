package ua.com.hookahcat.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ua.com.hookahcat.model.request.DocumentListRequest;
import ua.com.hookahcat.model.request.DocumentsStatusRequest;
import ua.com.hookahcat.model.response.CheckPossibilityCreateReturnResponse;
import ua.com.hookahcat.model.response.data.DocumentDataResponse;
import ua.com.hookahcat.model.response.data.DocumentListDataResponse;
import ua.com.hookahcat.model.response.DocumentListResponse;
import ua.com.hookahcat.model.response.DocumentsStatusResponse;
import ua.com.hookahcat.model.response.ParcelReturnResponse;
import ua.com.hookahcat.service.NewPostServiceProxy;

@RestController
@AllArgsConstructor
@Slf4j
@Validated
@Tag(name = "New Post Controller", description = "Provides operations with novaposhta-API")
@RequestMapping(path = "/v1/nova-poshta", produces = MediaType.APPLICATION_JSON_VALUE)
public class NewPostController {

    private final NewPostServiceProxy newPostServiceProxy;

    @PostMapping("/parcels-status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Endpoint allows to view more extensive information about the status of the parcel.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Parcel info received successfully",
            content = {@Content(schema = @Schema(implementation = DocumentsStatusResponse.class))}),
    })
    public DocumentsStatusResponse getDocumentsStatus(
        @RequestBody @Valid DocumentsStatusRequest documentsStatusRequest) {
        return newPostServiceProxy.getDocumentsStatus(documentsStatusRequest);
    }

    @PostMapping("/document-list")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Endpoint will return all the EN numbers that were created in the the personal account and their identifiers (Ref)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document list received successfully",
            content = {@Content(schema = @Schema(implementation = DocumentListResponse.class))}),
    })
    public DocumentListResponse getDocumentList(
        @RequestBody @Valid DocumentListRequest documentListRequest) {
        return newPostServiceProxy.getDocumentList(documentListRequest);
    }

    @GetMapping("/document-list/month")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary =
        "Endpoint will return all the EN numbers that were created in the the personal account for last month with status Прибув у відділення")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document list received successfully",
            content = {
                @Content(schema = @Schema(implementation = DocumentListDataResponse.class))}),
    })
    public List<DocumentListDataResponse> getArrivedParcelsForLastMonth(
        @Parameter(description = "Nova-poshta user apiKey", example = "3e20cc3afc189c626ec671616e7a6468")
        @RequestParam String apiKey) {
        return newPostServiceProxy.getArrivedParcelsForLastMonth(apiKey);
    }

    @GetMapping("/parcels-status/unreceived")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Endpoint allows to view information about the status of the not received parcels (unreceived for 4 days)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document list received successfully",
            content = {@Content(schema = @Schema(implementation = DocumentDataResponse.class))}),
    })
    public List<DocumentDataResponse> getUnreceivedParcels(
        @Parameter(description = "Nova-poshta user apiKey", example = "3e20cc3afc189c626ec671616e7a6468")
        @RequestParam String apiKey,
        @Parameter(description = "Sender's phone number", example = "380600000000")
        @RequestParam String phoneNumber,
        @Parameter(description = "To filter and return all parcels waiting for N days after actual delivery to the warehouse", example = "4")
        @RequestParam String maxStorageDays) {
        return newPostServiceProxy.getUnreceivedParcels(apiKey, phoneNumber, maxStorageDays);
    }

    @GetMapping("/return-order/check-return-possibility")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary =
        "Endpoint allows to check the possibility of creating a return order request. The method is only available "
            + "to sender clients. If successful, blocks of addresses are returned that can be used to create an return order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Response received successfully",
            content = {
                @Content(schema = @Schema(implementation = CheckPossibilityCreateReturnResponse.class))}),
    })
    public CheckPossibilityCreateReturnResponse checkPossibilityCreateReturnOrder(
        @Parameter(description = "Nova-poshta user apiKey", example = "3e20cc3afc189c626ec671616e7a6468")
        @RequestParam String apiKey,
        @Parameter(description = "EN number (ТТН) for return to warehouse", example = "207004860074693")
        @RequestParam String documentNumber) {
        return newPostServiceProxy.checkPossibilityCreateReturnOrder(apiKey, documentNumber);
    }

    @Hidden
    @GetMapping("/return-order/to-address")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Endpoint allows to create a return order request to an address "
        + "(the method is only available for sender customers)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Response received successfully",
            content = {
                @Content(schema = @Schema(implementation = ParcelReturnResponse.class))}),
    })
    public ParcelReturnResponse createParcelReturnOrderToAddress(
        @Parameter(description = "Nova-poshta user apiKey", example = "3e20cc3afc189c626ec671616e7a6468")
        @RequestParam String apiKey,
        @Parameter(description = "EN number (ТТН) for return to address", example = "207004860074693")
        @RequestParam String documentNumber) {
        return newPostServiceProxy.createParcelReturnOrderToAddress(apiKey, documentNumber);
    }

    @GetMapping("/return-order/to-warehouse")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Endpoint allows to create a return order request to a warehouse "
        + "(the method is only available for sender customers)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Response received successfully",
            content = {
                @Content(schema = @Schema(implementation = ParcelReturnResponse.class))}),
    })
    public ParcelReturnResponse createParcelReturnOrderToWarehouse(
        @Parameter(description = "Nova-poshta user apiKey", example = "3e20cc3afc189c626ec671616e7a6468")
        @RequestParam String apiKey,
        @Parameter(description = "EN number (ТТН) for return to warehouse", example = "207004860074693")
        @RequestParam String documentNumber,
        @Parameter(description = "Identifier (REF) of the warehouse for return, must be taken from "
            + "the response from checkPossibilityCreateReturnOrder API", example = "e71d006d-4b33-11e4-ab6d-005056801329")
        @RequestParam String returnAddressRef) {
        return newPostServiceProxy.createParcelReturnOrderToWarehouse(apiKey, documentNumber,
            returnAddressRef);
    }
}
