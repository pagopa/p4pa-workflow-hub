package it.gov.pagopa.pu.workflow.wf.debtposition.sync;

import it.gov.pagopa.payhub.activities.activity.debtposition.FinalizeDebtPositionSyncStatusActivity;
import it.gov.pagopa.payhub.activities.activity.debtposition.ionotification.IONotificationDebtPositionActivity;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtposition.dto.generated.IupdSyncStatusUpdateDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.utils.faker.InstallmentFaker;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.activity.ScheduleCheckDpExpirationActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.config.CheckDebtPositionExpirationWfConfig;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity.CancelCheckDpExpirationScheduleActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity.PublishPaymentEventActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.config.SynchronizeDebtPositionWfConfig;
import org.junit.jupiter.api.AfterEach;
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

    Mockito.when(applicationContextMock.getBean(SynchronizeDebtPositionWfConfig.class))
      .thenReturn(wfConfigMock);

    CheckDebtPositionExpirationWfConfig checkDebtPositionExpirationWfConfigMock = Mockito.mock(CheckDebtPositionExpirationWfConfig.class);

    Mockito.when(checkDebtPositionExpirationWfConfigMock.buildScheduleCheckDpExpirationActivityStub())
      .thenReturn(scheduleCheckDpExpirationActivityMock);

    Mockito.when(applicationContextMock.getBean(CheckDebtPositionExpirationWfConfig.class))
      .thenReturn(checkDebtPositionExpirationWfConfigMock);

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
    PaymentEventType paymentEventType = PaymentEventType.DP_CREATED;

    LocalDate ancientDueDate = LocalDate.now().minusDays(10);
    DebtPositionDTO debtPositionFinalized = buildFinalizedDebtPosition(ancientDueDate, InstallmentDTO.StatusEnum.UNPAID);
    Map<String, IupdSyncStatusUpdateDTO> iudSyncFinalizationMap = Map.of(
      SYNC_IUD, buildExpectedIupdSyncStatusUpdateDTO()
    );
    Mockito.when(finalizeDebtPositionSyncStatusActivityMock
        .finalizeDebtPositionSyncStatus(
          debtPositionRequested.getDebtPositionId(),
          iudSyncFinalizationMap))
      .thenReturn(debtPositionFinalized);

    configureIUDSyncOk(debtPositionRequested, SYNC_IUD);
    configureIUDSyncKo(debtPositionRequested, SYNC_IUD_ERROR, new RuntimeException("Error"));

    // When
    invokeWF(wf, debtPositionRequested, paymentEventType);

    // Then
    if(isNotifyIoInvolved()) {
      Mockito.verify(ioNotificationDebtPositionActivityMock)
        .sendIoNotification(Mockito.same(debtPositionRequested), Mockito.eq(iudSyncFinalizationMap));
    }
    Mockito.verify(publishPaymentEventActivityMock)
      .publishErrorEvent(Mockito.same(debtPositionFinalized), Mockito.same(paymentEventType), Mockito.isNull());
    Mockito.verify(cancelCheckDpExpirationScheduleActivityMock)
        .cancelExpirationSchedule(Mockito.same(debtPositionFinalized.getDebtPositionId()));
    Mockito.verify(scheduleCheckDpExpirationActivityMock)
      .scheduleNextCheckDpExpiration(Mockito.same(debtPositionFinalized.getDebtPositionId()), Mockito.eq(ancientDueDate.plusDays(1)));

    if(isSyncErrorPossible()) {
      Mockito.verify(publishPaymentEventActivityMock)
        .publishErrorEvent(Mockito.same(debtPositionRequested), Mockito.eq(PaymentEventType.SYNC_ERROR), Mockito.eq("Error occurred while synchronizing Installment with IUD: SYNCIUDERROR for DebtPosition ID: 1. Error: Error"));
    }
  }

  @Test
  protected void givenNoOptionalBehaviorWhenWfInvokedThenOk() {
    // Given
    DebtPositionDTO debtPosition = buildDebtPositionToSync();
    debtPosition.getPaymentOptions().forEach(po -> po.getInstallments().forEach(i -> i.setDueDate(null)));

    if(isSyncErrorPossible()) {
      configureIUDSyncKo(debtPosition, SYNC_IUD, new RuntimeException("Error"));
      configureIUDSyncKo(debtPosition, SYNC_IUD_ERROR, new RuntimeException("Error"));
    } else {
      debtPosition.getPaymentOptions().forEach(po -> po.getInstallments().forEach(i -> i.setStatus(InstallmentDTO.StatusEnum.UNPAID)));
    }

    // When, Then
    invokeWF(wf, debtPosition, null);

    if(isSyncErrorPossible()){
      Mockito.verify(publishPaymentEventActivityMock)
        .publishErrorEvent(Mockito.same(debtPosition), Mockito.eq(PaymentEventType.SYNC_ERROR), Mockito.eq("Error occurred while synchronizing Installment with IUD: "+SYNC_IUD+" for DebtPosition ID: 1. Error: Error"));
      Mockito.verify(publishPaymentEventActivityMock)
        .publishErrorEvent(Mockito.same(debtPosition), Mockito.eq(PaymentEventType.SYNC_ERROR), Mockito.eq("Error occurred while synchronizing Installment with IUD: "+SYNC_IUD_ERROR+" for DebtPosition ID: 1. Error: Error"));
    }

    Mockito.verify(cancelCheckDpExpirationScheduleActivityMock)
      .cancelExpirationSchedule(Mockito.same(debtPosition.getDebtPositionId()));
  }

  protected DebtPositionDTO buildDebtPositionToSync() {
    DebtPositionDTO debtPosition = buildDebtPositionDTO();

    InstallmentDTO firstInstallment = InstallmentFaker.buildInstallmentDTO()
      .iud(SYNC_IUD)
      .status(InstallmentDTO.StatusEnum.TO_SYNC)
      .syncStatus(new InstallmentSyncStatus(InstallmentSyncStatus.SyncStatusFromEnum.DRAFT, InstallmentSyncStatus.SyncStatusToEnum.UNPAID));

    InstallmentDTO secondInstallment = InstallmentFaker.buildInstallmentDTO()
      .iud("IUD_NOT_TO_SYNC")
      .dueDate(LocalDate.MIN)
      .status(InstallmentDTO.StatusEnum.UNPAID);

    InstallmentDTO thirdInstallment = InstallmentFaker.buildInstallmentDTO()
      .iud(SYNC_IUD_ERROR)
      .status(isSyncErrorPossible()? InstallmentDTO.StatusEnum.TO_SYNC : InstallmentDTO.StatusEnum.PAID)
      .syncStatus(new InstallmentSyncStatus(InstallmentSyncStatus.SyncStatusFromEnum.DRAFT, InstallmentSyncStatus.SyncStatusToEnum.UNPAID));

    debtPosition.getPaymentOptions().getFirst().setInstallments(List.of(firstInstallment, secondInstallment, thirdInstallment));
    return debtPosition;
  }

  protected DebtPositionDTO buildFinalizedDebtPosition(LocalDate ancientDueDate, InstallmentDTO.StatusEnum installmentStatus) {
    DebtPositionDTO debtPositionFinalized = buildDebtPositionDTO();
    InstallmentDTO firstFinalizedInstallment = debtPositionFinalized.getPaymentOptions().getFirst().getInstallments().getFirst();
    firstFinalizedInstallment.setStatus(installmentStatus);
    firstFinalizedInstallment.setDueDate(ancientDueDate);
    return debtPositionFinalized;
  }

  protected IupdSyncStatusUpdateDTO buildExpectedIupdSyncStatusUpdateDTO() {
    return new IupdSyncStatusUpdateDTO(IupdSyncStatusUpdateDTO.NewStatusEnum.UNPAID, null);
  }

  protected abstract void configureIUDSyncOk(DebtPositionDTO debtPosition, String iud);
  protected abstract void configureIUDSyncKo(DebtPositionDTO debtPosition, String iud, Throwable expectedException);

  protected abstract void invokeWF(W wf, DebtPositionDTO debtPosition, PaymentEventType paymentEventType);

  protected boolean isSyncErrorPossible(){
    return true;
  }

  protected boolean isNotifyIoInvolved(){
    return true;
  }
}
