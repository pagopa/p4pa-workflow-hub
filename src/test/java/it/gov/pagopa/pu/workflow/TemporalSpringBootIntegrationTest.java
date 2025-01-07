package it.gov.pagopa.pu.workflow;

import com.uber.m3.tally.NoopScope;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.client.WorkflowClient;
import io.temporal.internal.client.WorkflowClientHelper;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivityImpl;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivityImpl;
import it.gov.pagopa.payhub.activities.activity.paymentsreporting.PaymentsReportingIngestionFlowFileActivityImpl;
import it.gov.pagopa.payhub.activities.dto.paymentsreporting.PaymentsReportingIngestionFlowFileActivityResult;
import it.gov.pagopa.payhub.activities.exception.NotRetryableActivityException;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.PaymentsReportingIngestionWFClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class})
@SpringBootTest(classes = WorkflowApplication.class)
@TestPropertySource(properties = {
  "spring.temporal.test-server.enabled: true",
  "spring.temporal.workers[0].task-queue: PaymentsReportingIngestionWF",
  "spring.temporal.workers[0].name: mock",
  "spring.temporal.workers[0].activity-beans[0]: statusActivityMock",
  "spring.temporal.workers[0].activity-beans[1]: fileActivityMock",
  "spring.temporal.workers[0].activity-beans[2]: emailActivityMock",
  "workflow.payments-reporting-ingestion.retry-maximum-attempts: 3",
  "workflow.payments-reporting-ingestion.retry-maximum-interval: 100",
  "workflow.payments-reporting-ingestion.retry-backoff-coefficient: 1",
  "workflow.payments-reporting-ingestion.start-to-close-timeout-in-seconds: 100"
})
class TemporalSpringBootIntegrationTest {

  @Autowired
  private WorkflowClient temporalClient;

  @MockitoBean("fileActivityMock")
  private PaymentsReportingIngestionFlowFileActivityImpl fileActivityMock;
  @MockitoBean("emailActivityMock")
  private SendEmailIngestionFlowActivityImpl emailActivityMock;
  @MockitoBean("statusActivityMock")
  private UpdateIngestionFlowStatusActivityImpl statusActivityMock;

  @Autowired
  private PaymentsReportingIngestionWFClient workflowClient;

  @Test
  void testIngestSuccess() {
    PaymentsReportingIngestionFlowFileActivityResult result = new PaymentsReportingIngestionFlowFileActivityResult();
    result.setSuccess(true);

    when(fileActivityMock.processFile(anyLong())).thenReturn(result);

    String workflowId = workflowClient.ingest(1L);

    waitUntilWfCompletion(workflowId);

    verify(statusActivityMock, times(1)).updateStatus(1L, "IMPORT_IN_ELAB");
    verify(fileActivityMock, times(1)).processFile(1L);
    verify(emailActivityMock, times(1)).sendEmail(1L, true);
    verify(statusActivityMock, times(1)).updateStatus(1L, "OK");
  }

  @Test
  void testIngestFailure() {
    PaymentsReportingIngestionFlowFileActivityResult result = new PaymentsReportingIngestionFlowFileActivityResult();
    result.setSuccess(false);

    when(fileActivityMock.processFile(anyLong())).thenReturn(result);

    String workflowId = workflowClient.ingest(1L);

    waitUntilWfCompletion(workflowId);

    verify(statusActivityMock, times(1)).updateStatus(1L, "IMPORT_IN_ELAB");
    verify(fileActivityMock, times(1)).processFile(1L);
    verify(emailActivityMock, times(1)).sendEmail(1L, false);
    verify(statusActivityMock, times(1)).updateStatus(1L, "KO");
  }


  // TESTS FOR NOT RETRYABLE ACTIVITY EXCEPTION

  @Test
  void testIngestNotRetryableActivityExceptionOnProcessFile() {

    PaymentsReportingIngestionFlowFileActivityResult result = new PaymentsReportingIngestionFlowFileActivityResult();
    result.setSuccess(true);

    when(fileActivityMock.processFile(anyLong()))
      .thenThrow(new NotRetryableActivityException("NotRetryableActivityException"));

    String workflowId = workflowClient.ingest(1L);
    waitUntilWfFailed(workflowId);

    verify(statusActivityMock, times(1)).updateStatus(1L, "IMPORT_IN_ELAB");
    verify(fileActivityMock, times(1)).processFile(1L);
    verify(emailActivityMock, never()).sendEmail(anyLong(), anyBoolean());
    verify(statusActivityMock, never()).updateStatus(1L, "OK");
    verify(statusActivityMock, never()).updateStatus(1L, "KO");
  }


  // TEST FOR RETRYABLE ACTIVITY EXCEPTION

  @Test
  void testIngestRetryableActivityExceptionOnProcessFile() {

    PaymentsReportingIngestionFlowFileActivityResult result = new PaymentsReportingIngestionFlowFileActivityResult();
    result.setSuccess(true);

    when(fileActivityMock.processFile(anyLong()))
      .thenThrow(new RuntimeException("RetryableActivityException"));

    String workflowId = workflowClient.ingest(1L);
    waitUntilWfFailed(workflowId);

    verify(statusActivityMock, times(1)).updateStatus(1L, "IMPORT_IN_ELAB");
    verify(fileActivityMock, times(3)).processFile(1L);
    verify(emailActivityMock, never()).sendEmail(anyLong(), anyBoolean());
    verify(statusActivityMock, never()).updateStatus(1L, "OK");

  }
  // PRIVATE METHODS
  private void waitUntilWfCompletion(String workflowId) {
    WorkflowExecutionInfo info;
    do {
      info = WorkflowClientHelper.describeWorkflowInstance(temporalClient.getWorkflowServiceStubs(), "default", WorkflowExecution.newBuilder().setWorkflowId(workflowId).build(), new NoopScope());
    } while (!WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_COMPLETED.equals(info.getStatus()));
  }


  private void waitUntilWfFailed(String workflowId) {
    WorkflowExecutionInfo info;
    do {
      info = WorkflowClientHelper.describeWorkflowInstance(temporalClient.getWorkflowServiceStubs(), "default", WorkflowExecution.newBuilder().setWorkflowId(workflowId).build(), new NoopScope());
    } while (!WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_FAILED.equals(info.getStatus()));
  }


}


