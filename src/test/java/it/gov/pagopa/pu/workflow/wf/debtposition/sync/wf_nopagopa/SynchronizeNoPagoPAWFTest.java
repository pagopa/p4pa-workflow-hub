package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_nopagopa;

import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.SyncStatusUpdateRequestDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.BaseDPSynchronizeWFTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class SynchronizeNoPagoPAWFTest extends BaseDPSynchronizeWFTest<SynchronizeNoPagoPAWF> {

  @Override
  protected SynchronizeNoPagoPAWF configureMockAndCreateWf(ApplicationContext applicationContextMock) {
    SynchronizeNoPagoPAWFImpl wf = new SynchronizeNoPagoPAWFImpl();
    wf.setApplicationContext(applicationContextMock);
    return wf;
  }

  @Override
  protected void configureIUDSyncOk(DebtPositionDTO debtPosition, String iud) {
    // No Sync ops
  }

  @Override
  protected void configureIUDSyncKo(DebtPositionDTO debtPosition, String iud, Throwable expectedException) {
    // No Sync ops
  }

  @Override
  protected boolean isSyncErrorPossible() {
    return false;
  }

  @Override
  protected boolean isNotifyIoInvolved() {
    return false;
  }

  @Override
  protected SyncStatusUpdateRequestDTO invokeWF(SynchronizeNoPagoPAWF wf, DebtPositionDTO debtPosition, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig) {
    return wf.synchronizeDPNoPagoPA(debtPosition, paymentEventRequest, wfExecutionConfig);
  }

}
