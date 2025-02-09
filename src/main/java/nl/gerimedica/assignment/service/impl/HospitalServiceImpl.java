package nl.gerimedica.assignment.service.impl;

import lombok.RequiredArgsConstructor;
import nl.gerimedica.assignment.exceptions.AppointmentException;
import nl.gerimedica.assignment.mapper.AppointmentMapper;
import nl.gerimedica.assignment.objects.dto.AppointmentRequestDTO;
import nl.gerimedica.assignment.objects.dto.AppointmentResponseDTO;
import nl.gerimedica.assignment.objects.entities.Appointment;
import nl.gerimedica.assignment.objects.entities.Patient;
import nl.gerimedica.assignment.repository.AppointmentRepository;
import nl.gerimedica.assignment.repository.PatientRepository;
import nl.gerimedica.assignment.service.HospitalService;
import nl.gerimedica.assignment.utils.HospitalUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Implementation of the Hospital Service interface.
 * Handles business logic for appointment management.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class HospitalServiceImpl implements HospitalService {

    private static final Logger logger = LoggerFactory.getLogger(HospitalServiceImpl.class);

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    /**
     * Creates multiple appointments in bulk.
     *
     * @param request DTO containing appointment details
     * @return List of created appointment responses
     * @throws AppointmentException if creation fails
     */
    @Override
    @Transactional
    public List<AppointmentResponseDTO> createBulkAppointments(AppointmentRequestDTO request) {
        logger.debug("Creating bulk appointments for request: {}", request);

        // Validate the request
        validateAppointmentRequest(request);

        // Find or create the patient
        Patient patient = findOrCreatePatient(request.getSsn(), request.getPatientName());

        // Create appointments
        List<Appointment> appointments = createAppointments(request, patient);

        try {
            // Save all appointments
            appointmentRepository.saveAll(appointments);
            logger.info("Successfully created {} appointments for patient with SSN: {}", appointments.size(), patient.getSsn());

            // Record usage
            HospitalUtils.recordUsage("Bulk create appointments");

            // Map appointments to DTOs
            return AppointmentMapper.toDtoList(appointments);
        } catch (Exception e) {
            logger.error("Failed to create appointments for patient with SSN: {}", patient.getSsn(), e);
            throw new AppointmentException("Failed to create appointments: " + e.getMessage());
        }
    }

    /**
     * Validates the appointment request.
     *
     * @param request The appointment request DTO
     * @throws IllegalArgumentException if the request is invalid
     */
    private void validateAppointmentRequest(AppointmentRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getSsn() == null || request.getSsn().trim().isEmpty()) {
            throw new IllegalArgumentException("SSN cannot be null or empty");
        }
        if (request.getPatientName() == null || request.getPatientName().trim().isEmpty()) {
            throw new IllegalArgumentException("Patient name cannot be null or empty");
        }
        if (request.getReasons() == null || request.getReasons().isEmpty()) {
            throw new IllegalArgumentException("Reasons cannot be null or empty");
        }
        if (request.getDates() == null || request.getDates().isEmpty()) {
            throw new IllegalArgumentException("Dates cannot be null or empty");
        }
    }

    /**
     * Finds or creates a patient.
     *
     * @param ssn         The patient's SSN
     * @param patientName The patient's name
     * @return The patient entity
     */
    private Patient findOrCreatePatient(String ssn, String patientName) {
        return patientRepository.findBySsn(ssn)
                .orElseGet(() -> {
                    logger.info("Creating new patient with SSN: {}", ssn);
                    return patientRepository.save(new Patient(patientName, ssn));
                });
    }

    /**
     * Creates a list of appointments from the request.
     *
     * @param request The appointment request DTO
     * @param patient The patient entity
     * @return A list of appointments
     */
    private List<Appointment> createAppointments(AppointmentRequestDTO request, Patient patient) {
        int loopSize = Math.min(request.getReasons().size(), request.getDates().size());
        return IntStream.range(0, loopSize)
                .mapToObj(i -> new Appointment(request.getReasons().get(i), request.getDates().get(i).format(DATE_FORMATTER), patient))
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentResponseDTO> getAppointmentsByReason(String reasonKeyword) {
        if (reasonKeyword == null || reasonKeyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason keyword cannot be null or empty");
        }

        List<Appointment> matchedAppointments = appointmentRepository.findAll().stream()
                .filter(ap -> ap.getReason() != null && ap.getReason().equalsIgnoreCase(reasonKeyword))
                .collect(Collectors.toList());

        HospitalUtils.recordUsage("Get appointments by reason");
        List<AppointmentResponseDTO> responseDTOs = AppointmentMapper.toDtoList(matchedAppointments);
        return responseDTOs;
    }


    @Override
    public AppointmentResponseDTO findLatestAppointmentBySSN(String ssn) {
        Appointment appointment =  Optional.ofNullable(findPatientBySSN(ssn))
                .map(patient -> patient.appointments)
                .filter(appointments -> !appointments.isEmpty())
                .flatMap(appointments -> appointments.stream()
                        .max(Comparator.comparing(appt -> appt.date)))
                .orElse(null);

        AppointmentResponseDTO responseDTO = AppointmentMapper.toDto(appointment);

        return responseDTO;
    }

    @Override
    public void deleteAppointmentsBySSN(String ssn) {
        // Find the patient by SSN
        Patient patient = findPatientBySSN(ssn);
        if (patient == null) {
            logger.warn("No patient found with SSN: {}", ssn);
            return;
        }

        // Check if the patient has any appointments
        List<Appointment> appointments = patient.getAppointments(); // Use a getter instead of direct field access
        if (appointments == null || appointments.isEmpty()) {
            logger.info("No appointments found for patient with SSN: {}", ssn);
            return;
        }

        // Delete all appointments
        try {
            appointmentRepository.deleteAll(appointments);
            logger.info("Successfully deleted {} appointments for patient with SSN: {}", appointments.size(), ssn);
        } catch (Exception e) {
            logger.error("Failed to delete appointments for patient with SSN: {}", ssn, e);
            throw new RuntimeException("Failed to delete appointments: " + e.getMessage(), e);
        }
    }

    public Patient findPatientBySSN(String ssn) {
        List<Patient> patientList = patientRepository.findAll();
        for (Patient patient : patientList) {
            if (patient.ssn.equals(ssn)) {
                return patient;
            }
        }
        return null;
    }

    @Transactional
    void savePatient(Patient patient) {
        patientRepository.save(patient);
    }
}
