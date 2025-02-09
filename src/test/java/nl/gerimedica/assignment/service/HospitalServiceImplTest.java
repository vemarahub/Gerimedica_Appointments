package nl.gerimedica.assignment.service;

import nl.gerimedica.assignment.objects.dto.AppointmentRequestDTO;
import nl.gerimedica.assignment.objects.dto.AppointmentResponseDTO;
import nl.gerimedica.assignment.objects.entities.Appointment;
import nl.gerimedica.assignment.objects.entities.Patient;
import nl.gerimedica.assignment.repository.AppointmentRepository;
import nl.gerimedica.assignment.repository.PatientRepository;
import nl.gerimedica.assignment.service.impl.HospitalServiceImpl;
import nl.gerimedica.assignment.utils.HospitalUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HospitalServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private HospitalUtils hospitalUtils;

    @InjectMocks
    private HospitalServiceImpl hospitalService;

    private AppointmentRequestDTO requestDTO;
    private AppointmentResponseDTO responseDTO;
    private Patient patient;
    private Appointment appointment;

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

        patient = new Patient("John Doe", "123-45-6789");
        appointment = new Appointment("Checkup", "2023-10-01", patient);
    }

    // Test for createBulkAppointments
    @Test
    public void testCreateBulkAppointments_Success() {
        // Arrange
        when(patientRepository.findBySsn(any(String.class))).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);
        when(appointmentRepository.saveAll(any(List.class))).thenReturn(Collections.singletonList(appointment));

        // Act
        List<AppointmentResponseDTO> result = hospitalService.createBulkAppointments(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Checkup", result.get(0).getReason());
        verify(patientRepository, times(1)).findBySsn(any(String.class));
        verify(patientRepository, times(1)).save(any(Patient.class));
        verify(appointmentRepository, times(1)).saveAll(any(List.class));
    }

    @Test
    public void testCreateBulkAppointments_InvalidRequest() {
        // Arrange
        requestDTO.setReasons(Collections.emptyList()); // Invalid input

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> hospitalService.createBulkAppointments(requestDTO));
        verify(patientRepository, never()).findBySsn(any(String.class));
        verify(patientRepository, never()).save(any(Patient.class));
        verify(appointmentRepository, never()).saveAll(any(List.class));
    }

    // Test for getAppointmentsByReason
    @Test
    public void testGetAppointmentsByReason_Success() {
        // Arrange
        when(appointmentRepository.findAll()).thenReturn(Collections.singletonList(appointment));

        // Act
        List<AppointmentResponseDTO> result = hospitalService.getAppointmentsByReason("Checkup");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Checkup", result.get(0).getReason());
        verify(appointmentRepository, times(1)).findAll();
    }

    @Test
    public void testGetAppointmentsByReason_InvalidKeyword() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> hospitalService.getAppointmentsByReason(null));
        verify(appointmentRepository, never()).findAll();
    }

    // Test for findLatestAppointmentBySSN
    @Test
    public void testFindLatestAppointmentBySSN_Success() {
        // Arrange
        patient.setAppointments(Collections.singletonList(appointment));
        when(patientRepository.findAll()).thenReturn(Collections.singletonList(patient));

        // Act
        AppointmentResponseDTO result = hospitalService.findLatestAppointmentBySSN("123-45-6789");

        // Assert
        assertNotNull(result);
        assertEquals("Checkup", result.getReason());
        verify(patientRepository, times(1)).findAll();
    }

    @Test
    public void testFindLatestAppointmentBySSN_NoAppointments() {
        // Arrange
        when(patientRepository.findAll()).thenReturn(Collections.singletonList(patient));

        // Act
        AppointmentResponseDTO result = hospitalService.findLatestAppointmentBySSN("123-45-6789");

        // Assert
        assertNull(result);
        verify(patientRepository, times(1)).findAll();
    }

    // Test for deleteAppointmentsBySSN
    @Test
    public void testDeleteAppointmentsBySSN_Success() {
        // Arrange
        patient.setAppointments(Collections.singletonList(appointment));
        when(patientRepository.findAll()).thenReturn(Collections.singletonList(patient));
        doNothing().when(appointmentRepository).deleteAll(any(List.class));

        // Act
        hospitalService.deleteAppointmentsBySSN("123-45-6789");

        // Assert
        verify(patientRepository, times(1)).findAll();
        verify(appointmentRepository, times(1)).deleteAll(any(List.class));
    }

    @Test
    public void testDeleteAppointmentsBySSN_NoPatient() {
        // Arrange
        when(patientRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        hospitalService.deleteAppointmentsBySSN("123-45-6789");

        // Assert
        verify(patientRepository, times(1)).findAll();
        verify(appointmentRepository, never()).deleteAll(any(List.class));
    }

    @Test
    public void testDeleteAppointmentsBySSN_NoAppointments() {
        // Arrange
        when(patientRepository.findAll()).thenReturn(Collections.singletonList(patient));

        // Act
        String s = "123-45-6789";


        // Assert
        verify(patientRepository, times(1)).findAll();
        verify(appointmentRepository, never()).deleteAll(any(List.class));
    }
}