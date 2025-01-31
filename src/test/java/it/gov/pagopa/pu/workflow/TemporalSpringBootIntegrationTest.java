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
import it.gov.pagopa.payhub.activities.connector.processexecutions.IngestionFlowFileService;
import it.gov.pagopa.payhub.activities.dto.classifications.PaymentsReportingTransferDTO;
import it.gov.pagopa.payhub.activities.dto.paymentsreporting.PaymentsReportingIngestionFlowFileActivityResult;
import it.gov.pagopa.payhub.activities.exception.NotRetryableActivityException;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.IufClassificationWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyPaymentsReportingSignalDTO;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.PaymentsReportingIngestionWFClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class})
@SpringBootTest(classes = {WorkflowApplication.class,
  // loading real implementation to test NotRetryable extension
  UpdateIngestionFlowStatusActivityImpl.class})
@TestPropertySource(properties = {
  "spring.temporal.test-server.enabled: true",
  "spring.temporal.workers[0].task-queue: PaymentsReportingIngestionWF",
  "spring.temporal.workers[0].name: mock",
  "spring.temporal.workers[0].activity-beans[0]: updateIngestionFlowStatusActivityImpl",
  "spring.temporal.workers[0].activity-beans[1]: fileActivityMock",
  "spring.temporal.workers[0].activity-beans[2]: emailActivityMock",
  "spring.cloud.stream.default-binder: kafka-payments-producer",

  "workflow.payments-reporting-ingestion.retry-maximum-attempts: 3",
  "workflow.payments-reporting-ingestion.retry-maximum-interval: 100",
  "workflow.payments-reporting-ingestion.retry-backoff-coefficient: 1",
  "workflow.payments-reporting-ingestion.start-to-close-timeout-in-seconds: 100"
})
class TemporalSpringBootIntegrationTest {

  private final Set<WorkflowExecutionStatus> wfTerminationStatuses = Set.of(
    WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_COMPLETED,
    WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_FAILED,
    WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_TERMINATED,
    WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_CANCELED
  );

  @Autowired
  private WorkflowClient temporalClient;

  @MockitoBean("fileActivityMock")
  private PaymentsReportingIngestionFlowFileActivityImpl fileActivityMock;
  @MockitoBean("emailActivityMock")
  private SendEmailIngestionFlowActivityImpl emailActivityMock;
  @MockitoBean
  private IufClassificationWFClient iufClassificationWFClientMock;

  @MockitoSpyBean
  private UpdateIngestionFlowStatusActivityImpl statusActivitySpy;
  // Using real UpdateIngestionFlowStatusActivityImpl which depends on the following
  @MockitoBean
  private IngestionFlowFileService ingestionFlowFileServiceMock;

  @Autowired
  private PaymentsReportingIngestionWFClient workflowClient;

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      fileActivityMock,
      ingestionFlowFileServiceMock,
      emailActivityMock,
      statusActivitySpy,
      iufClassificationWFClientMock
    );
  }

  @Test
  void givenSuccessFulUseCaseWhenExecuteWfThenInvokeAllActivities() {
    PaymentsReportingIngestionFlowFileActivityResult result = new PaymentsReportingIngestionFlowFileActivityResult();
    result.setIuf("iuf");
    result.setOrganizationId(1L);
    PaymentsReportingTransferDTO paymentsReportingTransferDTO = new PaymentsReportingTransferDTO();
    result.setTransfers(List.of(paymentsReportingTransferDTO));

    when(fileActivityMock.processFile(anyLong())).thenReturn(result);

    when(ingestionFlowFileServiceMock.updateStatus(anyLong(), any(), any(), any()))
      .thenReturn(1);

    String workflowId = workflowClient.ingest(1L);

    waitUntilWfCompletion(workflowId);

    verify(statusActivitySpy).updateStatus(1L, IngestionFlowFile.StatusEnum.PROCESSING, null, null);
    verify(ingestionFlowFileServiceMock).updateStatus(1L, IngestionFlowFile.StatusEnum.PROCESSING, null, null);
    verify(fileActivityMock).processFile(1L);
    verify(emailActivityMock).sendEmail(1L, true);
    verify(statusActivitySpy).updateStatus(1L, IngestionFlowFile.StatusEnum.COMPLETED, null, null);
    verify(ingestionFlowFileServiceMock).updateStatus(1L, IngestionFlowFile.StatusEnum.COMPLETED, null, null);
    verify(iufClassificationWFClientMock)
      .notifyPaymentsReporting(new IufClassificationNotifyPaymentsReportingSignalDTO(result.getOrganizationId(), result.getIuf(), result.getTransfers()));
  }

  @Test
  void givenNotRetryableExceptionWhenExecuteWfThenStopExecutionWithoutRetries() {
    String workflowId = workflowClient.ingest(1L);
    waitUntilWfFailed(workflowId);

    verify(statusActivitySpy).updateStatus(1L, IngestionFlowFile.StatusEnum.PROCESSING, null, null);
    verify(ingestionFlowFileServiceMock).updateStatus(anyLong(), any(), any(), any());
  }

  @Test
  void givenNotRetryableExceptionExtensionWhenExecuteWfThenStopExecutionWithoutRetries() {
    when(ingestionFlowFileServiceMock.updateStatus(anyLong(), any(), any(), any()))
      .thenThrow(new NotRetryableActivityException("extension"){});

    String workflowId = workflowClient.ingest(1L);
    waitUntilWfFailed(workflowId);

    verify(statusActivitySpy).updateStatus(1L, IngestionFlowFile.StatusEnum.PROCESSING, null, null);
    verify(ingestionFlowFileServiceMock).updateStatus(anyLong(), any(), any(), any());
  }

  @Test
  void givenRetryableExceptionWhenExecuteWfThenRetrieActivityUntilMax() {
    when(ingestionFlowFileServiceMock.updateStatus(anyLong(), any(), any(), any()))
      .thenThrow(new RuntimeException("RetryableActivityException"));

    String workflowId = workflowClient.ingest(1L);
    waitUntilWfFailed(workflowId);

    verify(statusActivitySpy, times(3)).updateStatus(1L, IngestionFlowFile.StatusEnum.PROCESSING, null, null);
    verify(ingestionFlowFileServiceMock, times(3)).updateStatus(anyLong(), any(), any(), any());
  }

  // PRIVATE METHODS
  private void waitUntilWfCompletion(String workflowId) {
    waitUntilWfStatus(workflowId, WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_COMPLETED);
  }

  private void waitUntilWfFailed(String workflowId) {
    waitUntilWfStatus(workflowId, WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_FAILED);
  }

  private void waitUntilWfStatus(String workflowId, WorkflowExecutionStatus status) {
    WorkflowExecutionInfo info;
    do {
      info = WorkflowClientHelper.describeWorkflowInstance(temporalClient.getWorkflowServiceStubs(), "default", WorkflowExecution.newBuilder().setWorkflowId(workflowId).build(), new NoopScope());
    } while (!wfTerminationStatuses.contains(info.getStatus()));

    Assertions.assertEquals(status, info.getStatus());
  }
}


