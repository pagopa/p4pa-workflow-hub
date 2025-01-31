package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi;

import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.wfingestion.TreasuryOpiIngestionWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.wfingestion.TreasuryOpiIngestionWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TreasuryOpiIngestionWFClientTest {
  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private TreasuryOpiIngestionWF wfMock;

  private TreasuryOpiIngestionWFClient client;

  @BeforeEach
  void setUp(){
    client = new TreasuryOpiIngestionWFClient(workflowServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(workflowServiceMock);
  }

  @Test
  void whenIngestThenOk(){
    // Given
    long ingestionFlowFileId = 1L;
    String expectedWorkflowId = "TreasuryIngestionWF-1";

    try (MockedStatic<Utilities> utilitiesMockedStatic = mockStatic(Utilities.class)) {
      utilitiesMockedStatic
        .when(() -> Utilities.generateWorkflowId(ingestionFlowFileId, TreasuryOpiIngestionWFImpl.TASK_QUEUE_TREASURY_OPI_INGESTION_WF))
        .thenReturn(expectedWorkflowId);

      doReturn(wfMock).when(workflowServiceMock)
        .buildWorkflowStub(TreasuryOpiIngestionWF.class, TreasuryOpiIngestionWFImpl.TASK_QUEUE_TREASURY_OPI_INGESTION_WF, expectedWorkflowId);

      // When
      String workflowId = client.ingest(ingestionFlowFileId);

      // Then
      assertEquals(expectedWorkflowId, workflowId);
      verify(wfMock).ingest(ingestionFlowFileId);
    }
  }
}
