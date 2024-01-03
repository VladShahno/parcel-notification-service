package ua.com.hookahcat.notification.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.File;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailNotificationData {

    @NotBlank(message = "{not.blank}")
    private String subject;

    @NotBlank(message = "{not.blank}")
    @Email
    private String recipient;

    @NotBlank(message = "{not.blank}")
    @Email
    private String sender;

    @NotBlank(message = "{not.blank}")
    private String message;

    @NotNull(message = "{not.null}")
    private File file;
}
