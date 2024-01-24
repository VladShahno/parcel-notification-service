package ua.com.hookahcat.reststarter.restservicestarter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ua.com.hookahcat.reststarter.exception.BadRequestException;
import ua.com.hookahcat.reststarter.exception.DuplicateResourceException;
import ua.com.hookahcat.reststarter.exception.ResourceNotFoundException;

class ExceptionTest {

    public static final String message = "message";

    @Test
    void createBadRequestExceptionSuccessful1() {
        final BadRequestException exception = new BadRequestException(message, new Exception());
        Assertions.assertEquals(message, exception.getMessage());
        Assertions.assertNotNull(exception.getCause());
    }

    @Test
    void createBadRequestExceptionSuccessful2() {
        final BadRequestException exception = new BadRequestException(message, new Object[]{},
            new Exception());
        Assertions.assertEquals(message, exception.getMessage());
        Assertions.assertNotNull(exception.getArgs());
        Assertions.assertNotNull(exception.getCause());
    }

    @Test
    void createDuplicateResourceExceptionSuccessful1() {
        final DuplicateResourceException exception = new DuplicateResourceException(message,
            new Exception());
        Assertions.assertEquals(message, exception.getMessage());
        Assertions.assertNotNull(exception.getCause());
    }

    @Test
    void createDuplicateResourceExceptionSuccessful2() {
        final DuplicateResourceException exception = new DuplicateResourceException(message,
            new Object[]{},
            new Exception());
        Assertions.assertEquals(message, exception.getMessage());
        Assertions.assertNotNull(exception.getArgs());
        Assertions.assertNotNull(exception.getCause());
    }

    @Test
    void createResourceNotFoundExceptionSuccessful1() {
        final ResourceNotFoundException exception = new ResourceNotFoundException(message,
            new Exception());
        Assertions.assertEquals(message, exception.getMessage());
        Assertions.assertNotNull(exception.getCause());
    }

    @Test
    void createResourceNotFoundExceptionSuccessful2() {
        final ResourceNotFoundException exception = new ResourceNotFoundException(message,
            new Object[]{},
            new Exception());
        Assertions.assertEquals(message, exception.getMessage());
        Assertions.assertNotNull(exception.getArgs());
        Assertions.assertNotNull(exception.getCause());
    }
}
