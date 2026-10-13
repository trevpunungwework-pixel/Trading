package com.fintech.risk.scoring;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Profile("!train")
public class ScoringController {

  private final ScoringService scoringService;

  public ScoringController(ScoringService scoringService) {
    this.scoringService = scoringService;
  }

  @PostMapping("/score")
  public ScoreResponse score(@RequestBody ApplicantRequest request) throws Exception {
    return scoringService.score(request);
  }

  @GetMapping("/model")
  public ModelInfoResponse model() {
    return scoringService.modelInfo();
  }

  @ExceptionHandler(InvalidApplicantException.class)
  public ResponseEntity<MapMessage> badRequest(InvalidApplicantException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MapMessage(ex.getMessage()));
  }

  public record MapMessage(String message) {}
}
