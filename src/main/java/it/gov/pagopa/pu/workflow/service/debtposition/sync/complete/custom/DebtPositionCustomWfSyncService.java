package it.gov.pagopa.pu.workflow.service.debtposition.sync.complete.custom;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.WfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.DebtPositionFineClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DebtPositionCustomWfSyncService {

  private final DebtPositionFineClient fineClient;

  public DebtPositionCustomWfSyncService(DebtPositionFineClient fineClient) {
    this.fineClient = fineClient;
  }

  public String invokeWorkflow(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, WfExecutionParameters wfExecutionParameters) {
    WfExecutionConfig wfExecutionConfig = wfExecutionParameters.getWfExecutionConfig();
    log.debug("Requested complete change on debtPosition {} (paymentEventType {}) related to a custom WF sync (wfConfigClass {})",
      debtPositionDTO.getDebtPositionId(),
      paymentEventRequest!=null? paymentEventRequest.getPaymentEventType() : null,
      wfExecutionConfig.getClass());

    if (wfExecutionConfig instanceof FineWfExecutionConfig fineWfExecutionConfig) {
      return fineClient.synchronizeFineDP(debtPositionDTO, paymentEventRequest, wfExecutionParameters.isMassive(), fineWfExecutionConfig);
    } else {
      throw new IllegalStateException("WfExecutionConfig not supported: " + wfExecutionConfig.getClass());
    }
  }
}
