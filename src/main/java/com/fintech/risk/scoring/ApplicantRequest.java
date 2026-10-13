package com.fintech.risk.scoring;

import com.fasterxml.jackson.annotation.JsonProperty;

/** One loan applicant — fields match German Credit attributes. */
public class ApplicantRequest {

  @JsonProperty("checking_status")
  private String checkingStatus;
  private Double duration;
  @JsonProperty("credit_history")
  private String creditHistory;
  private String purpose;
  @JsonProperty("credit_amount")
  private Double creditAmount;
  @JsonProperty("savings_status")
  private String savingsStatus;
  private String employment;
  @JsonProperty("installment_commitment")
  private Double installmentCommitment;
  @JsonProperty("personal_status")
  private String personalStatus;
  @JsonProperty("other_parties")
  private String otherParties;
  @JsonProperty("residence_since")
  private Double residenceSince;
  @JsonProperty("property_magnitude")
  private String propertyMagnitude;
  private Double age;
  @JsonProperty("other_payment_plans")
  private String otherPaymentPlans;
  private String housing;
  @JsonProperty("existing_credits")
  private Double existingCredits;
  private String job;
  @JsonProperty("num_dependents")
  private Double numDependents;
  @JsonProperty("own_telephone")
  private String ownTelephone;
  @JsonProperty("foreign_worker")
  private String foreignWorker;

  public String getCheckingStatus() { return checkingStatus; }
  public void setCheckingStatus(String checkingStatus) { this.checkingStatus = checkingStatus; }
  public Double getDuration() { return duration; }
  public void setDuration(Double duration) { this.duration = duration; }
  public String getCreditHistory() { return creditHistory; }
  public void setCreditHistory(String creditHistory) { this.creditHistory = creditHistory; }
  public String getPurpose() { return purpose; }
  public void setPurpose(String purpose) { this.purpose = purpose; }
  public Double getCreditAmount() { return creditAmount; }
  public void setCreditAmount(Double creditAmount) { this.creditAmount = creditAmount; }
  public String getSavingsStatus() { return savingsStatus; }
  public void setSavingsStatus(String savingsStatus) { this.savingsStatus = savingsStatus; }
  public String getEmployment() { return employment; }
  public void setEmployment(String employment) { this.employment = employment; }
  public Double getInstallmentCommitment() { return installmentCommitment; }
  public void setInstallmentCommitment(Double installmentCommitment) {
    this.installmentCommitment = installmentCommitment;
  }
  public String getPersonalStatus() { return personalStatus; }
  public void setPersonalStatus(String personalStatus) { this.personalStatus = personalStatus; }
  public String getOtherParties() { return otherParties; }
  public void setOtherParties(String otherParties) { this.otherParties = otherParties; }
  public Double getResidenceSince() { return residenceSince; }
  public void setResidenceSince(Double residenceSince) { this.residenceSince = residenceSince; }
  public String getPropertyMagnitude() { return propertyMagnitude; }
  public void setPropertyMagnitude(String propertyMagnitude) {
    this.propertyMagnitude = propertyMagnitude;
  }
  public Double getAge() { return age; }
  public void setAge(Double age) { this.age = age; }
  public String getOtherPaymentPlans() { return otherPaymentPlans; }
  public void setOtherPaymentPlans(String otherPaymentPlans) {
    this.otherPaymentPlans = otherPaymentPlans;
  }
  public String getHousing() { return housing; }
  public void setHousing(String housing) { this.housing = housing; }
  public Double getExistingCredits() { return existingCredits; }
  public void setExistingCredits(Double existingCredits) { this.existingCredits = existingCredits; }
  public String getJob() { return job; }
  public void setJob(String job) { this.job = job; }
  public Double getNumDependents() { return numDependents; }
  public void setNumDependents(Double numDependents) { this.numDependents = numDependents; }
  public String getOwnTelephone() { return ownTelephone; }
  public void setOwnTelephone(String ownTelephone) { this.ownTelephone = ownTelephone; }
  public String getForeignWorker() { return foreignWorker; }
  public void setForeignWorker(String foreignWorker) { this.foreignWorker = foreignWorker; }
}
