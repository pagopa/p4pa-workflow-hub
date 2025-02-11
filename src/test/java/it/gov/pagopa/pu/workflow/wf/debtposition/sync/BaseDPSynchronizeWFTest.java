package it.gov.pagopa.pu.workflow.wf.debtposition.sync;

import it.gov.pagopa.payhub.activities.activity.debtposition.FinalizeDebtPositionSyncStatusActivity;
import it.gov.pagopa.payhub.activities.activity.debtposition.ionotification.SendDebtPositionIONotificationActivity;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtposition.dto.generated.IupdSyncStatusUpdateDTO;
import it.gov.pagopa.pu.workflow.event.payments.enums.PaymentEventType;
import it.gov.pagopa.pu.workflow.utils.faker.InstallmentFaker;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.activity.ScheduleCheckDpExpirationActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.config.CheckDebtPositionExpirationWfConfig;
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

import java.time.OffsetDateTime;
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
  protected SendDebtPositionIONotificationActivity sendDebtPositionIONotificationActivityMock;
  @Mock
  protected PublishPaymentEventActivity publishPaymentEventActivityMock;
  @Mock
  protected ScheduleCheckDpExpirationActivity scheduleCheckDpExpirationActivityMock;

  protected W wf;

  @BeforeEach
  protected void init() {
    SynchronizeDebtPositionWfConfig wfConfigMock = Mockito.mock(SynchronizeDebtPositionWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(wfConfigMock.buildFinalizeDebtPositionSyncStatusActivityStub())
      .thenReturn(finalizeDebtPositionSyncStatusActivityMock);
    Mockito.when(wfConfigMock.buildSendDebtPositionIONotificationActivityStub())
      .thenReturn(sendDebtPositionIONotificationActivityMock);
    Mockito.when(wfConfigMock.buildPublishPaymentEventActivityStub())
      .thenReturn(publishPaymentEventActivityMock);

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
      sendDebtPositionIONotificationActivityMock,
      publishPaymentEventActivityMock,
      scheduleCheckDpExpirationActivityMock);
  }

  @Test
  protected void givenPaymentEventTypeAndExpirationInstallmentWhenWfInvokedThenOk() {
    // Given
    DebtPositionDTO debtPosition = buildDebtPositionToSync();
    PaymentEventType paymentEventType = PaymentEventType.DP_CREATED;

    OffsetDateTime ancientDueDate = OffsetDateTime.now().minusDays(10);
    DebtPositionDTO debtPositionFinalized = buildFinalizedDebtPosition(ancientDueDate, InstallmentDTO.StatusEnum.UNPAID);
    Mockito.when(finalizeDebtPositionSyncStatusActivityMock
        .finalizeDebtPositionSyncStatus(
          debtPosition.getDebtPositionId(),
          Map.of(
            SYNC_IUD, buildExpectedIupdSyncStatusUpdateDTO()
          )))
      .thenReturn(debtPositionFinalized);

    configureSync(debtPosition, new RuntimeException("Error"));

    // When
    invokeWF(wf, debtPosition, paymentEventType);

    // Then
    Mockito.verify(sendDebtPositionIONotificationActivityMock)
      .sendMessage(Mockito.same(debtPositionFinalized));
    Mockito.verify(publishPaymentEventActivityMock)
      .publish(Mockito.same(debtPositionFinalized), Mockito.same(paymentEventType), Mockito.isNull());
    Mockito.verify(scheduleCheckDpExpirationActivityMock)
      .scheduleNextCheckDpExpiration(Mockito.same(debtPositionFinalized.getDebtPositionId()), Mockito.eq(ancientDueDate.plusDays(1)));

    if(isSyncErrorPossible()) {
      Mockito.verify(publishPaymentEventActivityMock)
        .publish(debtPosition, PaymentEventType.SYNC_ERROR, "Error");
    }
  }

  @Test
  protected void givenNoPaymentEventTypeAndNoExpirationInstallmentWhenWfInvokedThenOk() {
    // Given
    DebtPositionDTO debtPosition = buildDebtPositionToSync();

    OffsetDateTime ancientDueDate = OffsetDateTime.now().minusDays(10);
    DebtPositionDTO debtPositionFinalized = buildFinalizedDebtPosition(ancientDueDate, InstallmentDTO.StatusEnum.PAID);
    Mockito.when(finalizeDebtPositionSyncStatusActivityMock
        .finalizeDebtPositionSyncStatus(
          debtPosition.getDebtPositionId(),
          Map.of(
            SYNC_IUD, buildExpectedIupdSyncStatusUpdateDTO()
          )))
      .thenReturn(debtPositionFinalized);

    configureSync(debtPosition, new RuntimeException("Error"));

    // When
    invokeWF(wf, debtPosition, null);

    // Then
    Mockito.verify(sendDebtPositionIONotificationActivityMock)
      .sendMessage(Mockito.same(debtPositionFinalized));
    if(isSyncErrorPossible()) {
      Mockito.verify(publishPaymentEventActivityMock)
        .publish(debtPosition, PaymentEventType.SYNC_ERROR, "Error");
    }
  }

  protected DebtPositionDTO buildDebtPositionToSync() {
    DebtPositionDTO debtPosition = buildDebtPositionDTO();

    InstallmentDTO firstInstallment = InstallmentFaker.buildInstallmentDTO()
      .iud(SYNC_IUD)
      .status(InstallmentDTO.StatusEnum.TO_SYNC)
      .syncStatus(new InstallmentSyncStatus(InstallmentSyncStatus.SyncStatusFromEnum.DRAFT, InstallmentSyncStatus.SyncStatusToEnum.UNPAID));

    InstallmentDTO secondInstallment = InstallmentFaker.buildInstallmentDTO()
      .iud("IUD_NOT_TO_SYNC")
      .status(InstallmentDTO.StatusEnum.UNPAID);

    InstallmentDTO thirdInstallment = InstallmentFaker.buildInstallmentDTO()
      .iud(SYNC_IUD_ERROR)
      .status(isSyncErrorPossible()? InstallmentDTO.StatusEnum.TO_SYNC : InstallmentDTO.StatusEnum.PAID)
      .syncStatus(new InstallmentSyncStatus(InstallmentSyncStatus.SyncStatusFromEnum.DRAFT, InstallmentSyncStatus.SyncStatusToEnum.UNPAID));

    debtPosition.getPaymentOptions().getFirst().setInstallments(List.of(firstInstallment, secondInstallment, thirdInstallment));
    return debtPosition;
  }

  protected DebtPositionDTO buildFinalizedDebtPosition(OffsetDateTime ancientDueDate, InstallmentDTO.StatusEnum installmentStatus) {
    DebtPositionDTO debtPositionFinalized = buildDebtPositionDTO();
    InstallmentDTO firstFinalizedInstallment = debtPositionFinalized.getPaymentOptions().getFirst().getInstallments().getFirst();
    firstFinalizedInstallment.setStatus(installmentStatus);
    firstFinalizedInstallment.setDueDate(ancientDueDate);
    return debtPositionFinalized;
  }

  protected IupdSyncStatusUpdateDTO buildExpectedIupdSyncStatusUpdateDTO() {
    return new IupdSyncStatusUpdateDTO(IupdSyncStatusUpdateDTO.NewStatusEnum.UNPAID, null);
  }

  protected abstract void configureSync(DebtPositionDTO debtPosition, Throwable expectedException);

  protected abstract void invokeWF(W wf, DebtPositionDTO debtPosition, PaymentEventType paymentEventType);

  protected boolean isSyncErrorPossible(){
    return true;
  }
}
