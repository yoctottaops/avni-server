package org.avni.importer.batch.csv.writer;

import org.avni.dao.ConceptRepository;
import org.avni.dao.IndividualRepository;
import org.avni.dao.LocationRepository;
import org.avni.dao.UserRepository;
import org.avni.domain.*;
import org.avni.framework.security.UserContextHolder;
import org.avni.importer.batch.model.Row;
import org.avni.service.CatchmentService;
import org.avni.service.CognitoIdpService;
import org.avni.service.UserService;
import org.avni.util.S;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.avni.domain.OperatingIndividualScope.ByCatchment;

@Component
public class UserAndCatchmentWriter implements ItemWriter<Row>, Serializable {
    private final UserService userService;
    private final CatchmentService catchmentService;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final CognitoIdpService cognitoService;
    private final IndividualRepository individualRepository;
    private final ConceptRepository conceptRepository;

    @Autowired
    public UserAndCatchmentWriter(CatchmentService catchmentService,
                                  LocationRepository locationRepository,
                                  UserService userService,
                                  UserRepository userRepository,
                                  CognitoIdpService cognitoService,
                                  IndividualRepository individualRepository,
                                  ConceptRepository conceptRepository) {
        this.catchmentService = catchmentService;
        this.locationRepository = locationRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.cognitoService = cognitoService;
        this.individualRepository = individualRepository;
        this.conceptRepository = conceptRepository;
    }

    @Override
    public void write(List<? extends Row> rows) throws Exception {
        for (Row row : rows) write(row);
    }

    private void write(Row row) throws Exception {
        String fullAddress = row.get("Location with full hierarchy");
        String catchmentName = row.get("Catchment Name");
        String nameOfUser = row.get("Full Name of User");
        String username = row.get("Username");
        String email = row.get("Email");
        String phoneNumber = row.get("Phone");
        String language = row.get("Language");
        Locale locale = S.isEmpty(language) ? Locale.en : Locale.valueByName(language);
        Boolean trackLocation = row.getBool("Track Location");
        String datePickerMode = row.get("Date picker mode");
        Boolean beneficiaryMode = row.getBool("Enable Beneficiary mode");
        String idPrefix = row.get("Beneficiary ID Prefix");
        String syncConcept1Name = row.get("Sync concept 1 name");
        String syncConcept1Values = row.get("Sync concept 1 values");
        String syncConcept2Values = row.get("Sync concept 2 name");
        String syncConcept2Name = row.get("Sync concept 2 values");
        String subjectUUIDs = row.get("Sync subject UUIDs");
        JsonObject syncSettings = new JsonObject();
        if (syncConcept1Name != null && syncConcept1Values != null) {
            syncSettings.with(User.SyncSettingKeys.syncConcept1.name(), conceptRepository.findByName(syncConcept1Name));
            syncSettings.with(User.SyncSettingKeys.syncConcept1Values.name(), Arrays.asList(syncConcept1Values.split(",")));
        }
        if (syncConcept2Name != null && syncConcept2Values != null) {
            syncSettings.with(User.SyncSettingKeys.syncConcept2.name(), conceptRepository.findByName(syncConcept2Name));
            syncSettings.with(User.SyncSettingKeys.syncConcept2Values.name(), Arrays.asList(syncConcept2Values.split(",")));
        }
        if (subjectUUIDs != null) {
            List<Long> subjectIds = Arrays.stream(subjectUUIDs.split(","))
                    .map(individualRepository::findByUuid)
                    .filter(Objects::nonNull)
                    .map(CHSBaseEntity::getId)
                    .collect(Collectors.toList());
            syncSettings.with(User.SyncSettingKeys.subjectIds.name(), subjectIds);
        }

        AddressLevel location = locationRepository.findByTitleLineageIgnoreCase(fullAddress)
                .orElseThrow(() -> new Exception(format(
                        "Provided Location does not exist. Please check for spelling mistakes '%s'", fullAddress)));

        Catchment catchment = catchmentService.createOrUpdate(catchmentName, location);
        Organisation organisation = UserContextHolder.getUserContext().getOrganisation();
        String userSuffix = "@".concat(organisation.getUsernameSuffix());
        User.validateUsername(username, userSuffix);
        User user = userRepository.findByUsername(username);
        if (user != null) return;
        user = new User();
        user.assignUUIDIfRequired();
        user.setUsername(username);
        User.validateEmail(email);
        user.setEmail(email);
        User.validatePhoneNumber(phoneNumber);
        user.setPhoneNumber(phoneNumber);
        user.setName(nameOfUser);
        user.setCatchment(catchment);
        user.setOperatingIndividualScope(ByCatchment);

        user.setSettings(new JsonObject()
                .with("locale", locale)
                .with("trackLocation", trackLocation)
                .withEmptyCheck("datePickerMode", datePickerMode)
                .with("showBeneficiaryMode", beneficiaryMode)
                .withEmptyCheck("idPrefix", idPrefix));

        User currentUser = userService.getCurrentUser();
        user.setOrganisationId(organisation.getId());
        user.setAuditInfo(currentUser);
        user.setSyncSettings(syncSettings);
        cognitoService.createUser(user);
        userService.save(user);
        userService.addToDefaultUserGroup(user);
    }
}
