package it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.wfexpiration.CheckDebtPositionExpirationWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.wfexpiration.CheckDebtPositionExpirationWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
class CheckDebtPositionExpirationWfClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private CheckDebtPositionExpirationWF checkDebtPositionExpirationWFMock;

  private CheckDebtPositionExpirationWfClient client;

  @BeforeEach
  void init() {
    client = new CheckDebtPositionExpirationWfClientImpl(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void whenCheckDpExpirationThenSuccess() {
    // Given
    Long debtPositionId = 1L;
    String taskQueue = CheckDebtPositionExpirationWFImpl.TASK_QUEUE_CHECK_DEBT_POSITION_EXPIRATION_WF;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("CheckDebtPositionExpirationWF-1", "RUNID");

    Mockito.when(workflowServiceMock.buildWorkflowStub(
        CheckDebtPositionExpirationWF.class,
        taskQueue,
        expectedResult.getWorkflowId()))
      .thenReturn(checkDebtPositionExpirationWFMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, debtPositionId);

    // When
    WorkflowCreatedDTO result = client.checkDpExpiration(debtPositionId);

    // Then
    Assertions.assertEquals(expectedResult, result);
    Mockito.verify(checkDebtPositionExpirationWFMock).checkDpExpiration(debtPositionId);
  }

  @Test
  void whenScheduleNextCheckDpExpirationThenSuccess() {
    // Given
    Long debtPositionId = 1L;
    LocalDate offsetDateTime = LocalDate.of(2025, 1, 1);
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("CheckDebtPositionExpirationWF-1", "runId");

    Mockito.when(workflowServiceMock.buildWorkflowStubScheduled(
        CheckDebtPositionExpirationWF.class,
        CheckDebtPositionExpirationWFImpl.TASK_QUEUE_CHECK_DEBT_POSITION_EXPIRATION_WF,
        expectedResult.getWorkflowId(),
        offsetDateTime))
      .thenReturn(checkDebtPositionExpirationWFMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, debtPositionId);

    // When
    client.scheduleNextCheckDpExpiration(debtPositionId, offsetDateTime);

    // Then
    Mockito.verify(checkDebtPositionExpirationWFMock).checkDpExpiration(debtPositionId);
  }

  @Test
  void whenCancelNextScheduleThenInvokeWfService() {
    client.cancelScheduling(1L);

    Mockito.verify(workflowServiceMock)
      .cancelWorkflow("CheckDebtPositionExpirationWF-1");
  }
}
