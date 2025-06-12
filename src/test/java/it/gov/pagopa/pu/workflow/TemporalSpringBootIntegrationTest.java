package it.gov.pagopa.pu.workflow;

import com.uber.m3.tally.NoopScope;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.client.WorkflowClient;
import io.temporal.internal.client.WorkflowClientHelper;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivityImpl;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivityImpl;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.paymentsreporting.PaymentsReportingIngestionFlowFileActivityImpl;
import it.gov.pagopa.payhub.activities.connector.processexecutions.IngestionFlowFileService;
import it.gov.pagopa.payhub.activities.dto.classifications.PaymentsReportingTransferDTO;
import it.gov.pagopa.payhub.activities.dto.ingestion.IngestionFlowFileResult;
import it.gov.pagopa.payhub.activities.dto.ingestion.paymentsreporting.PaymentsReportingIngestionFlowFileActivityResult;
import it.gov.pagopa.payhub.activities.exception.NotRetryableActivityException;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFileStatus;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.IufClassificationWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyPaymentsReportingSignalDTO;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.PaymentsReportingIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.PaymentsReportingPagoPaBrokersFetchScheduler;
import it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy.SynchronizeTaxonomyPagoPaFetchScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
  "spring.datasource.driver-class-name=org.h2.Driver",
  "spring.datasource.url=jdbc:h2:mem:db;DB_CLOSE_DELAY=-1",
  "spring.datasource.username=sa",
  "spring.datasource.password=sa",

  "spring.cloud.function.definition=",

  "spring.temporal.test-server.enabled: true",
  "spring.temporal.workers[0].task-queue: IngestionFlowFileWF",
  "spring.temporal.workers[0].name: mock",
  "spring.temporal.workers[0].activity-beans[0]: updateIngestionFlowStatusActivityImpl",
  "spring.temporal.workers[0].activity-beans[1]: fileActivityMock",
  "spring.temporal.workers[0].activity-beans[2]: emailActivityMock",

  "workflow.base-ingestion-flow.retry-maximum-attempts: 3",
  "workflow.base-ingestion-flow.retry-maximum-interval: 100",
  "workflow.base-ingestion-flow.retry-backoff-coefficient: 1",
  "workflow.base-ingestion-flow.start-to-close-timeout-in-seconds: 100"
})
class TemporalSpringBootIntegrationTest {

  /** <a href="https://docs.temporal.io/workflows#status">Closed statuses</a> */
  private final Set<WorkflowExecutionStatus> wfTerminationStatuses = Set.of(
    WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_COMPLETED,
    WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_FAILED,
    WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_TERMINATED,
    WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_CANCELED
  );

  @Autowired
  private WorkflowClient temporalClient;
  @Value("${spring.temporal.namespace}")
  private String temporalNamespace;

  // disabling scheduling due to temporal test server not support
  @MockitoBean
  private PaymentsReportingPagoPaBrokersFetchScheduler paymentsReportingPagoPaBrokersFetchSchedulerMock;

  @MockitoBean
  private SynchronizeTaxonomyPagoPaFetchScheduler synchronizeTaxonomyPagoPaFetchSchedulerMock;

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
  void givenSuccessfulUseCaseWhenExecuteWfThenInvokeAllActivities() {
    PaymentsReportingTransferDTO paymentsReportingTransferDTO = new PaymentsReportingTransferDTO();
    PaymentsReportingIngestionFlowFileActivityResult result = new PaymentsReportingIngestionFlowFileActivityResult();
    result.setIuf("iuf");
    result.setOrganizationId(1L);
    result.setProcessedRows(1L);
    result.setTotalRows(10L);
    result.setTransfers(List.of(paymentsReportingTransferDTO));

    IngestionFlowFileResult expectedIngestionFlowFileResult = IngestionFlowFileResult.builder()
      .organizationId(result.getOrganizationId())
      .totalRows(result.getTotalRows())
      .processedRows(result.getProcessedRows())
      .build();

    when(fileActivityMock.processFile(anyLong())).thenReturn(result);

    when(ingestionFlowFileServiceMock.updateStatus(anyLong(), any(), any(), any()))
      .thenReturn(1);

    WorkflowCreatedDTO wfExec = workflowClient.ingest(1L);

    waitUntilWfCompletion(wfExec);

    verify(statusActivitySpy).updateIngestionFlowFileStatus(1L, IngestionFlowFileStatus.UPLOADED, IngestionFlowFileStatus.PROCESSING, null);
    verify(ingestionFlowFileServiceMock).updateStatus(1L, IngestionFlowFileStatus.UPLOADED, IngestionFlowFileStatus.PROCESSING, null);
    verify(fileActivityMock).processFile(1L);
    verify(statusActivitySpy).updateIngestionFlowFileStatus(1L, IngestionFlowFileStatus.PROCESSING, IngestionFlowFileStatus.COMPLETED, expectedIngestionFlowFileResult);
    verify(ingestionFlowFileServiceMock).updateStatus(1L, IngestionFlowFileStatus.PROCESSING, IngestionFlowFileStatus.COMPLETED, expectedIngestionFlowFileResult);
    verify(emailActivityMock).sendIngestionFlowFileCompleteEmail(1L, true);
    verify(iufClassificationWFClientMock)
      .notifyPaymentsReporting(new IufClassificationNotifyPaymentsReportingSignalDTO(result.getOrganizationId(), result.getIuf(), result.getTransfers()));
  }

  @Test
  void givenNotRetryableExceptionWhenExecuteWfThenStopExecutionWithoutRetries() {
    WorkflowCreatedDTO wfExec = workflowClient.ingest(1L);
    waitUntilWfFailed(wfExec);

    verify(statusActivitySpy).updateIngestionFlowFileStatus(1L, IngestionFlowFileStatus.UPLOADED, IngestionFlowFileStatus.PROCESSING, null);
    verify(ingestionFlowFileServiceMock).updateStatus(anyLong(), any(), any(), any());
  }

  @Test
  void givenNotRetryableExceptionExtensionWhenExecuteWfThenStopExecutionWithoutRetries() {
    when(ingestionFlowFileServiceMock.updateStatus(anyLong(), any(), any(), any()))
      .thenThrow(new NotRetryableActivityException("extension"){});

    WorkflowCreatedDTO wfExec = workflowClient.ingest(1L);
    waitUntilWfFailed(wfExec);

    verify(statusActivitySpy).updateIngestionFlowFileStatus(1L, IngestionFlowFileStatus.UPLOADED, IngestionFlowFileStatus.PROCESSING, null);
    verify(ingestionFlowFileServiceMock).updateStatus(anyLong(), any(), any(), any());
  }

  @Test
  void givenRetryableExceptionWhenExecuteWfThenRetrieActivityUntilMax() {
    when(ingestionFlowFileServiceMock.updateStatus(anyLong(), any(), any(), any()))
      .thenThrow(new RuntimeException("RetryableActivityException"));

    WorkflowCreatedDTO wfExec = workflowClient.ingest(1L);
    waitUntilWfFailed(wfExec);

    verify(statusActivitySpy, times(3)).updateIngestionFlowFileStatus(1L, IngestionFlowFileStatus.UPLOADED, IngestionFlowFileStatus.PROCESSING, null);
    verify(ingestionFlowFileServiceMock, times(3)).updateStatus(anyLong(), any(), any(), any());
  }

  // PRIVATE METHODS
  private void waitUntilWfCompletion(WorkflowCreatedDTO wfExec) {
    waitUntilWfStatus(wfExec.getWorkflowId(), WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_COMPLETED);
  }

  private void waitUntilWfFailed(WorkflowCreatedDTO wfExec) {
    waitUntilWfStatus(wfExec.getWorkflowId(), WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_FAILED);
  }

  private void waitUntilWfStatus(String workflowId, WorkflowExecutionStatus status) {
    WorkflowExecutionInfo info;
    do {
      info = WorkflowClientHelper.describeWorkflowInstance(temporalClient.getWorkflowServiceStubs(), temporalNamespace, WorkflowExecution.newBuilder().setWorkflowId(workflowId).build(), new NoopScope());
    } while (!wfTerminationStatuses.contains(info.getStatus()));

    Assertions.assertEquals(status, info.getStatus());
  }
}


