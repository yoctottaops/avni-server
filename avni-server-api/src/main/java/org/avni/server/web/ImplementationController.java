package org.avni.server.web;

import org.avni.server.domain.Concept;
import org.avni.server.domain.Organisation;
import org.avni.server.domain.accessControl.PrivilegeType;
import org.avni.server.domain.metadata.OrganisationCategory;
import org.avni.server.framework.security.UserContextHolder;
import org.avni.server.service.OrganisationService;
import org.avni.server.service.accessControl.AccessControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipOutputStream;

import static org.avni.server.domain.accessControl.PrivilegeType.MultiTxEntityTypeUpdate;

@RestController
public class ImplementationController implements RestControllerResourceProcessor<Concept> {

    private final OrganisationService organisationService;
    private final AccessControlService accessControlService;

    @Autowired
    public ImplementationController(OrganisationService organisationService, AccessControlService accessControlService) {
        this.organisationService = organisationService;
        this.accessControlService = accessControlService;
    }

    @RequestMapping(value = "/implementation/export/{includeLocations}", method = RequestMethod.GET)
    public ResponseEntity<ByteArrayResource> export(@PathVariable boolean includeLocations) throws Exception {
        accessControlService.checkPrivilege(PrivilegeType.DownloadBundle);
        Organisation organisation = UserContextHolder.getUserContext().getOrganisation();
        Long orgId = organisation.getId();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //ZipOutputStream will be automatically closed because we are using try-with-resources.
        /**
         * IMPORTANT: The un-tampered bundle is processed in the order of files inserted while generating the bundle,
         * which is as per below code.
         *
         * Always ensure that bundle is created with content in the same sequence that you want it to be processed during upload.
         * DISCLAIMER: If the bundle is tampered, for example to remove any forms or concepts, then the sequence of processing of bundle files is unknown
         */
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            organisationService.addAddressLevelTypesJson(orgId, zos);
            if (includeLocations) {
                organisationService.addAddressLevelsJson(orgId, zos);
                organisationService.addCatchmentsJson(organisation, zos);
            }
            organisationService.addSubjectTypesJson(orgId, zos);
            organisationService.addOperationalSubjectTypesJson(organisation, zos);
            organisationService.addEncounterTypesJson(organisation, zos);
            organisationService.addOperationalEncounterTypesJson(organisation, zos);
            organisationService.addProgramsJson(organisation, zos);
            organisationService.addOperationalProgramsJson(organisation, zos);
            organisationService.addConceptsJson(orgId, zos);
            organisationService.addFormsJson(orgId, zos);
            organisationService.addFormMappingsJson(orgId, zos);
            organisationService.addOrganisationConfig(orgId, zos);
            //Id source is mapped to a catchment so if includeLocations is false we don't add those sources to json
            organisationService.addIdentifierSourceJson(zos, includeLocations);
            organisationService.addRelationJson(zos);
            organisationService.addRelationShipTypeJson(zos);
            organisationService.addChecklistDetailJson(zos);
            organisationService.addGroupsJson(zos);
            organisationService.addGroupRoleJson(zos);
            organisationService.addGroupPrivilegeJson(zos);
            organisationService.addVideoJson(zos);
            organisationService.addReportCards(zos);
            organisationService.addReportDashboard(zos);
            organisationService.addGroupDashboardJson(zos);
            organisationService.addDocumentation(zos);
            organisationService.addTaskType(zos);
            organisationService.addTaskStatus(zos);
            organisationService.addSubjectTypeIcons(zos);
            organisationService.addReportCardIcons(zos);
            organisationService.addApplicationMenus(zos);
            organisationService.addMessageRules(zos);
            organisationService.addTranslations(orgId, zos);
            organisationService.addOldRuleDependency(orgId, zos);
            organisationService.addOldRules(orgId, zos);
        }

        byte[] baosByteArray = baos.toByteArray();

        return ResponseEntity.ok()
                .headers(getHttpHeaders())
                .contentLength(baosByteArray.length)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new ByteArrayResource(baosByteArray));

    }

    @RequestMapping(value = "/implementation/delete", method = RequestMethod.DELETE)
    @Transactional
    public ResponseEntity delete(@Param("deleteMetadata") boolean deleteMetadata) {
        if (accessControlService.isSuperAdmin()) {
            return new ResponseEntity<>("Super admin cannot delete implementation data", HttpStatus.FORBIDDEN);
        }
        Organisation organisation = organisationService.getCurrentOrganisation();
        if (OrganisationCategory.Production.equals(organisation.getCategory())) {
            return new ResponseEntity<>("Production organisation's data cannot be deleted", HttpStatus.CONFLICT);
        }

        accessControlService.checkPrivilege(MultiTxEntityTypeUpdate);
        organisationService.deleteTransactionalData();

        if (deleteMetadata) {
            accessControlService.checkPrivilege(PrivilegeType.DownloadBundle);
            organisationService.deleteMetadata();
        }
        organisationService.deleteMediaContent(deleteMetadata);

        if (deleteMetadata)
            organisationService.setupBaseOrganisationData(UserContextHolder.getOrganisation());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=impl.zip");
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");
        return header;
    }
}
