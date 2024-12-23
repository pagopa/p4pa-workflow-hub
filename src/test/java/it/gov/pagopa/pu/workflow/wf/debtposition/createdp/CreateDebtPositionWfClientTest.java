package it.gov.pagopa.pu.workflow.wf.debtposition.createdp;

import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.debtposition.createdp.wfsync.CreateDebtPositionSyncWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.createdp.wfsync.CreateDebtPositionSyncWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.workflow.utilities.faker.DebtPositionFaker.buildDebtPositionDTO;

@ExtendWith(MockitoExtension.class)
class CreateDebtPositionWfClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private CreateDebtPositionSyncWF wfMock;

  private CreateDebtPositionWfClient client;

  @BeforeEach
  void init(){
    client = new CreateDebtPositionWfClient(workflowServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(workflowServiceMock);
  }

  @Test
  void whenCreateDPSyncThenSuccess(){
    long id = 1L;
    String expectedWorkflowId = String.valueOf(id);
    DebtPositionDTO debtPosition = buildDebtPositionDTO();

    Mockito.when(workflowServiceMock.buildWorkflowStub(CreateDebtPositionSyncWF.class, CreateDebtPositionSyncWFImpl.TASK_QUEUE, expectedWorkflowId))
      .thenReturn(wfMock);

    String workflowId = client.createDPSync(debtPosition);

    Assertions.assertEquals(expectedWorkflowId, workflowId);
    Mockito.verify(wfMock).createDPSync(debtPosition);
  }
}
