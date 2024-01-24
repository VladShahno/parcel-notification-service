package ua.com.hookahcat.reststarter.rest.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static ua.com.hookahcat.reststarter.common.Constants.INVALID_FIELD_FORMAT_MESSAGE;
import static ua.com.hookahcat.reststarter.common.Constants.INVALID_SPECIAL_CHARACTERS_MESSAGE;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ua.com.hookahcat.reststarter.exception.handler.ValidationExceptionHandler;
import ua.com.hookahcat.reststarter.model.error.ErrorResponse;

@ExtendWith(MockitoExtension.class)
class ValidationExceptionHandlerTest {

  private static final String FIELD_NAME = "some-field";
  private static final String MESSAGE = "some-string";
  private static final String TEMPLATE = "template";
    private static final String VARIABLE_NAME = "description";

  private final ValidationExceptionHandler testedObject = new ValidationExceptionHandler();

  @Mock
  private ConstraintViolationException constraintViolationException;
  @Mock
  private BindingResult bindingResult;
  @Mock
  private ConstraintViolation<?> constraintViolation;
  @Mock
  private FieldError fieldError;
  @Mock
  private ObjectError objectError;
  @Mock
  private Path path;
  @Mock
  private JsonMappingException.Reference reference;
  @Mock
  private InvalidFormatException invalidFormatException;
  @Mock
  private JsonParseException jsonParseException;
  @Mock
  private MismatchedInputException mismatchedInputException;
  @Mock
  private MethodArgumentTypeMismatchException methodArgumentTypeMismatchException;

  @Test
  void shouldBuildMessageWithFieldNameForMethodArgumentNotValidExceptionWhenMessageIsEmpty() {
    var methodArgumentNotValidException = new MethodArgumentNotValidException((MethodParameter) null, bindingResult);
    given(bindingResult.getAllErrors()).willReturn(List.of(fieldError));
    given(fieldError.getField()).willReturn(FIELD_NAME);
    given(fieldError.getDefaultMessage()).willReturn(INVALID_SPECIAL_CHARACTERS_MESSAGE);

    var errorResponse = testedObject.
        handleFieldValidationException(methodArgumentNotValidException).getBody();

    var expectedErrors = List.of(
        ErrorResponse.ErrorDetail
            .builder()
            .field(FIELD_NAME)
            .message(String.format(INVALID_SPECIAL_CHARACTERS_MESSAGE, FIELD_NAME))
            .build());

    assertNotNull(errorResponse);
    assertThat(CollectionUtils.isEqualCollection(expectedErrors, errorResponse.getErrorDetails())).isTrue();
  }

  @Test
  void shouldReturnMessageForMethodArgumentNotValidExceptionWhenMessageIsNotEmpty() {
    var methodArgumentNotValidException = new MethodArgumentNotValidException((MethodParameter) null, bindingResult);
    given(bindingResult.getAllErrors()).willReturn(List.of(objectError));
    given(objectError.getDefaultMessage()).willReturn(MESSAGE);

    var errorResponse = testedObject.
        handleFieldValidationException(methodArgumentNotValidException).getBody();

    var expectedErrors = List.of(
        ErrorResponse.ErrorDetail
            .builder()
            .message(MESSAGE)
            .build());

    assertNotNull(errorResponse);
    assertThat(CollectionUtils.isEqualCollection(expectedErrors, errorResponse.getErrorDetails())).isTrue();
  }

  @Test
  void shouldBuildMessageWithFieldNameForConstraintViolationExceptionWhenPathContainsOneElement() {
    given(constraintViolationException.getConstraintViolations()).willReturn(Set.of(constraintViolation));
    given(constraintViolation.getMessage()).willReturn(INVALID_SPECIAL_CHARACTERS_MESSAGE);
    given(constraintViolation.getPropertyPath()).willReturn(path);
    given(constraintViolation.getMessageTemplate()).willReturn(TEMPLATE);
    given(path.toString()).willReturn(FIELD_NAME);

    var errorResponse = testedObject.
        handleParameterValidationException(constraintViolationException).getBody();

    var expectedErrors = List.of(
        new ErrorResponse.ErrorDetail(FIELD_NAME, String.format(INVALID_SPECIAL_CHARACTERS_MESSAGE, FIELD_NAME),
            TEMPLATE));

    assertNotNull(errorResponse);
    assertThat(CollectionUtils.isEqualCollection(expectedErrors, errorResponse.getErrorDetails())).isTrue();
  }

  @Test
  void shouldBuildMessageWithFieldNameForConstraintViolationExceptionWhenPathContainsTwoElement() {
    var propertyPath = FIELD_NAME + "." + FIELD_NAME;
    given(constraintViolationException.getConstraintViolations()).willReturn(Set.of(constraintViolation));
    given(constraintViolation.getMessage()).willReturn(INVALID_SPECIAL_CHARACTERS_MESSAGE);
    given(constraintViolation.getPropertyPath()).willReturn(path);
    given(constraintViolation.getMessageTemplate()).willReturn(TEMPLATE);
    given(path.toString()).willReturn(propertyPath);

    var errorResponse = testedObject.
        handleParameterValidationException(constraintViolationException).getBody();

    var expectedErrors = List.of(
        new ErrorResponse.ErrorDetail(propertyPath, String.format(INVALID_SPECIAL_CHARACTERS_MESSAGE, FIELD_NAME),
            TEMPLATE));

    assertNotNull(errorResponse);
    assertThat(CollectionUtils.isEqualCollection(expectedErrors, errorResponse.getErrorDetails())).isTrue();
  }

  @Test
  void shouldReturnMessageForConstraintViolationExceptionWhenMessageIsNotEmpty() {
    given(constraintViolationException.getConstraintViolations()).willReturn(Set.of(constraintViolation));
    given(constraintViolation.getMessage()).willReturn(MESSAGE);

    var errorResponse = testedObject.
        handleParameterValidationException(constraintViolationException).getBody();

    var expectedErrors = List.of(
        ErrorResponse.ErrorDetail
            .builder()
            .message(MESSAGE)
            .build());

    assertNotNull(errorResponse);
    assertThat(CollectionUtils.isEqualCollection(expectedErrors, errorResponse.getErrorDetails())).isTrue();
  }

  @Test
  void shouldReturnMessageWithListNameForConstraintViolationExceptionWhenMessageIsNotEmpty() {
    var propertyPath = FIELD_NAME + "." + FIELD_NAME + ".<list element>";
    given(constraintViolationException.getConstraintViolations()).willReturn(Set.of(constraintViolation));
    given(constraintViolation.getMessage()).willReturn(INVALID_SPECIAL_CHARACTERS_MESSAGE);
    given(constraintViolation.getPropertyPath()).willReturn(path);
    given(constraintViolation.getMessageTemplate()).willReturn(TEMPLATE);
    given(path.toString()).willReturn(propertyPath);

    var errorResponse = testedObject.
        handleParameterValidationException(constraintViolationException).getBody();

    var expectedErrors = List.of(
        new ErrorResponse.ErrorDetail(propertyPath, String.format(INVALID_SPECIAL_CHARACTERS_MESSAGE, FIELD_NAME),
            TEMPLATE));

    assertNotNull(errorResponse);
    assertThat(CollectionUtils.isEqualCollection(expectedErrors, errorResponse.getErrorDetails())).isTrue();
  }

  @Test
  void shouldReturnMessageWithIntegerInvalidValueForInvalidFormatExceptionWhenFieldIsNotEmpty() {
    JsonMappingException.Reference testClass = new JsonMappingException.Reference("TestClass", VARIABLE_NAME);

    given(invalidFormatException.getPath()).willReturn(List.of(testClass));
    doReturn(Integer.class).when(invalidFormatException).getTargetType();

    var errorResponse = testedObject
        .handleInvalidFormatOfInputParameterException(invalidFormatException)
        .getBody();

    var expectedErrors = List.of(
        ErrorResponse.ErrorDetail
            .builder()
            .field(VARIABLE_NAME)
            .message(String.format(INVALID_FIELD_FORMAT_MESSAGE, VARIABLE_NAME))
            .type(Integer.class.getSuperclass().getSimpleName())
            .build());

    assertThat(errorResponse).isNotNull();
    assertThat(errorResponse.getErrorDetails()).containsExactlyInAnyOrderElementsOf(expectedErrors);
  }

  @Test
  void shouldReturnMessageWithIntegerInvalidValueForInvalidFormatExceptionWhenFieldIsEmpty() {
    given(invalidFormatException.getPath()).willReturn(List.of(reference));
    given(reference.getFieldName()).willReturn(FIELD_NAME);
    doReturn(Integer.class).when(invalidFormatException).getTargetType();

    var errorResponse = testedObject
        .handleInvalidFormatOfInputParameterException(invalidFormatException)
        .getBody();

    var expectedErrors = List.of(
        ErrorResponse.ErrorDetail
            .builder()
            .field(FIELD_NAME)
            .message(String.format(INVALID_FIELD_FORMAT_MESSAGE, FIELD_NAME))
            .type(Integer.class.getSuperclass().getSimpleName())
            .build());

    assertThat(errorResponse).isNotNull();
    assertThat(errorResponse.getErrorDetails()).containsExactlyInAnyOrderElementsOf(expectedErrors);
  }

  @Test
  void shouldReturnMessageForJsonParseException() {
    given(jsonParseException.getOriginalMessage()).willReturn("json parse error");
    var errorResponse = testedObject
        .handleJsonParseException(jsonParseException)
        .getBody();

    var expectedErrors = List.of(
        ErrorResponse.ErrorDetail
            .builder()
            .message("json parse error")
            .type("JSON")
            .build());

    assertThat(errorResponse).isNotNull();
    assertThat(errorResponse.getErrorDetails()).containsExactlyInAnyOrderElementsOf(expectedErrors);
  }

  @Test
  void shouldBuildMessageForMismatchedInputException() {
    given(mismatchedInputException.getPath()).willReturn(List.of(reference));
    given(reference.getFieldName()).willReturn(FIELD_NAME);

    var errorResponse = testedObject
        .handleMismatchedInputException(mismatchedInputException)
        .getBody();

    var expectedErrors = List.of(
        ErrorResponse.ErrorDetail
            .builder()
            .field(FIELD_NAME)
            .message(String.format(INVALID_FIELD_FORMAT_MESSAGE, FIELD_NAME))
            .build());

    assertThat(errorResponse).isNotNull();
    assertThat(errorResponse.getErrorDetails()).containsExactlyInAnyOrderElementsOf(expectedErrors);
  }

  @Test
  void shouldReturnMessageForMethodArgumentTypeMismatchException() {
      given(methodArgumentTypeMismatchException.getName()).willReturn("size");

    var errorResponse = testedObject
        .handleMethodArgumentTypeMismatchException(methodArgumentTypeMismatchException)
        .getBody();

    var expectedErrors = List.of(
        ErrorResponse.ErrorDetail
            .builder()
            .field("size")
            .message("Failed to convert value of \"size\" to required type")
            .build());

    assertThat(errorResponse).isNotNull();
    assertThat(errorResponse.getErrorDetails()).containsExactlyInAnyOrderElementsOf(expectedErrors);
  }
}
