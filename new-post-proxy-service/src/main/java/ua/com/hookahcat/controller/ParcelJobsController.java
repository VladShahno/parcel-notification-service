package ua.com.hookahcat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ua.com.hookahcat.service.scheduler.ParcelSearchingJob;

@RestController
@AllArgsConstructor
@Slf4j
@Validated
@Tag(name = "Parcel Jobs Controller", description = "Provides operations allowed to run parcel jobs")
@RequestMapping(path = "/v1/nova-poshta/run-job", produces = MediaType.APPLICATION_JSON_VALUE)
public class ParcelJobsController {

    private final ParcelSearchingJob parcelSearchingJob;

    @PostMapping("/return-order-to-warehouse")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Endpoint allows to run scheduled createParcelReturnOrderToWarehouse job directly")
    public void createParcelReturnOrderToWarehouse() {
        parcelSearchingJob.createParcelReturnOrderToWarehouse();
    }
}
