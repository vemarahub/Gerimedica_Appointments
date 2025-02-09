package nl.gerimedica.assignment.repository;

import nl.gerimedica.assignment.objects.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findBySsn(String ssn);
}
