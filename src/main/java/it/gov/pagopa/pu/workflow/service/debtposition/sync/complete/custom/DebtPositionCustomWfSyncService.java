package it.gov.pagopa.pu.workflow.service.debtposition.sync.complete.custom;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.WfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DebtPositionCustomWfSyncService {
  public String invokeWorkflow(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, WfExecutionParameters wfExecutionParameters) {
    WfExecutionConfig wfExecutionConfig = wfExecutionParameters.getWfExecutionConfig();
    log.debug("Requested complete change on debtPosition {} (paymentEventType {}) related to a custom WF sync (wfConfigClass {})",
      debtPositionDTO.getDebtPositionId(),
      paymentEventRequest!=null? paymentEventRequest.getPaymentEventType() : null,
      wfExecutionConfig.getClass());

    return null;// TODO switch based on wfExecutionConfig type towards client invoke
  }
}
