package it.gov.pagopa.pu.workflow.config.temporal;

import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.spring.boot.TemporalOptionsCustomizer;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.time.Duration;

@Service
public class TemporalWFServiceCustomizer implements TemporalOptionsCustomizer<WorkflowServiceStubsOptions.Builder> {

  private final TemporalConfig temporalConfig;

  public TemporalWFServiceCustomizer(TemporalConfig temporalConfig) {
    this.temporalConfig = temporalConfig;
  }

  @Nonnull
  @Override
  public WorkflowServiceStubsOptions.Builder customize(@Nonnull WorkflowServiceStubsOptions.Builder optionsBuilder) {
    return optionsBuilder
      .setSystemInfoTimeout(Duration.ofSeconds(temporalConfig.getTimeouts().getSystemInfo()))
      .setRpcLongPollTimeout(Duration.ofSeconds(temporalConfig.getTimeouts().getRpcLongPoll()))
      .setRpcQueryTimeout(Duration.ofSeconds(temporalConfig.getTimeouts().getRpcQuery()))
      .setRpcTimeout(Duration.ofSeconds(temporalConfig.getTimeouts().getRpcGeneric()));
  }

}
