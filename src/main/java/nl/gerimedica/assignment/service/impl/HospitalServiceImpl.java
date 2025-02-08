package nl.gerimedica.assignment.service.impl;

import lombok.RequiredArgsConstructor;
import nl.gerimedica.assignment.objects.dto.AppointmentRequestDTO;
import nl.gerimedica.assignment.objects.dto.AppointmentResponseDTO;
import nl.gerimedica.assignment.objects.entities.Appointment;
import nl.gerimedica.assignment.service.HospitalService;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class HospitalServiceImpl implements HospitalService {
    @Override
    public List<AppointmentResponseDTO> createBulkAppointments(AppointmentRequestDTO request) {
        return null;
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
}
