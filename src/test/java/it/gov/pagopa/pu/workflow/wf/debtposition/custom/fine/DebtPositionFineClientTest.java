package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine;

import it.gov.pagopa.payhub.activities.dto.IONotificationMessage;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
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
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private FineReductionOptionExpirationWF fineReductionOptionExpirationWFMock;
  @Mock
  private SynchronizeFineWF synchronizeFineWFMock;

  private DebtPositionFineClient client;

  @BeforeEach
  void init() {
    client = new DebtPositionFineClientImpl(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void whenExpireFineReductionThenSuccess() {
    // Given
    Long debtPositionId = 1L;
    String taskQueue = TaskQueueConstants.TASK_QUEUE_DP_RESERVED_CUSTOM_SYNC;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("FineReductionOptionExpirationWF-1", "runId");
    FineWfExecutionConfig.IONotificationFineWfMessages fineWfMessages =
      new FineWfExecutionConfig.IONotificationFineWfMessages(null, new IONotificationMessage("subject", "message"));

    FineWfExecutionConfig wfExecutionConfig = new FineWfExecutionConfig();
    wfExecutionConfig.setIoMessages(fineWfMessages);

    Mockito.when(workflowServiceMock.buildWorkflowStubToStartNew(
        FineReductionOptionExpirationWF.class,
        taskQueue,
        expectedResult.getWorkflowId()))
      .thenReturn(fineReductionOptionExpirationWFMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, debtPositionId, wfExecutionConfig);

    // When
    WorkflowCreatedDTO result = client.expireFineReduction(debtPositionId, wfExecutionConfig);

    // Then
    Assertions.assertSame(expectedResult, result);
    Mockito.verify(fineReductionOptionExpirationWFMock).expireFineReduction(debtPositionId, wfExecutionConfig);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, FineReductionOptionExpirationWFImpl.class);
  }

  @Test
  void whenScheduleExpireFineReductionThenSuccess() {
    // Given
    Long debtPositionId = 1L;
    String taskQueue = TaskQueueConstants.TASK_QUEUE_DP_RESERVED_CUSTOM_SYNC;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("FineReductionOptionExpirationWF-1", "runId");
    FineWfExecutionConfig.IONotificationFineWfMessages fineWfMessages =
      new FineWfExecutionConfig.IONotificationFineWfMessages(null, new IONotificationMessage("subject", "message"));

    FineWfExecutionConfig wfExecutionConfig = new FineWfExecutionConfig();
    wfExecutionConfig.setIoMessages(fineWfMessages);

    Mockito.when(workflowServiceMock.buildWorkflowStubScheduled(
        FineReductionOptionExpirationWF.class,
        taskQueue,
        expectedResult.getWorkflowId(),
        OFFSET_DATE_TIME))
      .thenReturn(fineReductionOptionExpirationWFMock);

    Mockito.when(workflowClientServiceMock.start(TemporalTestUtils.buildArgumentMatcherProc(debtPositionId, wfExecutionConfig), Mockito.same(debtPositionId), Mockito.same(wfExecutionConfig)))
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = client.scheduleExpireFineReduction(debtPositionId, wfExecutionConfig, OFFSET_DATE_TIME);

    // Then
    Assertions.assertSame(expectedResult, result);
    Mockito.verify(fineReductionOptionExpirationWFMock).expireFineReduction(debtPositionId, wfExecutionConfig);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, FineReductionOptionExpirationWFImpl.class);
  }

  @Test
  void whenSynchronizeFineDPThenSuccess() {
    // Given
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    PaymentEventRequestDTO paymentEventRequest = new PaymentEventRequestDTO();
    String taskQueue = TaskQueueConstants.TASK_QUEUE_DP_RESERVED_CUSTOM_SYNC;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("SynchronizeFineWF-1", "runId");
    FineWfExecutionConfig.IONotificationFineWfMessages fineWfMessages =
      new FineWfExecutionConfig.IONotificationFineWfMessages(null, new IONotificationMessage("subject", "message"));
    Boolean massive = false;

    FineWfExecutionConfig wfExecutionConfig = new FineWfExecutionConfig();
    wfExecutionConfig.setIoMessages(fineWfMessages);

    Mockito.when(workflowServiceMock.buildWorkflowStubToStartNew(
        SynchronizeFineWF.class,
        taskQueue,
        expectedResult.getWorkflowId()))
      .thenReturn(synchronizeFineWFMock);

    Mockito.when(workflowClientServiceMock.start(TemporalTestUtils.buildArgumentMatcherProc(debtPositionDTO, paymentEventRequest, massive, wfExecutionConfig)
        , Mockito.same(debtPositionDTO), Mockito.same(paymentEventRequest), Mockito.same(massive), Mockito.same(wfExecutionConfig)))
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = client.synchronizeFineDP(debtPositionDTO, paymentEventRequest, massive, wfExecutionConfig);

    // Then
    Assertions.assertSame(expectedResult, result);
    Mockito.verify(synchronizeFineWFMock).synchronizeFineDP(debtPositionDTO, paymentEventRequest, massive, wfExecutionConfig);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, SynchronizeFineWFImpl.class);
  }
}
