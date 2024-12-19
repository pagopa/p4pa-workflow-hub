package it.gov.pagopa.pu.workflow.service;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.def.PaymentsReportingIngestionWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.def.PaymentsReportingIngestionWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

  @Mock
  private WorkflowClient workflowClientMock;
  @Mock
  private PaymentsReportingIngestionWF wfMock;

  private WorkflowService workflowService;

  @BeforeEach
  void init(){
    workflowService = new WorkflowService(workflowClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(workflowClientMock, wfMock);
  }

  @Test
  void whenIngestThenOk(){
    // Given
    long ingestionFlowFileId = 1L;
    String workflowId = String.valueOf(ingestionFlowFileId);

    Mockito.when(workflowClientMock.newWorkflowStub(
        Mockito.eq(PaymentsReportingIngestionWF.class),
        Mockito.<WorkflowOptions>argThat(options ->
          PaymentsReportingIngestionWFImpl.TASK_QUEUE.equals(options.getTaskQueue()) &&
            workflowId.equals(options.getWorkflowId())
        )))
      .thenReturn(wfMock);

    // When
    PaymentsReportingIngestionWF result = workflowService.buildWorkflowStub(PaymentsReportingIngestionWF.class, PaymentsReportingIngestionWFImpl.TASK_QUEUE, workflowId);

    // Then
    Assertions.assertSame(wfMock, result);
  }
}
