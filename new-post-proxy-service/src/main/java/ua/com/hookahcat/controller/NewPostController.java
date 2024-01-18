package ua.com.hookahcat.controller;

import static net.logstash.logback.argument.StructuredArguments.kv;
import static ua.com.hookahcat.common.Constants.API_KEY;
import static ua.com.hookahcat.common.Constants.DATE_TIME_FROM;
import static ua.com.hookahcat.common.Constants.DATE_TIME_TO;
import static ua.com.hookahcat.common.Constants.DOCUMENT_NUMBER;
import static ua.com.hookahcat.common.Constants.MAX_STORAGE_DAYS;
import static ua.com.hookahcat.common.Constants.PHONE_NUMBER;

import ua.com.hookahcat.model.request.DocumentListRequest;
import ua.com.hookahcat.model.request.DocumentsStatusRequest;
import ua.com.hookahcat.model.response.CheckPossibilityCreateReturnResponse;
import ua.com.hookahcat.model.response.DocumentDataResponse;
import ua.com.hookahcat.model.response.DocumentListDataResponse;
import ua.com.hookahcat.model.response.DocumentListResponse;
import ua.com.hookahcat.model.response.DocumentsStatusResponse;
import ua.com.hookahcat.model.response.ParcelReturnResponse;
import ua.com.hookahcat.service.NewPostServiceProxy;
import io.swagger.v3.oas.annotations.Operation;
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
        log.info("Getting document status for {} with {}",
            kv(DOCUMENT_NUMBER, documentsStatusRequest.getMethodProperties().getDocuments()),
            kv(API_KEY, documentsStatusRequest.getApiKey()));

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
        log.info("Getting document list for {} from {} to {}",
            kv(API_KEY, documentListRequest.getApiKey()),
            kv(DATE_TIME_FROM, documentListRequest.getMethodProperties().getDateTimeFrom()),
            kv(DATE_TIME_TO, documentListRequest.getMethodProperties().getDateTimeTo()));

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
        @RequestParam String apiKey) {
        log.info("Getting document list for last month for {}", kv(API_KEY, apiKey));

        return newPostServiceProxy.getArrivedParcelsForLastMonth(apiKey);
    }

    @GetMapping("/parcels-status/unreceived")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Endpoint allows to view information about the status of the not received parcels (unreceived for 4 days)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document list received successfully",
            content = {@Content(schema = @Schema(implementation = DocumentDataResponse.class))}),
    })
    public List<DocumentDataResponse> getUnreceivedParcels(@RequestParam String apiKey,
        @RequestParam String phoneNumber, @RequestParam long maxStorageDays) {
        log.info("Getting unreceived parcels for  {} and {} with {}", kv(API_KEY, apiKey),
            kv(PHONE_NUMBER, phoneNumber), kv(MAX_STORAGE_DAYS, maxStorageDays));

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
    public CheckPossibilityCreateReturnResponse CheckPossibilityCreateReturnOrder(
        @RequestParam String apiKey,
        @RequestParam String documentNumber) {
        log.info("Check possibility create return order for {}",
            kv(DOCUMENT_NUMBER, documentNumber));

        return newPostServiceProxy.checkPossibilityCreateReturnOrder(apiKey, documentNumber);
    }

    @GetMapping("/return-order")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Endpoint allows to create a return order request (the method is only available for sender customers)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Response received successfully",
            content = {
                @Content(schema = @Schema(implementation = ParcelReturnResponse.class))}),
    })
    public ParcelReturnResponse createParcelReturnOrder(
        @RequestParam String apiKey,
        @RequestParam String documentNumber) {
        log.info("Creation return order for {}", kv(DOCUMENT_NUMBER, documentNumber));

        return newPostServiceProxy.createParcelReturnOrder(apiKey, documentNumber);
    }
}
