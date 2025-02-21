package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition;

import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.wfingestion.DebtPositionIngestionFlowWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.wfingestion.DebtPositionIngestionFlowWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class DebtPositionIngestionWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private DebtPositionIngestionFlowWF wfMock;

  private DebtPositionIngestionWFClient client;

  @BeforeEach
  void init(){
    client = new DebtPositionIngestionWFClient(workflowServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(workflowServiceMock);
  }

  @Test
  void whenIngestThenOk(){
    // Given
    long ingestionFlowFileId = 1L;
    String taskQueue = DebtPositionIngestionFlowWFImpl.TASK_QUEUE_DEBT_POSITION_INGESTION_FLOW;
    String expectedWorkflowId = "DebtPositionIngestionWF-1";

    try (MockedStatic<Utilities> utilitiesMockedStatic = mockStatic(Utilities.class)) {
      utilitiesMockedStatic
        .when(() -> Utilities.generateWorkflowId(ingestionFlowFileId, taskQueue))
        .thenReturn(expectedWorkflowId);

      Mockito.when(workflowServiceMock.buildWorkflowStub(DebtPositionIngestionFlowWF.class, taskQueue, expectedWorkflowId))
        .thenReturn(wfMock);

      // When
      String workflowId = client.ingest(ingestionFlowFileId);

      // Then
      Assertions.assertEquals(expectedWorkflowId, workflowId);
      Mockito.verify(wfMock).ingest(ingestionFlowFileId);
    }
  }
}
