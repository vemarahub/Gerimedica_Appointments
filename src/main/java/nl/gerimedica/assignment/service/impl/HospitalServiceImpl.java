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
        List<Appointment> appointments = new ArrayList<>();
        List<AppointmentResponseDTO> responseDTOs = new ArrayList<>();

        try {
            if (request == null || request.getReasons().isEmpty() || request.getDates().isEmpty()) {
                logger.warn("Invalid request: Missing data for appointment creation.");
                throw new IllegalArgumentException("Request data is incomplete");
            }

            Patient patient = findPatientBySSN(request.getSsn());
            if (patient == null) {
                logger.info("Creating new patient with SSN: {}", request.getSsn());
                savePatient(new Patient(request.getPatientName(), request.getSsn()));
            } else {
                logger.info("Existing patient found, SSN: {}", request.getSsn());
            }

            int loopSize = Math.min(request.getReasons().size(), request.getDates().size());
            appointments = IntStream.range(0, loopSize)
                    .mapToObj(i -> new Appointment(request.getReasons().get(i), request.getDates().get(i).format(DATE_FORMATTER), patient))
                    .collect(Collectors.toList());

            appointmentRepository.saveAll(appointments);

            appointments.forEach(appointment ->
                    logger.info("Created appointment for reason: {} [Date: {}]",
                            appointment.getReason(), appointment.getDate()));

            HospitalUtils.recordUsage("Bulk create appointments");
            responseDTOs = AppointmentMapper.toDtoList(appointments);


        } catch (IllegalArgumentException e) {
            logger.error("Appointment creation failed due to invalid input: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during appointment creation: {}", e.getMessage(), e);
        }

        return responseDTOs;

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
