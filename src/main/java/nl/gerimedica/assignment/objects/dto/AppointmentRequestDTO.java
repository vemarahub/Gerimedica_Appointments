package nl.gerimedica.assignment.objects.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AppointmentRequestDTO {
    @NotNull
    private String patientName;
    @NotNull
    private String ssn;
    @NotNull
    private List<String> reasons;
    @NotNull
    @FutureOrPresent
    private List<LocalDate> dates;
}
