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
import java.util.List;
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
                    logger.info("Created appointment for reason: {} [Date: {}] [Patient SSN: {}]",
                            appointment.getReason(), appointment.getDate(), appointment.getPatient().getSsn()));

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
    public List<Appointment> getAppointmentsByReason(String reasonKeyword) {
        return null;
    }

    @Override
    public Appointment findLatestAppointmentBySSN(String ssn) {
        return null;
    }

    @Override
    public void deleteAppointmentsBySSN(String ssn) {

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
