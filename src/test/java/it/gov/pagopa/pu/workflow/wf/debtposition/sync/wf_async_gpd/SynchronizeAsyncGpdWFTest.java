package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_async_gpd;

import it.gov.pagopa.payhub.activities.activity.debtposition.synchronize.gpd.SynchronizeInstallmentGpdActivity;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.SyncStatusUpdateRequestDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.BaseDPSynchronizeWFTest;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.config.SynchronizeDebtPositionWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static org.mockito.Mockito.*;

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
    when(wfConfigMock.buildSynchronizeInstallmentGpdActivity())
      .thenReturn(synchronizeInstallmentGpdActivity);

    SynchronizeAsyncGpdWFImpl wf = new SynchronizeAsyncGpdWFImpl();
    wf.setApplicationContext(applicationContextMock);

    return wf;
  }

  @Override
  protected void configureIUDSyncOk(DebtPositionDTO debtPosition, String iud) {
    doReturn("IUPD_" + iud)
      .when(synchronizeInstallmentGpdActivity)
      .synchronizeInstallmentGpd(Mockito.same(debtPosition), Mockito.eq(iud));
  }

  @Override
  protected void configureIUDSyncKo(DebtPositionDTO debtPosition, String iud, Throwable expectedException) {
    doThrow(expectedException)
      .when(synchronizeInstallmentGpdActivity)
      .synchronizeInstallmentGpd(
        Mockito.same(debtPosition), Mockito.eq(iud)
      );
  }

  @Override
  protected SyncStatusUpdateRequestDTO invokeWF(SynchronizeAsyncGpdWF synchronizeSyncAcaWF, DebtPositionDTO debtPosition, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig) {
    return synchronizeSyncAcaWF.synchronizeDPAsyncGpd(debtPosition, paymentEventRequest, wfExecutionConfig);
  }

}
