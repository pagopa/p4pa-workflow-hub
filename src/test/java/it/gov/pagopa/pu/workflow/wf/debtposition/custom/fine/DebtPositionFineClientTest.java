package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine;

import it.gov.pagopa.payhub.activities.dto.IONotificationMessage;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.wfreductionexpiration.FineReductionOptionExpirationWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.wfreductionexpiration.FineReductionOptionExpirationWFImpl;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.wfsynchronizefine.SynchronizeFineWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.wfsynchronizefine.SynchronizeFineWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.OFFSET_DATE_TIME;
import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionDTO;

@ExtendWith(MockitoExtension.class)
class DebtPositionFineClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private FineReductionOptionExpirationWF fineReductionOptionExpirationWFMock;
  @Mock
  private SynchronizeFineWF synchronizeFineWFMock;

  private DebtPositionFineClient client;

  @BeforeEach
  void init() {
    client = new DebtPositionFineClientImpl(workflowServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock);
  }

  @Test
  void whenExpireFineReductionThenSuccess() {
    // Given
    Long debtPositionId = 1L;
    String taskQueue = FineReductionOptionExpirationWFImpl.TASK_QUEUE_FINE_REDUCTION_OPTION_EXPIRATION;
    String expectedWorkflowId = "FineReductionOptionExpirationWF-1";
    FineWfExecutionConfig.IONotificationFineWfMessages fineWfMessages =
      new FineWfExecutionConfig.IONotificationFineWfMessages(null, new IONotificationMessage("subject", "message"));

    FineWfExecutionConfig wfExecutionConfig = new FineWfExecutionConfig();
    wfExecutionConfig.setIoMessages(fineWfMessages);

    Mockito.when(workflowServiceMock.buildWorkflowStub(
        FineReductionOptionExpirationWF.class,
        taskQueue,
        expectedWorkflowId))
      .thenReturn(fineReductionOptionExpirationWFMock);

    // When
    String workflowId = client.expireFineReduction(debtPositionId, wfExecutionConfig);

    // Then
    Assertions.assertEquals(expectedWorkflowId, workflowId);
    Mockito.verify(fineReductionOptionExpirationWFMock).expireFineReduction(debtPositionId, wfExecutionConfig);
  }

  @Test
  void whenScheduleExpireFineReductionThenSuccess() {
    // Given
    Long debtPositionId = 1L;
    String taskQueue = FineReductionOptionExpirationWFImpl.TASK_QUEUE_FINE_REDUCTION_OPTION_EXPIRATION;
    String expectedWorkflowId = "FineReductionOptionExpirationWF-1";
    FineWfExecutionConfig.IONotificationFineWfMessages fineWfMessages =
      new FineWfExecutionConfig.IONotificationFineWfMessages(null, new IONotificationMessage("subject", "message"));

    FineWfExecutionConfig wfExecutionConfig = new FineWfExecutionConfig();
    wfExecutionConfig.setIoMessages(fineWfMessages);

    Mockito.when(workflowServiceMock.buildWorkflowStubScheduled(
        FineReductionOptionExpirationWF.class,
        taskQueue,
        expectedWorkflowId,
        OFFSET_DATE_TIME))
      .thenReturn(fineReductionOptionExpirationWFMock);

    // When
    String workflowId = client.scheduleExpireFineReduction(debtPositionId, wfExecutionConfig, OFFSET_DATE_TIME);

    // Then
    Assertions.assertEquals(expectedWorkflowId, workflowId);
    Mockito.verify(fineReductionOptionExpirationWFMock).expireFineReduction(debtPositionId, wfExecutionConfig);
  }

  @Test
  void whenSynchronizeFineThenSuccess() {
    // Given
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    PaymentEventRequestDTO paymentEventRequest = new PaymentEventRequestDTO();
    String taskQueue = SynchronizeFineWFImpl.TASK_QUEUE_SYNC_FINE;
    String expectedWorkflowId = "SynchronizeFineWF-1";
    FineWfExecutionConfig.IONotificationFineWfMessages fineWfMessages =
      new FineWfExecutionConfig.IONotificationFineWfMessages(null, new IONotificationMessage("subject", "message"));

    FineWfExecutionConfig wfExecutionConfig = new FineWfExecutionConfig();
    wfExecutionConfig.setIoMessages(fineWfMessages);

    Mockito.when(workflowServiceMock.buildWorkflowStub(
        SynchronizeFineWF.class,
        taskQueue,
        expectedWorkflowId))
      .thenReturn(synchronizeFineWFMock);

    // When
    String workflowId = client.synchronizeFine(debtPositionDTO, paymentEventRequest, false, wfExecutionConfig);

    // Then
    Assertions.assertEquals(expectedWorkflowId, workflowId);
    Mockito.verify(synchronizeFineWFMock).synchronizeFine(debtPositionDTO, paymentEventRequest, false, wfExecutionConfig);
  }
}
