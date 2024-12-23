package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting;

import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion.PaymentsReportingIngestionWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion.PaymentsReportingIngestionWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentsReportingIngestionWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private PaymentsReportingIngestionWF wfMock;

  private PaymentsReportingIngestionWFClient client;

  @BeforeEach
  void init(){
    client = new PaymentsReportingIngestionWFClient(workflowServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(workflowServiceMock);
  }

  @Test
  void whenIngestThenOk(){
    // Given
    long ingestionFlowFileId = 1L;
    String expectedWorkflowId = String.valueOf(ingestionFlowFileId);

    Mockito.when(workflowServiceMock.buildWorkflowStub(PaymentsReportingIngestionWF.class, PaymentsReportingIngestionWFImpl.TASK_QUEUE, expectedWorkflowId))
      .thenReturn(wfMock);

    // When
    String workflowId = client.ingest(ingestionFlowFileId);

    // Then
    Assertions.assertEquals(expectedWorkflowId, workflowId);
    Mockito.verify(wfMock).ingest(ingestionFlowFileId);
  }
}
