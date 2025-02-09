package nl.gerimedica.assignment.objects.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(description = "Error response details")
@Getter
@Setter
public class ErrorResponseDTO {
    @Schema(description = "Error message")
    private String message;

    @Schema(description = "Error code")
    private String errorCode;

    @Schema(description = "Timestamp of the error")
    private LocalDateTime timestamp;

}
