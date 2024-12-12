package it.gov.pagopa.pu.workflow.impl;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.paymentsreporting.PaymentsReportingIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.activity.utility.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.dto.reportingflow.PaymentsReportingIngestionFlowFileActivityResult;
import it.gov.pagopa.pu.workflow.PaymentsReportingIngestionWF;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
@WorkflowImpl(taskQueues = "PaymentsReportingIngestionWF")
public class PaymentsReportingIngestionWFImpl implements PaymentsReportingIngestionWF {


  public static final int START_TO_CLOSE_TIMEOUT_IN_SECONDS = 60;
  public static final int RETRY_INITIAL_INTERVAL_IN_MILLIS = 1000;
  public static final double RETRY_BACKOFF_COEFFICIENT = 1.1;
  public static final int RETRY_MAXIMUM_ATTEMPTS = 10;

  private final PaymentsReportingIngestionFlowFileActivity paymentsReportingIngestionFlowFileActivity =
    Workflow.newActivityStub(
      PaymentsReportingIngestionFlowFileActivity.class,
      ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofSeconds(START_TO_CLOSE_TIMEOUT_IN_SECONDS))
        .setTaskQueue("PaymentsReportingIngestionWF")
        .setRetryOptions(
          RetryOptions.newBuilder()
            .setInitialInterval(Duration.ofMillis(RETRY_INITIAL_INTERVAL_IN_MILLIS))
            .setBackoffCoefficient(RETRY_BACKOFF_COEFFICIENT)
            .setDoNotRetry(IllegalArgumentException.class.getName())
            .setMaximumAttempts(RETRY_MAXIMUM_ATTEMPTS)
            .build())
        .build());

/*
  private final SendEmailIngestionFlowActivity sendEmailIngestionFlowActivity =
    Workflow.newActivityStub(
      SendEmailIngestionFlowActivity.class,
      ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofSeconds(START_TO_CLOSE_TIMEOUT_IN_SECONDS))
        .setTaskQueue("PaymentsReportingIngestionWF")
        .setRetryOptions(
          RetryOptions.newBuilder()
            .setInitialInterval(Duration.ofMillis(RETRY_INITIAL_INTERVAL_IN_MILLIS))
            .setBackoffCoefficient(RETRY_BACKOFF_COEFFICIENT)
            .setDoNotRetry(IllegalArgumentException.class.getName())
            .setMaximumAttempts(RETRY_MAXIMUM_ATTEMPTS)
            .build())
        .build());

  private final UpdateIngestionFlowStatusActivity updateIngestionFlowStatusActivity =
    Workflow.newActivityStub(
      UpdateIngestionFlowStatusActivity.class,
      ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofSeconds(START_TO_CLOSE_TIMEOUT_IN_SECONDS))
        .setTaskQueue("PaymentsReportingIngestionWF")
        .setRetryOptions(
          RetryOptions.newBuilder()
            .setInitialInterval(Duration.ofMillis(RETRY_INITIAL_INTERVAL_IN_MILLIS))
            .setBackoffCoefficient(RETRY_BACKOFF_COEFFICIENT)
            .setDoNotRetry(IllegalArgumentException.class.getName())
            .setMaximumAttempts(RETRY_MAXIMUM_ATTEMPTS)
            .build())
        .build());
*/

  @Override
  public void ingest(Long fileId) {
    log.info("Ingesting file with ID: " + fileId);

    PaymentsReportingIngestionFlowFileActivityResult ingestionResult =
      paymentsReportingIngestionFlowFileActivity.processFile(fileId);
 //   sendEmailIngestionFlowActivity.sendEmail("" + fileId, ingestionResult.isSuccess());
 //   updateIngestionFlowStatusActivity.updateStatus(fileId, ingestionResult.isSuccess() ? "OK" : "KO");

    log.info("Ingestion completed for file with ID: " + fileId);
  }
}
