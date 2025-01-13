package it.gov.pagopa.pu.workflow.wf.debtposition.createdp;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.debtposition.createdp.wfsync.CreateDebtPositionSyncWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.createdp.wfsync.CreateDebtPositionSyncWFImpl;
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
class CreateDebtPositionWfClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private CreateDebtPositionSyncWF wfMock;

  private CreateDebtPositionWfClient client;

  @BeforeEach
  void init(){
    client = new CreateDebtPositionWfClientImpl(workflowServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(workflowServiceMock);
  }

  @Test
  void whenCreateDPSyncThenSuccess() {
    // Given
    long id = 1L;
    String expectedWorkflowId = "CreateDebtPositionWf-1";
    DebtPositionDTO debtPosition = buildDebtPositionDTO();

    try (MockedStatic<Utilities> utilitiesMockedStatic = mockStatic(Utilities.class)) {
      utilitiesMockedStatic
        .when(() -> Utilities.generateWorkflowId(id, CreateDebtPositionSyncWFImpl.TASK_QUEUE))
        .thenReturn(expectedWorkflowId);

      Mockito.when(workflowServiceMock.buildWorkflowStub(
          CreateDebtPositionSyncWF.class,
          CreateDebtPositionSyncWFImpl.TASK_QUEUE,
          expectedWorkflowId))
        .thenReturn(wfMock);

      // When
      String workflowId = client.createDPSync(debtPosition);

      // Then
      Assertions.assertEquals(expectedWorkflowId, workflowId);
      Mockito.verify(wfMock).createDPSync(debtPosition);
    }
  }
}
