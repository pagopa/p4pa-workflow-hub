package it.gov.pagopa.pu.workflow.ingestionflow;

import io.temporal.activity.ActivityOptions;
import io.temporal.activity.LocalActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.paymentsreporting.PaymentsReportingIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.dto.paymentsreporting.PaymentsReportingIngestionFlowFileActivityResult;
import it.gov.pagopa.pu.workflow.local.SpringValuesLocalActivity;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * Workflow implementation for the Payments Reporting Ingestion Workflow
 */

@Slf4j
@WorkflowImpl(taskQueues = "PaymentsReportingIngestionWF")
public class PaymentsReportingIngestionWFImpl implements PaymentsReportingIngestionWF {

  /**
   * Local activity stub
   * used to retrieve the Spring values
   */
  private static final Duration START_TO_CLOSE_TIMEOUT = Duration.ofSeconds(1);
  private static final Duration INITIAL_INTERVAL = Duration.ofMillis(1000);
  private static final double BACKOFF_COEFFICIENT = 1.1;
  private static final String DO_NOT_RETRY_EXCEPTION = IllegalArgumentException.class.getName();
  private static final int MAXIMUM_ATTEMPTS = 10;

  private final SpringValuesLocalActivity springValuesLocalActivity =
    Workflow.newLocalActivityStub(
      SpringValuesLocalActivity.class,
      LocalActivityOptions.newBuilder()
        .setStartToCloseTimeout(START_TO_CLOSE_TIMEOUT)
        .setRetryOptions(
          RetryOptions.newBuilder()
            .setInitialInterval(INITIAL_INTERVAL)
            .setBackoffCoefficient(BACKOFF_COEFFICIENT)
            .setDoNotRetry(DO_NOT_RETRY_EXCEPTION)
            .setMaximumAttempts(MAXIMUM_ATTEMPTS)
            .build())
        .build());


  /**
   * Workflow implementation
   */

  @Override
  public void ingest(Long ingestionFlowFileId) {
    log.info("Handling IngestingFlowFileId: " + ingestionFlowFileId);


    // Spring values
    int startToCloseTimeoutInSeconds = Integer.valueOf( springValuesLocalActivity.getProperties().get("startToCloseTimeoutInSeconds"));
    int retryInitialIntervalInMillis = Integer.valueOf( springValuesLocalActivity.getProperties().get("retryInitialIntervalInMillis"));
    int retryMaximumAttempts = Integer.valueOf( springValuesLocalActivity.getProperties().get("retryMaximumAttempts"));
    int retryBackoffCoefficient = Double.valueOf( springValuesLocalActivity.getProperties().get("retryBackoffCoefficient")).intValue();
    String workflowQueue =  springValuesLocalActivity.getProperties().get("workflowQueue");


    // Activity stubs
    PaymentsReportingIngestionFlowFileActivity paymentsReportingIngestionFlowFileActivity =
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

//  private final SendEmailIngestionFlowActivity sendEmailIngestionFlowActivity =
//    Workflow.newActivityStub(
//      SendEmailIngestionFlowActivity.class,
//      ActivityOptions.newBuilder()
//        .setStartToCloseTimeout(Duration.ofSeconds(startToCloseTimeoutInSeconds))
//        .setTaskQueue(workflowQueue)
//        .setRetryOptions(
//          RetryOptions.newBuilder()
//            .setInitialInterval(Duration.ofMillis(retryInitialIntervalInMillis))
//            .setBackoffCoefficient(retryBackoffCoefficient)
//            .setDoNotRetry(IllegalArgumentException.class.getName())
//            .setMaximumAttempts(retryMaximumAttempts)
//            .build())
//        .build());

    UpdateIngestionFlowStatusActivity updateIngestionFlowStatusActivity =
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
     * Workflow logic
     */

    PaymentsReportingIngestionFlowFileActivityResult ingestionResult =
      paymentsReportingIngestionFlowFileActivity.processFile(ingestionFlowFileId);
    //   sendEmailIngestionFlowActivity.sendEmail(ingestionFlowFileId, ingestionResult.isSuccess());
    updateIngestionFlowStatusActivity.updateStatus(ingestionFlowFileId, ingestionResult.isSuccess() ? "OK" : "KO");

    log.info("Ingestion completed for file with ID: " + ingestionFlowFileId);
  }

}
