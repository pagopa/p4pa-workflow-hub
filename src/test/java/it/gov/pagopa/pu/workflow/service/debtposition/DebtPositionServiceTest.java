package it.gov.pagopa.pu.workflow.service.debtposition;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
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

import java.util.function.Function;

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
  void givenCreateDPSyncThenOk() {
    testWorkflowCreationDP(
      debtPositionDTO -> clientMock.createDPSync(debtPositionDTO),
      debtPositionRequestDTO -> service.createDPSync(debtPositionRequestDTO)
    );
  }

  @Test
  void givenCreateDPSyncAcaThenOk() {
    testWorkflowCreationDP(
      debtPositionDTO -> clientMock.createDPSyncAca(debtPositionDTO),
      debtPositionRequestDTO -> service.createDPSyncAca(debtPositionRequestDTO)
    );
  }

  private void testWorkflowCreationDP(Function<DebtPositionDTO, String> clientMockSetup, Function<DebtPositionRequestDTO, WorkflowCreatedDTO> serviceMethod) {
    // when
    String workflowId = "workflow-1";
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionRequestDTO debtPositionRequestDTO = buildDebtPositionRequestDTO();

    Mockito.when(debtPositionMapperMock.map(debtPositionRequestDTO)).thenReturn(debtPositionDTO);

    Mockito.when(clientMockSetup.apply(debtPositionDTO)).thenReturn("workflow-1");

    // given
    WorkflowCreatedDTO workflowCreatedDTO = serviceMethod.apply(debtPositionRequestDTO);

    // then
    assertNotNull(workflowCreatedDTO);
    assertEquals(workflowId, workflowCreatedDTO.getWorkflowId());
  }
}
