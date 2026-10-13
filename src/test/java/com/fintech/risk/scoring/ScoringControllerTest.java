package com.fintech.risk.scoring;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Requires {@code models/credit-logit.model} (produced by {@code java -jar ... train}).
 * The model is committed so {@code mvn clean verify} works on a fresh clone.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ScoringControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void scoreEndpointReturnsValidResponse() throws Exception {
    String body = """
        {
          "checking_status": "<0",
          "duration": 6,
          "credit_history": "critical/other existing credit",
          "purpose": "radio/tv",
          "credit_amount": 1169,
          "savings_status": "no known savings",
          "employment": ">=7",
          "installment_commitment": 4,
          "personal_status": "male single",
          "other_parties": "none",
          "residence_since": 4,
          "property_magnitude": "real estate",
          "age": 67,
          "other_payment_plans": "none",
          "housing": "own",
          "existing_credits": 2,
          "job": "skilled",
          "num_dependents": 1,
          "own_telephone": "yes",
          "foreign_worker": "yes"
        }
        """;

    mockMvc.perform(post("/api/v1/score")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.probabilityOfDefault").isNumber())
        .andExpect(jsonPath("$.decision").isString())
        .andExpect(jsonPath("$.modelVersion").isString());
  }

  @Test
  void modelEndpointReturnsMetadata() throws Exception {
    mockMvc.perform(get("/api/v1/model"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.modelName").value("Logistic"))
        .andExpect(jsonPath("$.crossValidationMetrics.rocAuc").isNumber());
  }

  @Test
  void unknownNominalReturns400() throws Exception {
    String body = """
        {
          "checking_status": "not-a-real-value",
          "duration": 6,
          "credit_history": "critical/other existing credit",
          "purpose": "radio/tv",
          "credit_amount": 1169,
          "savings_status": "no known savings",
          "employment": ">=7",
          "installment_commitment": 4,
          "personal_status": "male single",
          "other_parties": "none",
          "residence_since": 4,
          "property_magnitude": "real estate",
          "age": 67,
          "other_payment_plans": "none",
          "housing": "own",
          "existing_credits": 2,
          "job": "skilled",
          "num_dependents": 1,
          "own_telephone": "yes",
          "foreign_worker": "yes"
        }
        """;

    mockMvc.perform(post("/api/v1/score")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").exists());
  }
}
