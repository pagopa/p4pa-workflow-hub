package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync_gpdpreload;

import it.gov.pagopa.payhub.activities.activity.debtposition.gpdpreload.SynchronizeInstallmentGpdPreLoadActivity;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.BaseDPSynchronizeWFTest;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.config.SynchronizeDebtPositionWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class SynchronizeSyncGpdPreLoadWFTest extends BaseDPSynchronizeWFTest<SynchronizeSyncGpdPreLoadWF> {

  @Mock
  private SynchronizeInstallmentGpdPreLoadActivity synchronizeInstallmentGpdPreLoadActivity;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      synchronizeInstallmentGpdPreLoadActivity);
  }

  @Override
  protected SynchronizeSyncGpdPreLoadWF configureMockAndCreateWf(ApplicationContext applicationContextMock) {
    SynchronizeDebtPositionWfConfig wfConfigMock = applicationContextMock.getBean(SynchronizeDebtPositionWfConfig.class);
    Mockito.when(wfConfigMock.buildSynchronizeInstallmentGpdPreLoadActivity())
      .thenReturn(synchronizeInstallmentGpdPreLoadActivity);

    SynchronizeSyncGpdPreLoadWFImpl wf = new SynchronizeSyncGpdPreLoadWFImpl();
    wf.setApplicationContext(applicationContextMock);

    return wf;
  }

  @Override
  protected void configureIUDSyncOk(DebtPositionDTO debtPosition, String iud) {
    Mockito.doNothing()
      .when(synchronizeInstallmentGpdPreLoadActivity)
      .synchronizeInstallmentGpdPreLoad(Mockito.same(debtPosition), Mockito.eq(iud));
  }

  @Override
  protected void configureIUDSyncKo(DebtPositionDTO debtPosition, String iud, Throwable expectedException) {
    Mockito.doThrow(expectedException)
      .when(synchronizeInstallmentGpdPreLoadActivity)
      .synchronizeInstallmentGpdPreLoad(
        Mockito.same(debtPosition), Mockito.eq(iud)
      );
  }

  @Override
  protected void invokeWF(SynchronizeSyncGpdPreLoadWF synchronizeSyncAcaWF, DebtPositionDTO debtPosition, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig) {
    synchronizeSyncAcaWF.synchronizeDPSyncGpdPreLoad(debtPosition, paymentEventRequest, wfExecutionConfig);
  }

}
