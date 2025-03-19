package it.gov.pagopa.pu.workflow.service.debtposition;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.service.debtposition.sync.DebtPositionGenericSyncService;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.CheckDebtPositionExpirationWfClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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
  private DebtPositionGenericSyncService debtPositionGenericSyncServiceMock;
  @Mock
  private CheckDebtPositionExpirationWfClient checkDebtPositionExpirationWfClientMock;

  private DebtPositionService service;

  @BeforeEach
  void init(){
    service = new DebtPositionServiceImpl(debtPositionGenericSyncServiceMock, checkDebtPositionExpirationWfClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(debtPositionGenericSyncServiceMock, checkDebtPositionExpirationWfClientMock);
  }

  @Test
  void givenHandleDPSyncThenOk() {
    // Given
    String accessToken = "ACCESSTOKEN";
    DebtPositionDTO debtPosition = new DebtPositionDTO();
    PaymentEventType paymentEventType = PaymentEventType.DP_CREATED;
    Boolean massive = Boolean.TRUE;

    WorkflowCreatedDTO expectedResult = WorkflowCreatedDTO.builder()
      .workflowId("WFID")
      .build();

    Mockito.when(debtPositionGenericSyncServiceMock.invokeWorkflow(Mockito.same(debtPosition), Mockito.same(paymentEventType), Mockito.same(massive), Mockito.same(accessToken)))
      .thenReturn("WFID");

    // When
    WorkflowCreatedDTO result = service.syncDebtPosition(debtPosition, paymentEventType, massive, accessToken);

    // Then
    Assertions.assertEquals(expectedResult, result);
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
