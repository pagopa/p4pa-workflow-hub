package it.gov.pagopa.pu.workflow.wf.debtposition.handledp;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.debtposition.handledp.wfsync.HandleDebtPositionSyncWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.handledp.wfsync.HandleDebtPositionSyncWFImpl;
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
class HandleDebtPositionWfClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private HandleDebtPositionSyncWF wfSyncMock;

  private HandleDebtPositionWfClient client;

  @BeforeEach
  void init(){
    client = new HandleDebtPositionWfClientImpl(workflowServiceMock);
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
        .when(() -> Utilities.generateWorkflowId(id, HandleDebtPositionSyncWFImpl.TASK_QUEUE_HANDLE_DEBT_POSITION_SYNC_WF))
        .thenReturn(expectedWorkflowId);

      Mockito.when(workflowServiceMock.buildWorkflowStub(
          HandleDebtPositionSyncWF.class,
          HandleDebtPositionSyncWFImpl.TASK_QUEUE_HANDLE_DEBT_POSITION_SYNC_WF,
          expectedWorkflowId))
        .thenReturn(wfSyncMock);

      // When
      String workflowId = client.handleDPSync(debtPosition);

      // Then
      Assertions.assertEquals(expectedWorkflowId, workflowId);
      Mockito.verify(wfSyncMock).handleDPSync(debtPosition);
    }
  }
}
