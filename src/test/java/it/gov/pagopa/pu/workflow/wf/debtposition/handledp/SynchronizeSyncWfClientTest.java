package it.gov.pagopa.pu.workflow.wf.debtposition.handledp;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync.SynchronizeSyncWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync.SynchronizeSyncWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionDTO;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class SynchronizeSyncWfClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private SynchronizeSyncWF wfSyncMock;

  private SynchronizeSyncWfClient client;

  @BeforeEach
  void init(){
    client = new SynchronizeSyncWfClientImpl(workflowServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(workflowServiceMock);
  }

  @Test
  void whenHandleDPSyncThenSuccess() {
    // Given
    long id = 1L;
    String expectedWorkflowId = "CreateDebtPositionSyncWF-1";
    DebtPositionDTO debtPosition = buildDebtPositionDTO();

    try (MockedStatic<Utilities> utilitiesMockedStatic = mockStatic(Utilities.class)) {
      utilitiesMockedStatic
        .when(() -> Utilities.generateWorkflowId(id, SynchronizeSyncWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_SYNC_WF))
        .thenReturn(expectedWorkflowId);

      Mockito.when(workflowServiceMock.buildWorkflowStub(
          SynchronizeSyncWF.class,
          SynchronizeSyncWFImpl.TASK_QUEUE_SYNCHRONIZE_DP_SYNC_WF,
          expectedWorkflowId))
        .thenReturn(wfSyncMock);

      // When
      String workflowId = client.handleDPSync(debtPosition);

      // Then
      Assertions.assertEquals(expectedWorkflowId, workflowId);
      Mockito.verify(wfSyncMock).synchronizeDpSync(debtPosition);
    }
  }
}
