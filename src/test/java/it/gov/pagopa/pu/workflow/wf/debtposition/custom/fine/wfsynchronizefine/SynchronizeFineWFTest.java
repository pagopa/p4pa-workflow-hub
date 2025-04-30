package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.wfsynchronizefine;

import it.gov.pagopa.payhub.activities.activity.debtposition.custom.fine.DebtPositionSynchronizeFineActivity;
import it.gov.pagopa.payhub.activities.dto.IONotificationMessage;
import it.gov.pagopa.payhub.activities.dto.debtposition.HandleFineDebtPositionResult;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.activity.InvokeSyncDebtPositionActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.activity.CancelReductionExpirationScheduleActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.activity.ScheduleReductionExpirationActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.config.DebtPositionFineWfConfig;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.mapper.FineWfExecutionConfigMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.OFFSET_DATE_TIME;
import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionDTO;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SynchronizeFineWFTest {

  @Mock
  private DebtPositionSynchronizeFineActivity debtPositionSynchronizeFineActivityMock;
  @Mock
  private InvokeSyncDebtPositionActivity invokeSyncDebtPositionActivityMock;
  @Mock
  private CancelReductionExpirationScheduleActivity cancelReductionExpirationScheduleActivityMock;
  @Mock
  private ScheduleReductionExpirationActivity scheduleReductionExpirationActivityMock;

  private SynchronizeFineWFImpl wf;

  @BeforeEach
  void init() {
    DebtPositionFineWfConfig configMock = Mockito.mock(DebtPositionFineWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(configMock.buildDebtPositionSynchronizeFineActivityStub())
      .thenReturn(debtPositionSynchronizeFineActivityMock);

    Mockito.when(configMock.buildInvokeSyncDebtPositionActivityStub())
      .thenReturn(invokeSyncDebtPositionActivityMock);

    Mockito.when(configMock.buildCancelReductionExpirationScheduleActivityStub())
      .thenReturn(cancelReductionExpirationScheduleActivityMock);

    Mockito.when(configMock.buildScheduleReductionExpirationActivityStub())
      .thenReturn(scheduleReductionExpirationActivityMock);

    Mockito.when(applicationContextMock.getBean(DebtPositionFineWfConfig.class))
      .thenReturn(configMock);

    wf = new SynchronizeFineWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      debtPositionSynchronizeFineActivityMock,
      invokeSyncDebtPositionActivityMock,
      cancelReductionExpirationScheduleActivityMock,
      scheduleReductionExpirationActivityMock
    );
  }

  @Test
  void givenDPStatusPaidWhenSynchronizeFineDPThenCancelReductionExpirationSchedule() {
    // Given
    String workflowId = "workflowId";
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.PAID);
    GenericWfExecutionConfig wfExecutionConfig =
      new GenericWfExecutionConfig(new GenericWfExecutionConfig.IONotificationBaseOpsMessages(new IONotificationMessage("subject", "message"), null, null));
    FineWfExecutionConfig.IONotificationFineWfMessages fineWfMessages =
      new FineWfExecutionConfig.IONotificationFineWfMessages(null, new IONotificationMessage("subject", "message"));
    FineWfExecutionConfig fineWfExecutionConfig = new FineWfExecutionConfig();
    fineWfExecutionConfig.setIoMessages(fineWfMessages);
    PaymentEventRequestDTO paymentEventRequest = new PaymentEventRequestDTO();

    Mockito.when(debtPositionSynchronizeFineActivityMock.handleFineDebtPosition(debtPositionDTO, false, fineWfExecutionConfig))
      .thenReturn(new HandleFineDebtPositionResult(debtPositionDTO, OFFSET_DATE_TIME, false));

    try (
      MockedStatic<FineWfExecutionConfigMapper> mapperMock = Mockito.mockStatic(FineWfExecutionConfigMapper.class)) {
      mapperMock.when(() -> FineWfExecutionConfigMapper.mapNotifiedInstallment(fineWfExecutionConfig, debtPositionDTO))
        .thenReturn(wfExecutionConfig);

      Mockito.when(invokeSyncDebtPositionActivityMock.synchronizeDPSync(debtPositionDTO, paymentEventRequest, false, wfExecutionConfig))
        .thenReturn(workflowId);

      // When
      wf.synchronizeFineDP(debtPositionDTO, paymentEventRequest, false, fineWfExecutionConfig);

      // Then
      verify(cancelReductionExpirationScheduleActivityMock, times(1)).cancelReductionPeriodExpirationScheduling(debtPositionDTO.getDebtPositionId());
    }
  }

  @Test
  void givenDPStatusPartiallyPaidWhenSynchronizeFineDPThenCancelReductionExpirationSchedule() {
    // Given
    String workflowId = "workflowId";
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.PARTIALLY_PAID);
    GenericWfExecutionConfig wfExecutionConfig =
      new GenericWfExecutionConfig(new GenericWfExecutionConfig.IONotificationBaseOpsMessages(new IONotificationMessage("subject", "message"), null, null));
    FineWfExecutionConfig.IONotificationFineWfMessages fineWfMessages =
      new FineWfExecutionConfig.IONotificationFineWfMessages(null, new IONotificationMessage("subject", "message"));
    FineWfExecutionConfig fineWfExecutionConfig = new FineWfExecutionConfig();
    fineWfExecutionConfig.setIoMessages(fineWfMessages);
    PaymentEventRequestDTO paymentEventRequest = new PaymentEventRequestDTO();

    Mockito.when(debtPositionSynchronizeFineActivityMock.handleFineDebtPosition(debtPositionDTO, false, fineWfExecutionConfig))
      .thenReturn(new HandleFineDebtPositionResult(debtPositionDTO, OFFSET_DATE_TIME, false));

    try (
      MockedStatic<FineWfExecutionConfigMapper> mapperMock = Mockito.mockStatic(FineWfExecutionConfigMapper.class)) {
      mapperMock.when(() -> FineWfExecutionConfigMapper.mapNotifiedInstallment(fineWfExecutionConfig, debtPositionDTO))
        .thenReturn(wfExecutionConfig);

      Mockito.when(invokeSyncDebtPositionActivityMock.synchronizeDPSync(debtPositionDTO, paymentEventRequest, false, wfExecutionConfig))
        .thenReturn(workflowId);

      // When
      wf.synchronizeFineDP(debtPositionDTO, paymentEventRequest, false, fineWfExecutionConfig);

      // Then
      verify(cancelReductionExpirationScheduleActivityMock, times(1)).cancelReductionPeriodExpirationScheduling(debtPositionDTO.getDebtPositionId());
    }
  }

  @Test
  void givenDPNotifiedWhenSynchronizeFineDPThenCancelAndRescheduleReductionExpiration() {
    // Given
    String workflowId = "workflowId";
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    GenericWfExecutionConfig wfExecutionConfig =
      new GenericWfExecutionConfig(new GenericWfExecutionConfig.IONotificationBaseOpsMessages(new IONotificationMessage("subject", "message"), null, null));
    FineWfExecutionConfig fineWfExecutionConfig = new FineWfExecutionConfig();
    fineWfExecutionConfig.setIoMessages(new FineWfExecutionConfig.IONotificationFineWfMessages(null, new IONotificationMessage("subject", "message")));
    PaymentEventRequestDTO paymentEventRequest = new PaymentEventRequestDTO();

    HandleFineDebtPositionResult result = new HandleFineDebtPositionResult(debtPositionDTO, OFFSET_DATE_TIME, true);

    Mockito.when(debtPositionSynchronizeFineActivityMock.handleFineDebtPosition(debtPositionDTO, false, fineWfExecutionConfig))
      .thenReturn(result);

    try (
      MockedStatic<FineWfExecutionConfigMapper> mapperMock = Mockito.mockStatic(FineWfExecutionConfigMapper.class)) {
      mapperMock.when(() -> FineWfExecutionConfigMapper.mapNotifiedInstallment(fineWfExecutionConfig, debtPositionDTO))
        .thenReturn(wfExecutionConfig);

      Mockito.when(invokeSyncDebtPositionActivityMock.synchronizeDPSync(debtPositionDTO, paymentEventRequest, false, wfExecutionConfig))
        .thenReturn(workflowId);

      // When
      wf.synchronizeFineDP(debtPositionDTO, paymentEventRequest, false, fineWfExecutionConfig);

      // Then
      verify(cancelReductionExpirationScheduleActivityMock, times(1)).cancelReductionPeriodExpirationScheduling(debtPositionDTO.getDebtPositionId());
      verify(scheduleReductionExpirationActivityMock, times(1)).scheduleExpireFineReduction(debtPositionDTO.getDebtPositionId(), fineWfExecutionConfig, OFFSET_DATE_TIME);
    }
  }
}
