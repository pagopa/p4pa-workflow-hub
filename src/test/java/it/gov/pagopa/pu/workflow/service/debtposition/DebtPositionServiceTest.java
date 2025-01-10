package it.gov.pagopa.pu.workflow.service.debtposition;

import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.DebtPositionRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.createdp.CreateDebtPositionWfClient;
import it.gov.pagopa.pu.workflow.wf.debtposition.mapper.DebtPositionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionRequestDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class DebtPositionServiceTest {

  @Mock
  private CreateDebtPositionWfClient clientMock;
  @Mock
  private DebtPositionMapper debtPositionMapperMock;

  private DebtPositionService service;

  @BeforeEach
  void init(){
    service = new DebtPositionServiceImpl(clientMock, debtPositionMapperMock);
  }

  @Test
  void givenCreateDPSyncThenOk(){
    String workflowId = "workflow-1";
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionRequestDTO debtPositionRequestDTO = buildDebtPositionRequestDTO();

    Mockito.when(clientMock.createDPSync(debtPositionDTO)).thenReturn(workflowId);

    Mockito.when(debtPositionMapperMock.map(debtPositionRequestDTO)).thenReturn(debtPositionDTO);

    WorkflowCreatedDTO workflowCreatedDTO = service.createDPSync(debtPositionRequestDTO);

    assertNotNull(workflowCreatedDTO);
    assertEquals(workflowId, workflowCreatedDTO.getWorkflowId());
  }
}
