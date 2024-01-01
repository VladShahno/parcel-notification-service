package hookahcat.com.ua.controller;

import hookahcat.com.ua.model.request.DocumentListRequest;
import hookahcat.com.ua.model.request.DocumentsStatusRequest;
import hookahcat.com.ua.model.response.DocumentDataResponse;
import hookahcat.com.ua.model.response.DocumentListDataResponse;
import hookahcat.com.ua.model.response.DocumentListResponse;
import hookahcat.com.ua.model.response.DocumentsStatusResponse;
import hookahcat.com.ua.service.NewPostServiceProxy;
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
    @Operation(summary = "Endpoint will return all the EN numbers that were created in the the personal account for last month")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document list received successfully",
            content = {
                @Content(schema = @Schema(implementation = DocumentListDataResponse.class))}),
    })
    public List<DocumentListDataResponse> getArrivedParcelsForLastMonth(
        @RequestParam String apiKey) {
        return newPostServiceProxy.getArrivedParcelsForLastMonth(apiKey);
    }

    @GetMapping("/parcels-status/unreceived")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Endpoint allows to view information about the status of the not received parcels")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document list received successfully",
            content = {@Content(schema = @Schema(implementation = DocumentDataResponse.class))}),
    })
    public List<DocumentDataResponse> getUnreceivedParcels(@RequestParam String apiKey,
        @RequestParam String phoneNumber) {
        return newPostServiceProxy.getUnreceivedParcels(apiKey, phoneNumber);
    }
}
