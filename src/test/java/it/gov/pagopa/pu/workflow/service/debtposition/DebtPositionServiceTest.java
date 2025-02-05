package it.gov.pagopa.pu.workflow.service.debtposition;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.aligndp.SynchronizeSyncAcaWfClient;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.HandleDebtPositionExpirationWfClient;
import it.gov.pagopa.pu.workflow.wf.debtposition.handledp.HandleDebtPositionWfClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Function;

import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class DebtPositionServiceTest {

  @Mock
  private HandleDebtPositionWfClient handleDebtPositionWfClientMock;
  @Mock
  private SynchronizeSyncAcaWfClient synchronizeSyncAcaWfClientMock;
  @Mock
  private HandleDebtPositionExpirationWfClient handleDebtPositionExpirationWfClient;

  private DebtPositionService service;

  @BeforeEach
  void init(){
    service = new DebtPositionServiceImpl(handleDebtPositionWfClientMock, synchronizeSyncAcaWfClientMock, handleDebtPositionExpirationWfClient);
  }

  @Test
  void givenHandleDPSyncThenOk() {
    testWorkflowCreationDP(
      debtPositionDTO -> handleDebtPositionWfClientMock.handleDPSync(debtPositionDTO),
      debtPositionRequestDTO -> service.handleDPSync(debtPositionRequestDTO)
    );
  }

  @Test
  void givenHandleDPSyncAcaThenOk() {
    testWorkflowCreationDP(
      debtPositionDTO -> synchronizeSyncAcaWfClientMock.synchronizeDPSyncAca(debtPositionDTO),
      debtPositionRequestDTO -> service.alignDpSyncAca(debtPositionRequestDTO)
    );
  }

  private void testWorkflowCreationDP(Function<DebtPositionDTO, String> clientMockSetup, Function<DebtPositionDTO, WorkflowCreatedDTO> serviceMethod) {
    // when
    String workflowId = "workflow-1";
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    Mockito.when(clientMockSetup.apply(debtPositionDTO)).thenReturn("workflow-1");

    // given
    WorkflowCreatedDTO workflowCreatedDTO = serviceMethod.apply(debtPositionDTO);

    // then
    assertNotNull(workflowCreatedDTO);
    assertEquals(workflowId, workflowCreatedDTO.getWorkflowId());
  }
}
