package it.gov.pagopa.pu.workflow.service.wf.debtposition;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.MassiveDebtPositionIbanUpdateRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowConflictException;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowCompletionService;
import it.gov.pagopa.pu.workflow.service.wf.debtposition.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.CheckDebtPositionExpirationWfClient;
import it.gov.pagopa.pu.workflow.wf.debtposition.massive.MassiveDebtPositionWFClient;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DebtPositionServiceTest {

  @Mock
  private DebtPositionSyncService debtPositionSyncServiceMock;
  @Mock
  private CheckDebtPositionExpirationWfClient checkDebtPositionExpirationWfClientMock;
  @Mock
  private MassiveDebtPositionWFClient massiveDPClientMock;
  @Mock
  private WorkflowCompletionService wfCompletionMock;

  private DebtPositionService service;

  private static final Long ORG_ID = 1L;
  private static final String BASE_WF_ID = "MassiveIbanUpdateWF-" + ORG_ID;
  private static final String SYNC_WF_ID = BASE_WF_ID + "_TO_SYNC";

  @BeforeEach
  void init(){
    service = new DebtPositionServiceImpl(debtPositionSyncServiceMock, checkDebtPositionExpirationWfClientMock, massiveDPClientMock, wfCompletionMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(debtPositionSyncServiceMock, checkDebtPositionExpirationWfClientMock, massiveDPClientMock, wfCompletionMock);
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

  @Test
  void whenMassiveIbanUpdateThenSuccess() {
    // Given
    MassiveDebtPositionIbanUpdateRequestDTO requestDTO = createMassiveIbanUpdateRequestDTO();
    WorkflowCreatedDTO expectedResponse = WorkflowCreatedDTO.builder()
      .workflowId("NEW-WF-ID")
      .runId("RUN-ID")
      .build();

    when(massiveDPClientMock.startMassiveIbanUpdate(
      eq(ORG_ID), any(), any(), any(), any(), any()))
      .thenReturn(expectedResponse);

    // When
    WorkflowCreatedDTO result = service.massiveIbanUpdate(ORG_ID, requestDTO);

    // Then
    assertNotNull(result);
    assertEquals(expectedResponse.getWorkflowId(), result.getWorkflowId());

    verify(wfCompletionMock).checkWorkflowExistsAndNotTerminated(BASE_WF_ID);
    verify(wfCompletionMock).checkWorkflowExistsAndNotTerminated(SYNC_WF_ID);
    verify(massiveDPClientMock).startMassiveIbanUpdate(
      ORG_ID,
      requestDTO.getDebtPositionTypeOrgId(),
      requestDTO.getOldIban(),
      requestDTO.getNewIban(),
      requestDTO.getOldPostalIban(),
      requestDTO.getNewPostalIban()
    );
  }

  @Test
  void whenMassiveIbanUpdateThenThrowsException() {
    // Given
    MassiveDebtPositionIbanUpdateRequestDTO requestDTO = createMassiveIbanUpdateRequestDTO();

    doThrow(new WorkflowConflictException("Conflict"))
      .when(wfCompletionMock).checkWorkflowExistsAndNotTerminated(BASE_WF_ID);

    // When & Then
    assertThrows(WorkflowConflictException.class,
      () -> service.massiveIbanUpdate(ORG_ID, requestDTO));

    verify(wfCompletionMock).checkWorkflowExistsAndNotTerminated(BASE_WF_ID);
    verify(wfCompletionMock, never()).checkWorkflowExistsAndNotTerminated(SYNC_WF_ID);
    verify(massiveDPClientMock, never()).startMassiveIbanUpdate(anyLong(), any(), any(), any(), any(), any());
  }

  private MassiveDebtPositionIbanUpdateRequestDTO createMassiveIbanUpdateRequestDTO() {
    MassiveDebtPositionIbanUpdateRequestDTO dto = new MassiveDebtPositionIbanUpdateRequestDTO();
    dto.setDebtPositionTypeOrgId(10L);
    dto.setOldIban("IT01A");
    dto.setNewIban("IT02B");
    dto.setOldPostalIban("IT03P");
    dto.setNewPostalIban("IT04Q");
    return dto;
  }


}
