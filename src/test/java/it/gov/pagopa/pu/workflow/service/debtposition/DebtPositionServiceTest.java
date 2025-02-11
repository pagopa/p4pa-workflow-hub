package it.gov.pagopa.pu.workflow.service.debtposition;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.CheckDebtPositionExpirationWfClient;
import it.gov.pagopa.pu.workflow.wf.debtposition.handledp.SynchronizeSyncWfClient;
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
  private SynchronizeSyncWfClient synchronizeSyncWfClientMock;
  @Mock
  private SynchronizeSyncAcaWfClient synchronizeSyncAcaWfClientMock;
  @Mock
  private CheckDebtPositionExpirationWfClient checkDebtPositionExpirationWfClientMock;

  private DebtPositionService service;

  @BeforeEach
  void init(){
    service = new DebtPositionServiceImpl(synchronizeSyncWfClientMock, synchronizeSyncAcaWfClientMock, checkDebtPositionExpirationWfClientMock);
  }

  @Test
  void givenHandleDPSyncThenOk() {
    testWorkflowDP(
      debtPositionDTO -> synchronizeSyncWfClientMock.handleDPSync(debtPositionDTO),
      debtPositionRequestDTO -> service.syncDebtPosition(debtPositionRequestDTO)
    );
  }

  @Test
  void givenHandleDPSyncAcaThenOk() {
    testWorkflowDP(
      debtPositionDTO -> synchronizeSyncAcaWfClientMock.synchronizeDPSyncAca(debtPositionDTO),
      debtPositionRequestDTO -> service.alignDpSyncAca(debtPositionRequestDTO)
    );
  }

  @Test
  void givenCheckDPExpirationThenOk() {
    testWorkflowDP(
      debtPositionDTO -> checkDebtPositionExpirationWfClientMock.checkDpExpiration(1L),
      debtPositionRequestDTO -> service.checkDpExpiration(1L)
    );
  }

  private void testWorkflowDP(Function<DebtPositionDTO, String> clientMockSetup, Function<DebtPositionDTO, WorkflowCreatedDTO> serviceMethod) {
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
