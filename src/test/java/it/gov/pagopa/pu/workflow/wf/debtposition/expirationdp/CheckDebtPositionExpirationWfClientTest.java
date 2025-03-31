package it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp;

import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.wfexpiration.CheckDebtPositionExpirationWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.wfexpiration.CheckDebtPositionExpirationWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class CheckDebtPositionExpirationWfClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private CheckDebtPositionExpirationWF checkDebtPositionExpirationWFMock;

  private CheckDebtPositionExpirationWfClient client;

  @BeforeEach
  void init(){
    client = new CheckDebtPositionExpirationWfClientImpl(workflowServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(workflowServiceMock);
  }

  @Test
  void whenCheckDpExpirationThenSuccess() {
    // Given
    Long debtPositionId = 1L;
    String taskQueue = CheckDebtPositionExpirationWFImpl.TASK_QUEUE_CHECK_DEBT_POSITION_EXPIRATION_WF;
    String expectedWorkflowId = "CheckDebtPositionExpirationWF-1";

    try (MockedStatic<Utilities> utilitiesMockedStatic = mockStatic(Utilities.class)) {
      utilitiesMockedStatic
        .when(() -> Utilities.generateWorkflowId(debtPositionId, taskQueue))
        .thenReturn(expectedWorkflowId);

      Mockito.when(workflowServiceMock.buildWorkflowStub(
          CheckDebtPositionExpirationWF.class,
          taskQueue,
          expectedWorkflowId))
        .thenReturn(checkDebtPositionExpirationWFMock);

      // When
      String workflowId = client.checkDpExpiration(debtPositionId);

      // Then
      Assertions.assertEquals(expectedWorkflowId, workflowId);
      Mockito.verify(checkDebtPositionExpirationWFMock).checkDpExpiration(debtPositionId);
    }
  }

  @Test
  void whenScheduleNextCheckDpExpirationThenSuccess() {
    // Given
    Long debtPositionId = 1L;
    LocalDate offsetDateTime = LocalDate.of(2025,1,1);
    String expectedWorkflowId = "CheckDebtPositionExpirationWF-1";

    Mockito.when(workflowServiceMock.buildWorkflowStubScheduled(
        CheckDebtPositionExpirationWF.class,
        CheckDebtPositionExpirationWFImpl.TASK_QUEUE_CHECK_DEBT_POSITION_EXPIRATION_WF,
        expectedWorkflowId,
        offsetDateTime))
      .thenReturn(checkDebtPositionExpirationWFMock);

    // When
    assertDoesNotThrow(() -> client.scheduleNextCheckDpExpiration(debtPositionId, offsetDateTime));

    // Then
    Mockito.verify(checkDebtPositionExpirationWFMock).checkDpExpiration(debtPositionId);
  }

  @Test
  void whenCancelNextScheduleThenInvokeWfService(){
    client.cancelScheduling(1L);

    Mockito.verify(workflowServiceMock)
      .cancelWorkflow("CheckDebtPositionExpirationWF-1");
  }
}
