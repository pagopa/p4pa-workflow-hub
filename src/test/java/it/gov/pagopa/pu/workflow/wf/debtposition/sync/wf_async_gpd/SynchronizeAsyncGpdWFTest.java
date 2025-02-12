package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_async_gpd;

import it.gov.pagopa.payhub.activities.activity.debtposition.gpd.SynchronizeInstallmentGpdActivity;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.IupdSyncStatusUpdateDTO;
import it.gov.pagopa.pu.workflow.event.payments.enums.PaymentEventType;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.BaseDPSynchronizeWFTest;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.config.SynchronizeDebtPositionWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class SynchronizeAsyncGpdWFTest extends BaseDPSynchronizeWFTest<SynchronizeAsyncGpdWF> {

  @Mock
  private SynchronizeInstallmentGpdActivity synchronizeInstallmentGpdActivity;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      synchronizeInstallmentGpdActivity);
  }

  @Override
  protected SynchronizeAsyncGpdWF configureMockAndCreateWf(ApplicationContext applicationContextMock) {
    SynchronizeDebtPositionWfConfig wfConfigMock = applicationContextMock.getBean(SynchronizeDebtPositionWfConfig.class);
    Mockito.when(wfConfigMock.buildSynchronizeInstallmentGpdActivity())
      .thenReturn(synchronizeInstallmentGpdActivity);

    SynchronizeAsyncGpdWFImpl wf = new SynchronizeAsyncGpdWFImpl();
    wf.setApplicationContext(applicationContextMock);

    return wf;
  }

  @Override
  protected void configureIUDSyncOk(DebtPositionDTO debtPosition, String iud) {
    Mockito.doReturn("IUPD_" + iud)
      .when(synchronizeInstallmentGpdActivity)
      .synchronizeInstallmentGpd(Mockito.same(debtPosition), Mockito.eq(iud));
  }

  @Override
  protected void configureIUDSyncKo(DebtPositionDTO debtPosition, String iud, Throwable expectedException) {
    Mockito.doThrow(expectedException)
      .when(synchronizeInstallmentGpdActivity)
      .synchronizeInstallmentGpd(
        Mockito.same(debtPosition), Mockito.eq(iud)
      );
  }

  @Override
  protected IupdSyncStatusUpdateDTO buildExpectedIupdSyncStatusUpdateDTO() {
    return super.buildExpectedIupdSyncStatusUpdateDTO()
      .iupdPagopa("IUPD_"+SYNC_IUD);
  }

  @Override
  protected void invokeWF(SynchronizeAsyncGpdWF synchronizeSyncAcaWF, DebtPositionDTO debtPosition, PaymentEventType paymentEventType) {
    synchronizeSyncAcaWF.synchronizeDPAsyncGpd(debtPosition, paymentEventType);
  }

}
