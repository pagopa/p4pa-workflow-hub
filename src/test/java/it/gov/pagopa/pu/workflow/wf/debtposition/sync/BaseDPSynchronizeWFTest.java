package it.gov.pagopa.pu.workflow.wf.debtposition.sync;

import it.gov.pagopa.payhub.activities.activity.debtposition.synchronize.FinalizeDebtPositionSyncStatusActivity;
import it.gov.pagopa.payhub.activities.activity.debtposition.ionotification.IONotificationDebtPositionActivity;
import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionIoNotificationDTO;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.*;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.utils.faker.InstallmentFaker;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity.CancelCheckDpExpirationScheduleActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity.PublishPaymentEventActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity.ScheduleCheckDpExpirationActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.config.SynchronizeDebtPositionWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionDTO;

@ExtendWith(MockitoExtension.class)
public abstract class BaseDPSynchronizeWFTest<W> {

  protected static final String SYNC_IUD = "SYNCIUD";
  protected static final String SYNC_IUD_ERROR = "SYNCIUDERROR";

  @Mock
  protected FinalizeDebtPositionSyncStatusActivity finalizeDebtPositionSyncStatusActivityMock;
  @Mock
  protected IONotificationDebtPositionActivity ioNotificationDebtPositionActivityMock;
  @Mock
  protected PublishPaymentEventActivity publishPaymentEventActivityMock;
  @Mock
  protected CancelCheckDpExpirationScheduleActivity cancelCheckDpExpirationScheduleActivityMock;
  @Mock
  protected ScheduleCheckDpExpirationActivity scheduleCheckDpExpirationActivityMock;

  protected W wf;

  @BeforeEach
  protected void init() {
    SynchronizeDebtPositionWfConfig wfConfigMock = Mockito.mock(SynchronizeDebtPositionWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(wfConfigMock.buildFinalizeDebtPositionSyncStatusActivityStub())
      .thenReturn(finalizeDebtPositionSyncStatusActivityMock);
    Mockito.when(wfConfigMock.buildIONotificationDebtPositionActivityStub())
      .thenReturn(ioNotificationDebtPositionActivityMock);
    Mockito.when(wfConfigMock.buildPublishPaymentEventActivityStub())
      .thenReturn(publishPaymentEventActivityMock);
    Mockito.when(wfConfigMock.buildCancelCheckDpExpirationScheduleActivityStub())
        .thenReturn(cancelCheckDpExpirationScheduleActivityMock);
    Mockito.when(wfConfigMock.buildScheduleCheckDpExpirationActivityStub())
      .thenReturn(scheduleCheckDpExpirationActivityMock);

    Mockito.when(applicationContextMock.getBean(SynchronizeDebtPositionWfConfig.class))
      .thenReturn(wfConfigMock);

    wf = configureMockAndCreateWf(applicationContextMock);
  }

  protected abstract W configureMockAndCreateWf(ApplicationContext applicationContextMock);

  @AfterEach
  protected final void verifyNoMoreInteractionsBaseClass() {
    Mockito.verifyNoMoreInteractions(
      finalizeDebtPositionSyncStatusActivityMock,
      ioNotificationDebtPositionActivityMock,
      publishPaymentEventActivityMock,
      cancelCheckDpExpirationScheduleActivityMock,
      scheduleCheckDpExpirationActivityMock);
  }

  @Test
  protected void givenCompleteUseCaseWhenWfInvokedThenOk() {
    // Given
    DebtPositionDTO debtPositionRequested = buildDebtPositionToSync();
    PaymentEventRequestDTO paymentEventRequest = new PaymentEventRequestDTO(PaymentEventType.DP_CREATED, "EVENTDESCRIPTION");
    GenericWfExecutionConfig wfExecutionConfig = new GenericWfExecutionConfig();
    wfExecutionConfig.setIoMessages(new GenericWfExecutionConfig.IONotificationBaseOpsMessages());

    LocalDate ancientDueDate = LocalDate.now().minusDays(10);
    DebtPositionDTO debtPositionFinalized = buildFinalizedDebtPosition(ancientDueDate, InstallmentStatus.UNPAID);
    SyncStatusUpdateRequestDTO syncStatusUpdateRequestDTO = new SyncStatusUpdateRequestDTO();

    if(isSyncErrorPossible()){
      syncStatusUpdateRequestDTO.setIupdSyncError(Map.of(SYNC_IUD_ERROR, new SyncErrorDTO("Error occurred while synchronizing Installment with IUD: SYNCIUDERROR for DebtPosition ID: 1. Error: Error")));
    }
    syncStatusUpdateRequestDTO.setIupd2finalize(Map.of(
      SYNC_IUD, buildExpectedIupdSyncStatusUpdateDTO()
    ));

    Mockito.when(finalizeDebtPositionSyncStatusActivityMock
        .finalizeDebtPositionSyncStatus(
          debtPositionRequested.getDebtPositionId(),
          syncStatusUpdateRequestDTO))
      .thenReturn(debtPositionFinalized);


    DebtPositionIoNotificationDTO ioNotificationDTO = new DebtPositionIoNotificationDTO();
    Mockito.lenient()
      .when(ioNotificationDebtPositionActivityMock.sendIoNotification(Mockito.same(debtPositionRequested), Mockito.eq(syncStatusUpdateRequestDTO.getIupd2finalize()), Mockito.same(wfExecutionConfig.getIoMessages())))
        .thenReturn(ioNotificationDTO);


    configureIUDSyncOk(debtPositionRequested, SYNC_IUD);
    configureIUDSyncKo(debtPositionRequested, SYNC_IUD_ERROR, new RuntimeException("Error"));

    // When
    SyncStatusUpdateRequestDTO result = invokeWF(wf, debtPositionRequested, paymentEventRequest, wfExecutionConfig);

    // Then
    if(isNotifyIoInvolved()) {
      Mockito.verify(ioNotificationDebtPositionActivityMock)
        .sendIoNotification(Mockito.same(debtPositionRequested), Mockito.eq(syncStatusUpdateRequestDTO.getIupd2finalize()), Mockito.same(wfExecutionConfig.getIoMessages()));
      Mockito.verify(publishPaymentEventActivityMock)
        .publishDebtPositionIoNotificationEvent(Mockito.same(ioNotificationDTO), Mockito.eq(new PaymentEventRequestDTO(PaymentEventType.IO_NOTIFIED, null)));
    }
    Mockito.verify(publishPaymentEventActivityMock)
      .publishDebtPositionEvent(Mockito.same(debtPositionFinalized), Mockito.same(paymentEventRequest));
    Mockito.verify(cancelCheckDpExpirationScheduleActivityMock)
        .cancelDpExpirationSchedule(Mockito.same(debtPositionFinalized.getDebtPositionId()));
    Mockito.verify(scheduleCheckDpExpirationActivityMock)
      .scheduleNextCheckDpExpiration(Mockito.same(debtPositionFinalized.getDebtPositionId()), Mockito.eq(ancientDueDate.plusDays(1)));

    if(isSyncErrorPossible()) {
      Mockito.verify(publishPaymentEventActivityMock)
        .publishDebtPositionErrorEvent(Mockito.same(debtPositionRequested), Mockito.eq(new PaymentEventRequestDTO(PaymentEventType.SYNC_ERROR, "Error occurred while synchronizing Installment with IUD: SYNCIUDERROR for DebtPosition ID: 1. Error: Error")));
    }

    Assertions.assertEquals(syncStatusUpdateRequestDTO, result);
  }

  @Test
  protected void givenNoOptionalBehaviorWhenWfInvokedThenOk() {
    // Given
    DebtPositionDTO debtPosition = buildDebtPositionToSync();
    debtPosition.getPaymentOptions().forEach(po -> po.getInstallments().forEach(i -> i.setDueDate(null)));

    if(isSyncErrorPossible()) {
      configureIUDSyncKo(debtPosition, SYNC_IUD, new RuntimeException("Error"));
      configureIUDSyncKo(debtPosition, SYNC_IUD_ERROR, new RuntimeException("Error"));

      DebtPositionDTO debtPositionFinalized = buildFinalizedDebtPosition(null, InstallmentStatus.TO_SYNC);

      Mockito.when(finalizeDebtPositionSyncStatusActivityMock
          .finalizeDebtPositionSyncStatus(
            debtPosition.getDebtPositionId(),
            new SyncStatusUpdateRequestDTO(
              Map.of(),
              Map.of(
                SYNC_IUD, new SyncErrorDTO("Error occurred while synchronizing Installment with IUD: SYNCIUD for DebtPosition ID: 1. Error: Error"),
                SYNC_IUD_ERROR, new SyncErrorDTO("Error occurred while synchronizing Installment with IUD: SYNCIUDERROR for DebtPosition ID: 1. Error: Error")
              )
            )))
        .thenReturn(debtPositionFinalized);
    } else {
      debtPosition.getPaymentOptions().forEach(po -> po.getInstallments().forEach(i -> i.setStatus(InstallmentStatus.UNPAID)));
    }

    // When, Then
    SyncStatusUpdateRequestDTO result = invokeWF(wf, debtPosition, null, null);

    if(isSyncErrorPossible()){
      Mockito.verify(publishPaymentEventActivityMock)
        .publishDebtPositionErrorEvent(Mockito.same(debtPosition), Mockito.eq(new PaymentEventRequestDTO(PaymentEventType.SYNC_ERROR, "Error occurred while synchronizing Installment with IUD: "+SYNC_IUD+" for DebtPosition ID: 1. Error: Error")));
      Mockito.verify(publishPaymentEventActivityMock)
        .publishDebtPositionErrorEvent(Mockito.same(debtPosition), Mockito.eq(new PaymentEventRequestDTO(PaymentEventType.SYNC_ERROR, "Error occurred while synchronizing Installment with IUD: "+SYNC_IUD_ERROR+" for DebtPosition ID: 1. Error: Error")));
    }

    Mockito.verify(cancelCheckDpExpirationScheduleActivityMock)
      .cancelDpExpirationSchedule(Mockito.same(debtPosition.getDebtPositionId()));

    SyncStatusUpdateRequestDTO expectedResult = new SyncStatusUpdateRequestDTO();
    if(isSyncErrorPossible()){
      expectedResult.setIupdSyncError(Map.of(
        SYNC_IUD, new SyncErrorDTO("Error occurred while synchronizing Installment with IUD: SYNCIUD for DebtPosition ID: 1. Error: Error"),
        SYNC_IUD_ERROR, new SyncErrorDTO("Error occurred while synchronizing Installment with IUD: SYNCIUDERROR for DebtPosition ID: 1. Error: Error")
      ));
    }
    Assertions.assertEquals(expectedResult, result);
  }

  protected DebtPositionDTO buildDebtPositionToSync() {
    DebtPositionDTO debtPosition = buildDebtPositionDTO();

    InstallmentDTO firstInstallment = InstallmentFaker.buildInstallmentDTO()
      .iud(SYNC_IUD)
      .status(InstallmentStatus.TO_SYNC)
      .syncStatus(new InstallmentSyncStatus(InstallmentStatus.DRAFT, InstallmentStatus.UNPAID, null));

    InstallmentDTO secondInstallment = InstallmentFaker.buildInstallmentDTO()
      .iud("IUD_NOT_TO_SYNC")
      .dueDate(LocalDate.MIN)
      .status(InstallmentStatus.UNPAID);

    InstallmentDTO thirdInstallment = InstallmentFaker.buildInstallmentDTO()
      .iud(SYNC_IUD_ERROR)
      .status(isSyncErrorPossible()? InstallmentStatus.TO_SYNC : InstallmentStatus.PAID)
      .syncStatus(new InstallmentSyncStatus(InstallmentStatus.DRAFT, InstallmentStatus.UNPAID, null));

    debtPosition.getPaymentOptions().getFirst().setInstallments(List.of(firstInstallment, secondInstallment, thirdInstallment));
    return debtPosition;
  }

  protected DebtPositionDTO buildFinalizedDebtPosition(LocalDate ancientDueDate, InstallmentStatus installmentStatus) {
    DebtPositionDTO debtPositionFinalized = buildDebtPositionDTO();
    InstallmentDTO firstFinalizedInstallment = debtPositionFinalized.getPaymentOptions().getFirst().getInstallments().getFirst();
    firstFinalizedInstallment.setStatus(installmentStatus);
    firstFinalizedInstallment.setDueDate(ancientDueDate);
    firstFinalizedInstallment.setSwitchToExpired(Boolean.TRUE);
    return debtPositionFinalized;
  }

  protected SyncCompleteDTO buildExpectedIupdSyncStatusUpdateDTO() {
    return new SyncCompleteDTO(InstallmentStatus.UNPAID);
  }

  protected abstract void configureIUDSyncOk(DebtPositionDTO debtPosition, String iud);
  protected abstract void configureIUDSyncKo(DebtPositionDTO debtPosition, String iud, Throwable expectedException);

  protected abstract SyncStatusUpdateRequestDTO invokeWF(W wf, DebtPositionDTO debtPosition, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig);

  protected boolean isSyncErrorPossible(){
    return true;
  }

  protected boolean isNotifyIoInvolved(){
    return true;
  }
}
