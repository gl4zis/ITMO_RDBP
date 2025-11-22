package ru.itmo.is.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;

@Getter
@Setter
public class PasswordChangeRequest {
    @NotBlank
    private String oldPassword;
    @NotBlank
    private String newPassword;
}
