package org.avni.report;

public class UserActivityResult {
    private String userName;
    private Long id;
    private Long registrationCount;
    private Long programEnrolmentCount;
    private Long programEncounterCount;
    private Long generalEncounterCount;
    private Long count;

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRegistrationCount() {
        return registrationCount;
    }

    public void setRegistrationCount(Long registrationCount) {
        this.registrationCount = registrationCount;
    }

    public Long getProgramEnrolmentCount() {
        return programEnrolmentCount;
    }

    public void setProgramEnrolmentCount(Long programEnrolmentCount) {
        this.programEnrolmentCount = programEnrolmentCount;
    }

    public Long getProgramEncounterCount() {
        return programEncounterCount;
    }

    public void setProgramEncounterCount(Long programEncounterCount) {
        this.programEncounterCount = programEncounterCount;
    }

    public Long getGeneralEncounterCount() {
        return generalEncounterCount;
    }

    public void setGeneralEncounterCount(Long generalEncounterCount) {
        this.generalEncounterCount = generalEncounterCount;
    }
}
