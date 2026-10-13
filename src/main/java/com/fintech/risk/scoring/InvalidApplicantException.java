package com.fintech.risk.scoring;

public class InvalidApplicantException extends RuntimeException {
  public InvalidApplicantException(String message) {
    super(message);
  }
}
