package org.avni.server.web.api;

import org.avni.server.dao.ConceptRepository;
import org.avni.server.dao.GroupSubjectRepository;
import org.avni.server.domain.GroupSubject;
import org.avni.server.domain.accessControl.PrivilegeType;
import org.avni.server.service.ConceptService;
import org.avni.server.service.S3Service;
import org.avni.server.service.accessControl.AccessControlService;
import org.avni.server.util.S;
import org.avni.server.web.request.api.SubjectResponseOptions;
import org.avni.server.web.response.GroupSubjectResponse;
import org.avni.server.web.response.ResponsePage;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
public class GroupSubjectApiController {

    private final GroupSubjectRepository groupSubjectRepository;
    private final ConceptRepository conceptRepository;
    private final ConceptService conceptService;
    private final S3Service s3Service;
    private final AccessControlService accessControlService;

    @Autowired
    public GroupSubjectApiController(GroupSubjectRepository groupSubjectRepository,
                                     ConceptRepository conceptRepository, ConceptService conceptService,
                                     S3Service s3Service, AccessControlService accessControlService) {
        this.groupSubjectRepository = groupSubjectRepository;
        this.conceptRepository = conceptRepository;
        this.conceptService = conceptService;
        this.s3Service = s3Service;
        this.accessControlService = accessControlService;
    }

    @RequestMapping(value = "/api/groupSubjects", method = RequestMethod.GET)
    @PreAuthorize(value = "hasAnyAuthority('user')")
    public Object getSubjects(@RequestParam(value = "lastModifiedDateTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) DateTime lastModifiedDateTime,
                              @RequestParam("now") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) DateTime now,
                              @RequestParam(value = "groupSubjectId", required = false) String groupSubjectUUID,
                              @RequestParam(value = "memberSubjectId", required = false) String memberSubjectUUID,
                              @RequestParam(value = "includeCatchments", required = false) Boolean includeCatchments,
                              Pageable pageable) {
        Page<GroupSubject> groupSubjects;
        if (!S.isEmpty(groupSubjectUUID) && lastModifiedDateTime == null) {
            groupSubjects = groupSubjectRepository.findByGroupSubjectUuidOrderByLastModifiedDateTimeAscIdAsc(groupSubjectUUID, pageable);
        } else if (!S.isEmpty(memberSubjectUUID) && lastModifiedDateTime == null) {
            groupSubjects = groupSubjectRepository.findByMemberSubjectUuidOrderByLastModifiedDateTimeAscIdAsc(memberSubjectUUID, pageable);
        } else if (lastModifiedDateTime != null) {
            groupSubjects = groupSubjectRepository.findByLastModifiedDateTimeIsBetweenOrderByLastModifiedDateTimeAscIdAsc(lastModifiedDateTime, now, pageable);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        accessControlService.checkGroupSubjectPrivileges(PrivilegeType.ViewSubject, groupSubjects.getContent());
        ArrayList<GroupSubjectResponse> groupSubjectResponses = new ArrayList<>();
        groupSubjects.forEach(groupSubject -> groupSubjectResponses.add(GroupSubjectResponse.fromGroupSubject(groupSubject, conceptRepository, conceptService, s3Service, SubjectResponseOptions.forSingleSubject(includeCatchments))));
        return new ResponsePage(groupSubjectResponses, groupSubjects.getNumberOfElements(), groupSubjects.getTotalPages(), groupSubjects.getSize());
    }
}
