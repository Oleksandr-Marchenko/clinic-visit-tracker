package com.marchenko.clinicvisittracker.perf;

import com.marchenko.clinicvisittracker.dto.PagedResponseDto;
import com.marchenko.clinicvisittracker.dto.PatientResponseDto;
import com.marchenko.clinicvisittracker.entity.Doctor;
import com.marchenko.clinicvisittracker.entity.Patient;
import com.marchenko.clinicvisittracker.entity.Visit;
import com.marchenko.clinicvisittracker.repository.DoctorRepository;
import com.marchenko.clinicvisittracker.repository.PatientRepository;
import com.marchenko.clinicvisittracker.repository.VisitRepository;
import com.marchenko.clinicvisittracker.service.PatientService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
public class PerformanceIT {

	@Autowired
	private DoctorRepository doctorRepository;

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private VisitRepository visitRepository;

	@Autowired
	private PatientService patientService;

	private static final MySQLContainer<?> MYSQL = new MySQLContainer<>(DockerImageName.parse("mysql:8.4"))
			.withDatabaseName("clinic_perf")
			.withUsername("test")
			.withPassword("test");

	static {
		MYSQL.start();
	}

	@DynamicPropertySource
	static void registerProps(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
		registry.add("spring.datasource.username", MYSQL::getUsername);
		registry.add("spring.datasource.password", MYSQL::getPassword);
		registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
		registry.add("spring.liquibase.enabled", () -> "true");
		registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/db.changelog-master.yaml");
	}

	@BeforeEach
	void prepare() {
		visitRepository.deleteAll();
		doctorRepository.deleteAll();
		patientRepository.deleteAll();
	}

	@Test
	void bulkLoadAndQueryLoops() {
		int doctors = Integer.getInteger("perf.doctors", 200);
		int patients = Integer.getInteger("perf.patients", 5000);
		int visitsPerPatient = Integer.getInteger("perf.visitsPerPatient", 5);

		List<Doctor> doctorList = new ArrayList<>(doctors);
		for (int i = 0; i < doctors; i++) {
			doctorList.add(new Doctor(null, "D" + i, "L" + i, "Europe/Kyiv"));
		}
		doctorList = doctorRepository.saveAll(doctorList);

		List<Patient> patientList = new ArrayList<>(patients);
		for (int i = 0; i < patients; i++) {
			patientList.add(new Patient(null, "P" + i, "LN" + i));
		}
		patientList = patientRepository.saveAll(patientList);

		ThreadLocalRandom rnd = ThreadLocalRandom.current();
		List<Visit> batch = new ArrayList<>(patients * visitsPerPatient);
		LocalDateTime base = LocalDateTime.of(2025, 1, 1, 9, 0);
		for (Patient p : patientList) {
			for (int j = 0; j < visitsPerPatient; j++) {
				Doctor d = doctorList.get(rnd.nextInt(doctorList.size()));
				LocalDateTime start = base.plusDays(rnd.nextInt(0, 60)).plusHours(rnd.nextInt(0, 6));
				LocalDateTime end = start.plusHours(1);
				batch.add(new Visit(null, start, end, p, d));
				if (batch.size() % 5000 == 0) {
					visitRepository.saveAll(batch);
					batch.clear();
				}
			}
		}
		if (!batch.isEmpty()) visitRepository.saveAll(batch);

		org.springframework.util.StopWatch sw = new org.springframework.util.StopWatch("getPatients-loop");
		sw.start();
		for (int page = 0; page < 5; page++) {
			PagedResponseDto<PatientResponseDto> res = patientService.getPatients(null, null, page, 50);
			assertThat(res.getData()).isNotEmpty();
		}
		sw.stop();
		log.info("getPatients loop took ms: {}",  sw.getTotalTimeMillis());
	}
}
