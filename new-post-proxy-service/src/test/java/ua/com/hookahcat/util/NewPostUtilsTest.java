package ua.com.hookahcat.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ua.com.hookahcat.util.Constants.FileNames.NOT_RECEIVED_PARCELS;
import static ua.com.hookahcat.util.Constants.Patterns.DATE_PATTERN;
import static ua.com.hookahcat.util.NewPostUtils.createCsvFile;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NewPostUtilsTest {

    @Test
    void testCreateCsvFile() {
        File file = createCsvFile("mockedCsvData".getBytes(), NOT_RECEIVED_PARCELS);

        assertNotNull(file);
        assertTrue(file.exists());
        assertEquals("Not received parcels [" + LocalDate.now()
            .format(DateTimeFormatter.ofPattern(DATE_PATTERN)) + "].csv", file.getName());
    }
}
