package com.fintech.risk.scoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.risk.data.Schema;
import com.fintech.risk.integration.LoanApplicationEvents;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

@Service
@Profile("!train")
public class ScoringService {

  private final ModelHolder modelHolder;
  private final ScoringAuditRepository auditRepository;
  private final LoanApplicationEvents events;
  private final ObjectMapper objectMapper;
  private final double approveThreshold;
  private final double declineThreshold;
  private final String modelVersion;

  public ScoringService(
      ModelHolder modelHolder,
      ScoringAuditRepository auditRepository,
      LoanApplicationEvents events,
      ObjectMapper objectMapper,
      @Value("${credit.risk.approve-threshold}") double approveThreshold,
      @Value("${credit.risk.decline-threshold}") double declineThreshold,
      @Value("${credit.risk.model-version}") String modelVersion) {
    this.modelHolder = modelHolder;
    this.auditRepository = auditRepository;
    this.events = events;
    this.objectMapper = objectMapper;
    this.approveThreshold = approveThreshold;
    this.declineThreshold = declineThreshold;
    this.modelVersion = modelVersion;
  }

  public ScoreResponse score(ApplicantRequest request) throws Exception {
    Instances header = modelHolder.header();
    Instance inst = toInstance(request, header);
    int posIndex = header.classAttribute().indexOfValue(Schema.POSITIVE_CLASS);
    double[] dist = modelHolder.classifier().distributionForInstance(inst);
    double pd = dist[posIndex];
    String decision = decide(pd);

    String json = objectMapper.writeValueAsString(request);
    auditRepository.insert(json, pd, decision, modelVersion);
    events.publishedScored(
        new LoanApplicationEvents.ScoredEvent("n/a", pd, decision, modelVersion));

    return new ScoreResponse(pd, decision, modelVersion);
  }

  public ModelInfoResponse modelInfo() {
    var meta = modelHolder.metadata();
    Map<String, Double> metrics = new LinkedHashMap<>();
    metrics.put("rocAuc", meta.rocAuc());
    metrics.put("gini", meta.gini());
    metrics.put("ks", meta.ks());
    metrics.put("recallBad", meta.recallBad());
    metrics.put("precisionBad", meta.precisionBad());
    metrics.put("totalCost", meta.totalCost());
    return new ModelInfoResponse(
        meta.modelName(), meta.trainingDate(), meta.version(), metrics);
  }

  private String decide(double pd) {
    if (pd < approveThreshold) {
      return "approve";
    }
    if (pd >= declineThreshold) {
      return "decline";
    }
    return "refer";
  }

  Instance toInstance(ApplicantRequest req, Instances header) {
    double[] values = new double[header.numAttributes()];
    setNominal(header, values, "checking_status", req.getCheckingStatus());
    setNumeric(header, values, "duration", req.getDuration());
    setNominal(header, values, "credit_history", req.getCreditHistory());
    setNominal(header, values, "purpose", req.getPurpose());
    setNumeric(header, values, "credit_amount", req.getCreditAmount());
    setNominal(header, values, "savings_status", req.getSavingsStatus());
    setNominal(header, values, "employment", req.getEmployment());
    setNumeric(header, values, "installment_commitment", req.getInstallmentCommitment());
    setNominal(header, values, "personal_status", req.getPersonalStatus());
    setNominal(header, values, "other_parties", req.getOtherParties());
    setNumeric(header, values, "residence_since", req.getResidenceSince());
    setNominal(header, values, "property_magnitude", req.getPropertyMagnitude());
    setNumeric(header, values, "age", req.getAge());
    setNominal(header, values, "other_payment_plans", req.getOtherPaymentPlans());
    setNominal(header, values, "housing", req.getHousing());
    setNumeric(header, values, "existing_credits", req.getExistingCredits());
    setNominal(header, values, "job", req.getJob());
    setNumeric(header, values, "num_dependents", req.getNumDependents());
    setNominal(header, values, "own_telephone", req.getOwnTelephone());
    setNominal(header, values, "foreign_worker", req.getForeignWorker());
    // class unknown at score time
    values[header.classIndex()] = Double.NaN;

    Instance inst = new DenseInstance(1.0, values);
    inst.setDataset(header);
    return inst;
  }

  private void setNumeric(Instances header, double[] values, String name, Double value) {
    int idx = header.attribute(name).index();
    if (value == null) {
      values[idx] = Double.NaN;
    } else {
      values[idx] = value;
    }
  }

  private void setNominal(Instances header, double[] values, String name, String value) {
    int idx = header.attribute(name).index();
    if (value == null || value.isBlank()) {
      throw new InvalidApplicantException("Missing required attribute: " + name);
    }
    int code = header.attribute(name).indexOfValue(value);
    if (code < 0) {
      throw new InvalidApplicantException(
          "Unknown value for " + name + ": '" + value + "'");
    }
    values[idx] = code;
  }
}
