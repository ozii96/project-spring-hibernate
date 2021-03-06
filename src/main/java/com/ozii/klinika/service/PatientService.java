package com.ozii.klinika.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ozii.klinika.entity.Patient;
import com.ozii.klinika.entity.PatientExam;

@Service
public interface PatientService {

	public List<Patient> getPatient();

	public void savePatient(Patient thePatient);

	public Patient getPatient(int theId);

	public void deletePatient(int theId);

	public List<Patient> searchPatients(String theSearchName);
	
	public void addPatientExam(int theId, PatientExam patientExam);

	public List<PatientExam> getPatientExams(int theId);
	
	public int getPatientId (String pesel);
}
