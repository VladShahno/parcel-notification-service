package ua.com.hookahcat.util;

import static ua.com.hookahcat.util.Constants.Patterns.DATE_PATTERN;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import ua.com.hookahcat.reststarter.exception.CustomRuntimeException;

@UtilityClass
public class NewPostUtils {

    public static File createCsvFile(byte[] exportedParcelsData, String fileName) {
        if (Objects.nonNull(exportedParcelsData)) {
            var todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN));
            var pathName = fileName + todayDate + "].csv";
            var file = new File(pathName);
            try {
                FileUtils.writeByteArrayToFile(file, exportedParcelsData);
            } catch (IOException e) {
                throw new CustomRuntimeException(e.getMessage());
            }
            return file;
        }
        return null;
    }
}
