package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_finalize_massive.wf_sync;

import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.BaseDPSynchronizeWFTest;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_finalize_massive.FinalizeMassiveSyncWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_finalize_massive.FinalizeMassiveSyncWFImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class FinalizeMassiveSyncWFTest extends BaseDPSynchronizeWFTest<FinalizeMassiveSyncWF> {

  @Override
  protected FinalizeMassiveSyncWF configureMockAndCreateWf(ApplicationContext applicationContextMock) {
    FinalizeMassiveSyncWFImpl wf = new FinalizeMassiveSyncWFImpl();
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
  protected void invokeWF(FinalizeMassiveSyncWF wf, DebtPositionDTO debtPosition, PaymentEventType paymentEventType, GenericWfExecutionConfig wfExecutionConfig) {
    wf.finalizeMassiveSync(debtPosition, paymentEventType, wfExecutionConfig);
  }

}
