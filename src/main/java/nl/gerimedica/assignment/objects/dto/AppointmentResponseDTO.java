package nl.gerimedica.assignment.objects.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AppointmentResponseDTO {
    private Long id;
    private String reason;
    private LocalDate date;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
