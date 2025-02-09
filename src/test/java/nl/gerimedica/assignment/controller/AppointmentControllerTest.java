package nl.gerimedica.assignment.controller;

import nl.gerimedica.assignment.exceptions.AppointmentException;
import nl.gerimedica.assignment.objects.dto.AppointmentRequestDTO;
import nl.gerimedica.assignment.objects.dto.AppointmentResponseDTO;
import nl.gerimedica.assignment.service.HospitalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentControllerTest {

    @Mock
    private HospitalService hospitalService;

    @InjectMocks
    private AppointmentController appointmentController;

    private AppointmentRequestDTO requestDTO;
    private AppointmentResponseDTO responseDTO;

    @BeforeEach
    public void setUp() {
        requestDTO = new AppointmentRequestDTO();
        requestDTO.setPatientName("John Doe");
        requestDTO.setSsn("123-45-6789");
        requestDTO.setReasons(Arrays.asList("Checkup", "Vaccination"));
        requestDTO.setDates(Arrays.asList(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 11, 1)));

        responseDTO = new AppointmentResponseDTO();
        responseDTO.setReason("Checkup");
        responseDTO.setDate(LocalDate.parse("2023-10-01"));
    }

    // Test for createBulkAppointments
    @Test
    public void testCreateBulkAppointments_Success() {
        // Arrange
        List<AppointmentResponseDTO> responseDTOs = Collections.singletonList(responseDTO);
        when(hospitalService.createBulkAppointments(any(AppointmentRequestDTO.class))).thenReturn(responseDTOs);

        // Act
        ResponseEntity<List<AppointmentResponseDTO>> response = appointmentController.createBulkAppointments(requestDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Checkup", response.getBody().get(0).getReason());
        verify(hospitalService, times(1)).createBulkAppointments(any(AppointmentRequestDTO.class));
    }

    @Test
    public void testCreateBulkAppointments_InvalidInput() {
        // Arrange
        requestDTO.setReasons(Collections.emptyList()); // Invalid input
        when(hospitalService.createBulkAppointments(any(AppointmentRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Invalid input"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> appointmentController.createBulkAppointments(requestDTO));
        verify(hospitalService, times(1)).createBulkAppointments(any(AppointmentRequestDTO.class));
    }

    // Test for getAppointmentsByReason
    @Test
    public void testGetAppointmentsByReason_Success() {
        // Arrange
        List<AppointmentResponseDTO> responseDTOs = Collections.singletonList(responseDTO);
        when(hospitalService.getAppointmentsByReason("Checkup")).thenReturn(responseDTOs);

        // Act
        ResponseEntity<List<AppointmentResponseDTO>> response = appointmentController.getAppointmentsByReason("Checkup");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Checkup", response.getBody().get(0).getReason());
        verify(hospitalService, times(1)).getAppointmentsByReason("Checkup");
    }

    @Test
    public void testGetAppointmentsByReason_NoResults() {
        // Arrange
        when(hospitalService.getAppointmentsByReason("Checkup")).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<AppointmentResponseDTO>> response = appointmentController.getAppointmentsByReason("Checkup");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(hospitalService, times(1)).getAppointmentsByReason("Checkup");
    }

    // Test for deleteAppointmentsBySSN
    @Test
    public void testDeleteAppointmentsBySSN_Success() {
        // Arrange
        doNothing().when(hospitalService).deleteAppointmentsBySSN("123-45-6789");

        // Act
        ResponseEntity<String> response = appointmentController.deleteAppointmentsBySSN("123-45-6789");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully deleted all appointments for SSN: 123-45-6789", response.getBody());
        verify(hospitalService, times(1)).deleteAppointmentsBySSN("123-45-6789");
    }

    @Test
    public void testDeleteAppointmentsBySSN_Failure() {
        // Arrange
        doThrow(new AppointmentException("Failed to delete appointments")).when(hospitalService).deleteAppointmentsBySSN("123-45-6789");

        // Act & Assert
        assertThrows(AppointmentException.class, () -> appointmentController.deleteAppointmentsBySSN("123-45-6789"));
        verify(hospitalService, times(1)).deleteAppointmentsBySSN("123-45-6789");
    }

    // Test for getLatestAppointment
    @Test
    public void testGetLatestAppointment_Success() {
        // Arrange
        when(hospitalService.findLatestAppointmentBySSN("123-45-6789")).thenReturn(responseDTO);

        // Act
        ResponseEntity<AppointmentResponseDTO> response = appointmentController.getLatestAppointment("123-45-6789");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Checkup", response.getBody().getReason());
        verify(hospitalService, times(1)).findLatestAppointmentBySSN("123-45-6789");
    }

    @Test
    public void testGetLatestAppointment_NotFound() {
        // Arrange
        when(hospitalService.findLatestAppointmentBySSN("123-45-6789")).thenReturn(null);

        // Act
        ResponseEntity<AppointmentResponseDTO> response = appointmentController.getLatestAppointment("123-45-6789");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(hospitalService, times(1)).findLatestAppointmentBySSN("123-45-6789");
    }
}