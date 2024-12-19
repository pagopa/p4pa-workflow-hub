package it.gov.pagopa.pu.workflow.config;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.TemporalOptionsCustomizer;
import io.temporal.worker.WorkflowImplementationOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.time.Duration;

@Service
public class TemporalWFImplementationCustomizer implements TemporalOptionsCustomizer<WorkflowImplementationOptions.Builder> {

  private final BaseWfConfig defaultConfig;

  public TemporalWFImplementationCustomizer(
    @Qualifier("baseWfConfig") BaseWfConfig defaultConfig) {
    this.defaultConfig = defaultConfig;
  }

  @Nonnull
  @Override
  public WorkflowImplementationOptions.Builder customize(@Nonnull WorkflowImplementationOptions.Builder optionsBuilder) {
    return optionsBuilder
      .setDefaultActivityOptions(baseWfConfig2ActivityOptions(defaultConfig));
  }

  public static ActivityOptions baseWfConfig2ActivityOptions(BaseWfConfig baseWfConfig) {
    return ActivityOptions.newBuilder()
      .setStartToCloseTimeout(Duration.ofSeconds(baseWfConfig.getStartToCloseTimeoutInSeconds()))
      .setRetryOptions(
        RetryOptions.newBuilder()
          .setInitialInterval(Duration.ofMillis(baseWfConfig.getRetryInitialIntervalInMillis()))
          .setBackoffCoefficient(baseWfConfig.getRetryBackoffCoefficient())
          .setDoNotRetry(IllegalArgumentException.class.getName())
          .setMaximumAttempts(baseWfConfig.getRetryMaximumAttempts())
          .build())
      .build();
  }
}
