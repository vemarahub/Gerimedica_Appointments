package nl.gerimedica.assignment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nl.gerimedica.assignment.constants.AppointmentConstants;
import nl.gerimedica.assignment.exceptions.AppointmentException;
import nl.gerimedica.assignment.objects.dto.AppointmentRequestDTO;
import nl.gerimedica.assignment.objects.dto.AppointmentResponseDTO;
import nl.gerimedica.assignment.objects.dto.ErrorResponseDTO;
import nl.gerimedica.assignment.objects.entities.Appointment;
import nl.gerimedica.assignment.service.HospitalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing hospital appointments.
 * Provides endpoints for creating, retrieving, and managing appointments.
 */
@RestController
@RequestMapping(AppointmentConstants.ROUTE_PATH)
@Tag(name = "Appointment Management", description = "Endpoints for managing hospital appointments")
@Validated
@RequiredArgsConstructor
public class AppointmentController {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);

    private final HospitalService hospitalService;

    /**
     * Creates multiple appointments in bulk.
     * Example: {
     * "patientName" : "John Doe",
     * "ssn": "23454555",
     * "reasons": ["Checkup", "Follow-up", "X-Ray"],
     * "dates": ["2025-02-01", "2025-02-15", "2025-03-01"]
     * }
     *
     * @param request DTO containing lists of reasons and dates for appointments
     * @return ResponseEntity containing list of created appointments
     */
    @Operation(summary = "Create bulk appointments",
            description = "Creates multiple appointments with specified reasons and dates")
    @ApiResponse(responseCode = "201", description = "Appointments created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PostMapping(
            value = AppointmentConstants.BULK_APPOINTMENTS,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<AppointmentResponseDTO>> createBulkAppointments(
            @Valid @RequestBody AppointmentRequestDTO request) {
        logger.debug("REST request to create bulk appointments : {}", request);

        try {
            List<AppointmentResponseDTO> createdAppointments = hospitalService.createBulkAppointments(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(createdAppointments);
        } catch (AppointmentException e) {
            logger.error("Error creating bulk appointments", e);
            throw e;
        }
    }

    /**
     * Retrieves appointments by reason keyword.
     *
     * @param keyword The search keyword for appointment reason
     * @return List of matching appointments
     */
    @Operation(
            summary = "Get appointments by reason",
            description = "Retrieves all appointments matching the provided reason keyword"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved appointments",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = List.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid keyword provided",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No appointments found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping(
            value = AppointmentConstants.APPOINTMENT_BY_REASON,
            produces = MediaType.APPLICATION_JSON_VALUE
            )
    public ResponseEntity<List<Appointment>> getAppointmentsByReason( @RequestParam(required = true) String keyword) {
        logger.debug("Fetching appointments for reason containing: {}", keyword);
        List<Appointment> appointments = hospitalService.getAppointmentsByReason(keyword);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Deletes all appointments for a given SSN.
     *
     * @param ssn The Social Security Number
     * @return Confirmation message
     */
    @Operation(
            summary = "Delete appointments by SSN",
            description = "Deletes all appointments associated with the provided SSN"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Appointments successfully deleted",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = String.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid SSN format",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No appointments found for the SSN",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @PostMapping(AppointmentConstants.DELETE_APPOINTMENTS)
    public ResponseEntity<String> deleteAppointmentsBySSN(@RequestParam(required = true) String ssn) {
        logger.debug("Deleting appointments for SSN: {}", ssn);
        try {
            hospitalService.deleteAppointmentsBySSN(ssn);
            String message = String.format("Successfully deleted all appointments for SSN: %s", ssn);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            logger.error("Failed to delete appointments for SSN: {}", ssn, e);
            throw new AppointmentException("Failed to delete appointments: " + e.getMessage());
        }
    }

    /**
     * Retrieves the latest appointment for a given SSN.
     *
     * @param ssn The Social Security Number
     * @return The latest appointment
     */
    @Operation(
            summary = "Get latest appointment by SSN",
            description = "Retrieves the most recent appointment for the provided SSN"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved latest appointment",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Appointment.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid SSN format",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No appointments found for the SSN",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping(value=AppointmentConstants.LATEST_APPOINTMENTS,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Appointment> getLatestAppointment(@RequestParam(required = true) String ssn) {
        logger.debug("Fetching latest appointment for SSN: {}", ssn);
        try {
            Appointment latest = hospitalService.findLatestAppointmentBySSN(ssn);
            return ResponseEntity.ok(latest);
        } catch (Exception e) {
            logger.error("Failed to fetch latest appointment for SSN: {}", ssn, e);
            throw new AppointmentException("Failed to fetch latest appointment: " + e.getMessage());
        }
    }
}
