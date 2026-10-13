package com.fintech.risk;

import com.fintech.risk.train.TrainingRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Dual-mode entry: {@code train} builds models/docs; {@code serve} (default) starts the REST API.
 */
@SpringBootApplication
public class CreditRiskApplication {

  public static void main(String[] args) throws Exception {
    String mode = resolveMode(args);
    if ("train".equals(mode)) {
      SpringApplication app = new SpringApplication(CreditRiskApplication.class);
      app.setAdditionalProfiles("train");
      try (ConfigurableApplicationContext ctx = app.run(args)) {
        ctx.getBean(TrainingRunner.class).runTraining();
      }
      return;
    }
    // serve (default)
    SpringApplication.run(CreditRiskApplication.class, stripModeArgs(args));
  }

  static String resolveMode(String[] args) {
    if (args == null) {
      return "serve";
    }
    for (String arg : args) {
      if ("train".equalsIgnoreCase(arg) || "serve".equalsIgnoreCase(arg)) {
        return arg.toLowerCase();
      }
      if (arg.startsWith("--mode=")) {
        return arg.substring("--mode=".length()).toLowerCase();
      }
    }
    return "serve";
  }

  private static String[] stripModeArgs(String[] args) {
    return java.util.Arrays.stream(args)
        .filter(a -> !"train".equalsIgnoreCase(a) && !"serve".equalsIgnoreCase(a)
            && !a.startsWith("--mode="))
        .toArray(String[]::new);
  }
}
