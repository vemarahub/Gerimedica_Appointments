package nl.gerimedica.assignment.service;

import nl.gerimedica.assignment.objects.dto.AppointmentRequestDTO;
import nl.gerimedica.assignment.objects.dto.AppointmentResponseDTO;
import nl.gerimedica.assignment.objects.entities.Appointment;
import nl.gerimedica.assignment.objects.entities.Patient;

import java.util.List;

public interface HospitalService {

    List<AppointmentResponseDTO> createBulkAppointments(AppointmentRequestDTO request);
    List<Appointment> getAppointmentsByReason(String reasonKeyword);
    Appointment findLatestAppointmentBySSN(String ssn);
    void deleteAppointmentsBySSN(String ssn);
}
