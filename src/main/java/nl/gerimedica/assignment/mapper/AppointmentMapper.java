package nl.gerimedica.assignment.mapper;

import nl.gerimedica.assignment.objects.dto.AppointmentResponseDTO;
import nl.gerimedica.assignment.objects.entities.Appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AppointmentMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static AppointmentResponseDTO toDto(Appointment appointment) {
        if (appointment == null) {
            return null;
        }

        AppointmentResponseDTO dto = new AppointmentResponseDTO();
        dto.setId(appointment.getId());
        dto.setReason(appointment.getReason());

        // Convert String date to LocalDate
        dto.setDate(parseLocalDate(appointment.getDate()));

        // Set createdAt & updatedAt (assuming entity has these fields)
        dto.setCreatedAt(LocalDateTime.now()); // Replace with actual timestamps if available
        dto.setUpdatedAt(LocalDateTime.now());

        return dto;
    }

    public static List<AppointmentResponseDTO> toDtoList(List<Appointment> appointments) {
        return appointments.stream().map(AppointmentMapper::toDto).collect(Collectors.toList());
    }

    private static LocalDate parseLocalDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format: " + dateStr, e);
        }
    }
}
