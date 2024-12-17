package it.gov.pagopa.pu.workflow.ingestionflow;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.paymentsreporting.PaymentsReportingIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.activity.utility.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.dto.paymentsreporting.PaymentsReportingIngestionFlowFileActivityResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;

/**
 * Workflow implementation for the Payments Reporting Ingestion Workflow
 */

@Slf4j
@WorkflowImpl(taskQueues = "PaymentsReportingIngestionWF")
public class PaymentsReportingIngestionWFImpl implements PaymentsReportingIngestionWF {

  private String workflowQueue;
  private int startToCloseTimeoutInSeconds = 0;
  private int retryInitialIntervalInMillis;
  private double retryBackoffCoefficient;
  private int retryMaximumAttempts;

  public PaymentsReportingIngestionWFImpl(
    @Value("${workflow.queue:PaymentsReportingIngestionWF}") String workflowQueue,
    @Value("${workflow.startToCloseTimeoutInSeconds:60}") int startToCloseTimeoutInSeconds,
    @Value("${workflow.retryInitialIntervalInMillis:1000}") int retryInitialIntervalInMillis,
    @Value("${workflow.retryBackoffCoefficient:1.1}") double retryBackoffCoefficient,
    @Value("${workflow.retryMaximumAttempts:10}") int retryMaximumAttempts) {
    this.workflowQueue = workflowQueue;
    this.startToCloseTimeoutInSeconds = startToCloseTimeoutInSeconds;
    this.retryInitialIntervalInMillis = retryInitialIntervalInMillis;
    this.retryBackoffCoefficient = retryBackoffCoefficient;
    this.retryMaximumAttempts = retryMaximumAttempts;
  }

  /**
   * Activity stubs
   */

  private final PaymentsReportingIngestionFlowFileActivity paymentsReportingIngestionFlowFileActivity =
    Workflow.newActivityStub(
      PaymentsReportingIngestionFlowFileActivity.class,
      ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofSeconds(startToCloseTimeoutInSeconds))
        .setTaskQueue(workflowQueue)
        .setRetryOptions(
          RetryOptions.newBuilder()
            .setInitialInterval(Duration.ofMillis(retryInitialIntervalInMillis))
            .setBackoffCoefficient(retryBackoffCoefficient)
            .setDoNotRetry(IllegalArgumentException.class.getName())
            .setMaximumAttempts(retryMaximumAttempts)
            .build())
        .build());

  private final SendEmailIngestionFlowActivity sendEmailIngestionFlowActivity =
    Workflow.newActivityStub(
      SendEmailIngestionFlowActivity.class,
      ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofSeconds(startToCloseTimeoutInSeconds))
        .setTaskQueue(workflowQueue)
        .setRetryOptions(
          RetryOptions.newBuilder()
            .setInitialInterval(Duration.ofMillis(retryInitialIntervalInMillis))
            .setBackoffCoefficient(retryBackoffCoefficient)
            .setDoNotRetry(IllegalArgumentException.class.getName())
            .setMaximumAttempts(retryMaximumAttempts)
            .build())
        .build());

  private final UpdateIngestionFlowStatusActivity updateIngestionFlowStatusActivity =
    Workflow.newActivityStub(
      UpdateIngestionFlowStatusActivity.class,
      ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofSeconds(startToCloseTimeoutInSeconds))
        .setTaskQueue(workflowQueue)
        .setRetryOptions(
          RetryOptions.newBuilder()
            .setInitialInterval(Duration.ofMillis(retryInitialIntervalInMillis))
            .setBackoffCoefficient(retryBackoffCoefficient)
            .setDoNotRetry(IllegalArgumentException.class.getName())
            .setMaximumAttempts(retryMaximumAttempts)
            .build())
        .build());


  /**
   * Workflow implementation
   */


  @Override
  public void ingest(Long ingestionFlowFileId) {
    log.info("Handling IngestingFlowFileId: " + ingestionFlowFileId);

    PaymentsReportingIngestionFlowFileActivityResult ingestionResult =
      paymentsReportingIngestionFlowFileActivity.processFile(ingestionFlowFileId);
    sendEmailIngestionFlowActivity.sendEmail("" + ingestionFlowFileId, ingestionResult.isSuccess());
    updateIngestionFlowStatusActivity.updateStatus(ingestionFlowFileId, ingestionResult.isSuccess() ? "OK" : "KO");

    log.info("Ingestion completed for file with ID: " + ingestionFlowFileId);
  }

}
