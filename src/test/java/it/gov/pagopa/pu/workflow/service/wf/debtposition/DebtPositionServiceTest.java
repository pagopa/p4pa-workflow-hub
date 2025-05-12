package it.gov.pagopa.pu.workflow.service.wf.debtposition;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.wf.debtposition.sync.DebtPositionSyncService;
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
  private DebtPositionSyncService debtPositionSyncServiceMock;
  @Mock
  private CheckDebtPositionExpirationWfClient checkDebtPositionExpirationWfClientMock;

  private DebtPositionService service;

  @BeforeEach
  void init(){
    service = new DebtPositionServiceImpl(debtPositionSyncServiceMock, checkDebtPositionExpirationWfClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(debtPositionSyncServiceMock, checkDebtPositionExpirationWfClientMock);
  }

  @Test
  void givenHandleDPSyncThenOk() {
    // Given
    String accessToken = "ACCESSTOKEN";
    DebtPositionDTO debtPosition = new DebtPositionDTO();
    PaymentEventRequestDTO paymentEventRequest = new PaymentEventRequestDTO(PaymentEventType.DP_CREATED, null);
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();

    WorkflowCreatedDTO expectedResult = WorkflowCreatedDTO.builder()
      .workflowId("WFID")
      .runId("RUNID")
      .build();

    Mockito.when(debtPositionSyncServiceMock.invokeWorkflow(Mockito.same(debtPosition), Mockito.same(paymentEventRequest), Mockito.same(wfExecutionParameters), Mockito.same(accessToken)))
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = service.syncDebtPosition(debtPosition, paymentEventRequest, wfExecutionParameters, accessToken);

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

  private void testWorkflowDP(Function<DebtPositionDTO, WorkflowCreatedDTO> clientMockSetup, Function<DebtPositionDTO, WorkflowCreatedDTO> serviceMethod) {
    // when
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflow-1", "runId");
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    Mockito.when(clientMockSetup.apply(debtPositionDTO)).thenReturn(expectedResult);

    // given
    WorkflowCreatedDTO result = serviceMethod.apply(debtPositionDTO);

    // then
    assertNotNull(result);
    assertEquals(expectedResult, result);
  }

}
