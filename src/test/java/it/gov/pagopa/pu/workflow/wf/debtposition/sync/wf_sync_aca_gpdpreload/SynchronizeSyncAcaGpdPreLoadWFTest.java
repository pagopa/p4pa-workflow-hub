package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync_aca_gpdpreload;

import it.gov.pagopa.payhub.activities.activity.debtposition.aca.SynchronizeInstallmentAcaActivity;
import it.gov.pagopa.payhub.activities.activity.debtposition.gpdpreload.SynchronizeInstallmentGpdPreLoadActivity;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.BaseDPSynchronizeWFTest;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.config.SynchronizeDebtPositionWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class SynchronizeSyncAcaGpdPreLoadWFTest extends BaseDPSynchronizeWFTest<SynchronizeSyncAcaGpdPreLoadWF> {

  @Mock
  private SynchronizeInstallmentAcaActivity synchronizeInstallmentAcaActivityMock;
  @Mock
  private SynchronizeInstallmentGpdPreLoadActivity synchronizeInstallmentGpdPreLoadActivity;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      synchronizeInstallmentAcaActivityMock,
      synchronizeInstallmentGpdPreLoadActivity);
  }

  @Override
  protected SynchronizeSyncAcaGpdPreLoadWF configureMockAndCreateWf(ApplicationContext applicationContextMock) {
    SynchronizeDebtPositionWfConfig wfConfigMock = applicationContextMock.getBean(SynchronizeDebtPositionWfConfig.class);
    Mockito.when(wfConfigMock.buildSynchronizeInstallmentAcaActivity())
      .thenReturn(synchronizeInstallmentAcaActivityMock);
    Mockito.when(wfConfigMock.buildSynchronizeInstallmentGpdPreLoadActivity())
      .thenReturn(synchronizeInstallmentGpdPreLoadActivity);

    SynchronizeSyncAcaGpdPreLoadWFImpl wf = new SynchronizeSyncAcaGpdPreLoadWFImpl();
    wf.setApplicationContext(applicationContextMock);

    return wf;
  }

  @Override
  protected void configureIUDSyncOk(DebtPositionDTO debtPosition, String iud) {
    configureIUDSyncOk_ACA(debtPosition, iud);

    configureIUDSyncOk_GpdPreLoad(debtPosition, iud);
  }

  private void configureIUDSyncOk_ACA(DebtPositionDTO debtPosition, String iud) {
    Mockito.doNothing()
      .when(synchronizeInstallmentAcaActivityMock)
      .synchronizeInstallmentAca(Mockito.same(debtPosition), Mockito.eq(iud));
  }

  private void configureIUDSyncOk_GpdPreLoad(DebtPositionDTO debtPosition, String iud) {
    Mockito.doNothing()
      .when(synchronizeInstallmentGpdPreLoadActivity)
      .synchronizeInstallmentGpdPreLoad(Mockito.same(debtPosition), Mockito.eq(iud));
  }

  private enum FAIL_USE_CASE {
    BOTH,
    ACA,
    GPD_PRELOAD
  }

  private FAIL_USE_CASE failUseCase = FAIL_USE_CASE.BOTH;

  @Override
  protected void configureIUDSyncKo(DebtPositionDTO debtPosition, String iud, Throwable expectedException) {
    if(FAIL_USE_CASE.BOTH.equals(failUseCase) || FAIL_USE_CASE.ACA.equals(failUseCase)) {
      Mockito.doThrow(expectedException)
        .when(synchronizeInstallmentAcaActivityMock)
        .synchronizeInstallmentAca(
          Mockito.same(debtPosition), Mockito.eq(iud)
        );
    } else {
      configureIUDSyncOk_ACA(debtPosition, iud);
    }

    if(FAIL_USE_CASE.BOTH.equals(failUseCase) || FAIL_USE_CASE.GPD_PRELOAD.equals(failUseCase)) {
      Mockito.doThrow(expectedException)
        .when(synchronizeInstallmentGpdPreLoadActivity)
        .synchronizeInstallmentGpdPreLoad(
          Mockito.same(debtPosition), Mockito.eq(iud)
        );
    } else {
      configureIUDSyncOk_GpdPreLoad(debtPosition, iud);
    }
  }

  @Test
  void givenBothErrorsWhenSuperGivenCompleteUseCaseWhenWfInvokedThenOk(){
    failUseCase = FAIL_USE_CASE.BOTH;
    Assertions.assertDoesNotThrow(super::givenCompleteUseCaseWhenWfInvokedThenOk);
  }

  @Test
  void givenACAErrorsWhenSuperGivenCompleteUseCaseWhenWfInvokedThenOk(){
    failUseCase = FAIL_USE_CASE.ACA;
    Assertions.assertDoesNotThrow(super::givenCompleteUseCaseWhenWfInvokedThenOk);
  }

  @Test
  void givenGpdPreLoadErrorsWhenSuperGivenCompleteUseCaseWhenWfInvokedThenOk(){
    failUseCase = FAIL_USE_CASE.GPD_PRELOAD;
    Assertions.assertDoesNotThrow(super::givenCompleteUseCaseWhenWfInvokedThenOk);
  }

  @Override
  protected void givenCompleteUseCaseWhenWfInvokedThenOk() {
    super.givenCompleteUseCaseWhenWfInvokedThenOk();
  }

  @Override
  protected void invokeWF(SynchronizeSyncAcaGpdPreLoadWF synchronizeSyncAcaWF, DebtPositionDTO debtPosition, PaymentEventType paymentEventType) {
    synchronizeSyncAcaWF.synchronizeDPSyncAcaGpdPreLoad(debtPosition, paymentEventType);
  }

}
