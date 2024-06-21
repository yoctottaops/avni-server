package org.avni.server.web.request;

import org.avni.server.domain.JsonObject;
import org.avni.server.domain.metadata.OrganisationCategory;
import org.springframework.hateoas.core.Relation;

import java.util.List;

@Relation(collectionRelation = "userInfo")
public class UserInfoClientContract extends UserInfoContract {
    private long lastSessionTime;
    private List<GroupPrivilegeContract> privileges;

    public UserInfoClientContract() {
    }

    public UserInfoClientContract(String username, String orgName, Long orgId, String usernameSuffix, String[] roles, JsonObject settings, String name, String catchmentName, JsonObject syncSettings, List<GroupPrivilegeContract> privileges) {
        super(username, orgName, orgId, usernameSuffix, settings, name, catchmentName, syncSettings);
        this.privileges = privileges;
    }

    public List<GroupPrivilegeContract> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<GroupPrivilegeContract> privileges) {
        this.privileges = privileges;
    }

    public long getLastSessionTime() {
        return lastSessionTime;
    }

    public void setLastSessionTime(long lastSessionTime) {
        this.lastSessionTime = lastSessionTime;
    }
}
