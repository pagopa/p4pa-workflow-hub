package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.BaseDPSynchronizeWFTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class SynchronizeNoPagoPAWFTest extends BaseDPSynchronizeWFTest<SynchronizeSyncWF> {

  @Override
  protected SynchronizeSyncWF configureMockAndCreateWf(ApplicationContext applicationContextMock) {
    SynchronizeSyncWFImpl wf = new SynchronizeSyncWFImpl();
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
  protected void invokeWF(SynchronizeSyncWF wf, DebtPositionDTO debtPosition, PaymentEventType paymentEventType) {
    wf.synchronizeDPSync(debtPosition, paymentEventType);
  }

}
