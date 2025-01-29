package it.gov.pagopa.pu.workflow.wf.debtposition.aligndp;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.debtposition.aligndp.wfsyncstandin.SynchronizeSyncAcaWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.aligndp.wfsyncstandin.SynchronizeSyncAcaWFImpl;
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
class SynchronizeSyncAcaWfClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private SynchronizeSyncAcaWF wfSyncAcaMock;

  private SynchronizeSyncAcaWfClient client;

  @BeforeEach
  void init(){
    client = new SynchronizeSyncAcaWfClientImpl(workflowServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(workflowServiceMock);
  }

  @Test
  void whenCreateDPSyncAcaThenSuccess() {
    // Given
    long id = 1L;
    String expectedWorkflowId = "CreateDebtPositionSyncAcaWF-1";
    DebtPositionDTO debtPosition = buildDebtPositionDTO();

    try (MockedStatic<Utilities> utilitiesMockedStatic = mockStatic(Utilities.class)) {
      utilitiesMockedStatic
        .when(() -> Utilities.generateWorkflowId(id, SynchronizeSyncAcaWFImpl.TASK_QUEUE))
        .thenReturn(expectedWorkflowId);

      Mockito.when(workflowServiceMock.buildWorkflowStub(
          SynchronizeSyncAcaWF.class,
          SynchronizeSyncAcaWFImpl.TASK_QUEUE,
          expectedWorkflowId))
        .thenReturn(wfSyncAcaMock);

      // When
      String workflowId = client.synchronizeDPSyncAca(debtPosition);

      // Then
      Assertions.assertEquals(expectedWorkflowId, workflowId);
      Mockito.verify(wfSyncAcaMock).synchronizeDPSyncAca(debtPosition);
    }
  }
}
