package org.openchs.web.response;

import org.openchs.dao.ConceptRepository;
import org.openchs.domain.ProgramEnrolment;
import org.openchs.service.ConceptService;

import java.util.LinkedHashMap;

public class ProgramEnrolmentResponse extends LinkedHashMap<String, Object> {
    public static ProgramEnrolmentResponse fromProgramEnrolment(ProgramEnrolment programEnrolment, ConceptRepository conceptRepository, ConceptService conceptService) {
        ProgramEnrolmentResponse programEnrolmentResponse = new ProgramEnrolmentResponse();
        programEnrolmentResponse.put("ID", programEnrolment.getUuid());
        programEnrolmentResponse.put("Subject type", programEnrolment.getIndividual().getSubjectType().getName());
        programEnrolmentResponse.put("Program", programEnrolment.getProgram().getName());
        programEnrolmentResponse.put("Enrolment date-time", programEnrolment.getEnrolmentDateTime());
        programEnrolmentResponse.put("Exit date-time", programEnrolment.getProgramExitDateTime());

        programEnrolmentResponse.put("Subject ID", programEnrolment.getIndividual().getUuid());
        Response.putIfPresent(programEnrolmentResponse, "Enrolment location", programEnrolment.getEnrolmentLocation());
        Response.putIfPresent(programEnrolmentResponse, "Exit location", programEnrolment.getExitLocation());

        LinkedHashMap<String, Object> observationsResponse = new LinkedHashMap<>();
        Response.putObservations(conceptRepository, conceptService, programEnrolmentResponse, observationsResponse, programEnrolment.getObservations());
        Response.putObservations(conceptRepository, conceptService, programEnrolmentResponse, observationsResponse, programEnrolment.getObservations(), "exitObservations");
        Response.putAudit(programEnrolment, programEnrolmentResponse);
        return programEnrolmentResponse;
    }
}