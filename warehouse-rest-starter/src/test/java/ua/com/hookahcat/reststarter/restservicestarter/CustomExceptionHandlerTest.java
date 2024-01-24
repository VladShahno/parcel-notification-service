package ua.com.hookahcat.reststarter.restservicestarter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartException;
import ua.com.hookahcat.reststarter.exception.BadRequestException;
import ua.com.hookahcat.reststarter.exception.DuplicateResourceException;
import ua.com.hookahcat.reststarter.exception.MultipleIllegalArgumentsException;
import ua.com.hookahcat.reststarter.exception.ResourceNotFoundException;
import ua.com.hookahcat.reststarter.exception.handler.CustomExceptionHandler;
import ua.com.hookahcat.reststarter.model.error.ErrorDetails;
import ua.com.hookahcat.reststarter.model.error.InformationDisclosureHandlingConfig;

@ExtendWith(MockitoExtension.class)
class CustomExceptionHandlerTest {

    private static final String message = "error message";
    private static final String localizeMessage = "localize error message";
    private static final Locale locale = Locale.getDefault();

    @Mock
    private MessageSource messageSource;
    @Mock
    private InformationDisclosureHandlingConfig informationDisclosureHandlingConfig;
    @InjectMocks
    private CustomExceptionHandler exceptionHandler;

    @Test
    void handleResourceNotFoundExceptionShouldReturnNotFoundResponse() {
        mockSuccessfulMessageSourceGetMessage();
        final ResponseEntity<Object> responseEntity = exceptionHandler.handleResourceNotFoundException(
            new ResourceNotFoundException(message), locale);
        verifySingleMassageResponse(responseEntity, HttpStatus.NOT_FOUND, localizeMessage);
    }

    @Test
    void handleDuplicateResourceExceptionShouldReturnConflictResponse() {
        mockSuccessfulMessageSourceGetMessage();
        final ResponseEntity<Object> responseEntity = exceptionHandler.handleDuplicateResourceException(
            new DuplicateResourceException(message), locale);
        verifySingleMassageResponse(responseEntity, HttpStatus.CONFLICT, localizeMessage);
    }

    @Test
    void handleAccessedDeniedExceptionShouldReturnForbiddenResponse() {
        mockSuccessfulMessageSourceGetMessage();
        final ResponseEntity<Object> responseEntity = exceptionHandler.handleAccessedDeniedException(
            new AccessDeniedException(message), locale);
        verifySingleMassageResponse(responseEntity, HttpStatus.FORBIDDEN, localizeMessage);
    }

    @Test
    void handleIllegalArgumentExceptionShouldReturnBadRequestResponse() {
        mockSuccessfulMessageSourceGetMessage();
        final ResponseEntity<Object> responseEntity = exceptionHandler.handleIllegalArgumentException(
            new IllegalArgumentException(message), locale);
        verifySingleMassageResponse(responseEntity, HttpStatus.BAD_REQUEST, localizeMessage);
    }

    @Test
    void handleBadRequestExceptionShouldReturnBadRequestResponse() {
        mockSuccessfulMessageSourceGetMessage();
        final ResponseEntity<Object> responseEntity = exceptionHandler.handleBadRequestException(
            new BadRequestException(message), locale);
        verifySingleMassageResponse(responseEntity, HttpStatus.BAD_REQUEST, localizeMessage);
    }

    @Test
    void handleUnsupportedOperationExceptionShouldReturnBadRequestResponse() {
        mockSuccessfulMessageSourceGetMessage();
        final ResponseEntity<Object> responseEntity = exceptionHandler.handleUnsupportedOperationException(
            new UnsupportedOperationException(message), locale);
        verifySingleMassageResponse(responseEntity, HttpStatus.METHOD_NOT_ALLOWED, localizeMessage);
    }

    @Test
    void handleMultipleIllegalArgumentExceptionShouldReturnBadRequestErrorResponse() {
        mockSuccessfulMessageSourceGetMessage();
        final ResponseEntity<Object> responseEntity = exceptionHandler.handleMultipleIllegalArgumentException(
            new MultipleIllegalArgumentsException(List.of(message)), locale);
        verifySingleMassageResponse(responseEntity, HttpStatus.BAD_REQUEST, localizeMessage);
    }

    @Test
    void handleFileUploadExceptionShouldReturnBadRequestErrorResponse() {
        mockSuccessfulMessageSourceGetMessage();
        final ResponseEntity<Object> responseEntity = exceptionHandler.handleFileUploadException(
            new MultipartException(message), locale);
        verifySingleMassageResponse(responseEntity, HttpStatus.BAD_REQUEST, localizeMessage);
    }

    private void verifySingleMassageResponse(
        ResponseEntity<Object> responseEntity,
        HttpStatus expectedHttpCode,
        String expectedErrorMessage) {
        Assertions.assertNotNull(responseEntity);
        final HttpStatusCode statusCode = responseEntity.getStatusCode();
        Assertions.assertNotNull(statusCode);
        Assertions.assertEquals(expectedHttpCode, statusCode);
        final ErrorDetails body = (ErrorDetails) responseEntity.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertEquals(expectedErrorMessage, body.getMessages().get(0));
    }

    private void mockSuccessfulMessageSourceGetMessage() {
        when(messageSource.getMessage(eq(message), any(), eq(locale))).thenReturn(localizeMessage);
    }
}
