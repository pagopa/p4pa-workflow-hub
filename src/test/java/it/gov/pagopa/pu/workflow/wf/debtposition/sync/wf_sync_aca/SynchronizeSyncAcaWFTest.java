package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync_aca;

import it.gov.pagopa.payhub.activities.activity.debtposition.aca.SynchronizeInstallmentAcaActivity;
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
class SynchronizeSyncAcaWFTest extends BaseDPSynchronizeWFTest<SynchronizeSyncAcaWF> {

  @Mock
  private SynchronizeInstallmentAcaActivity synchronizeInstallmentAcaActivityMock;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      synchronizeInstallmentAcaActivityMock);
  }

  @Override
  protected SynchronizeSyncAcaWF configureMockAndCreateWf(ApplicationContext applicationContextMock) {
    SynchronizeDebtPositionWfConfig wfConfigMock = applicationContextMock.getBean(SynchronizeDebtPositionWfConfig.class);
    Mockito.when(wfConfigMock.buildSynchronizeInstallmentAcaActivity())
      .thenReturn(synchronizeInstallmentAcaActivityMock);

    SynchronizeSyncAcaWFImpl wf = new SynchronizeSyncAcaWFImpl();
    wf.setApplicationContext(applicationContextMock);

    return wf;
  }

  @Override
  protected void configureIUDSyncOk(DebtPositionDTO debtPosition, String iud) {
    Mockito.doNothing()
      .when(synchronizeInstallmentAcaActivityMock)
      .synchronizeInstallmentAca(Mockito.same(debtPosition), Mockito.eq(iud));
  }

  @Override
  protected void configureIUDSyncKo(DebtPositionDTO debtPosition, String iud, Throwable expectedException) {
    Mockito.doThrow(expectedException)
      .when(synchronizeInstallmentAcaActivityMock)
      .synchronizeInstallmentAca(
        Mockito.same(debtPosition), Mockito.eq(iud)
      );
  }

  @Override
  protected void invokeWF(SynchronizeSyncAcaWF synchronizeSyncAcaWF, DebtPositionDTO debtPosition, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig) {
    synchronizeSyncAcaWF.synchronizeDPSyncAca(debtPosition, paymentEventRequest, wfExecutionConfig);
  }

}
