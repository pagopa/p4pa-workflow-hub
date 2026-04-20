package it.gov.pagopa.pu.workflow.service.wf.debtposition.sync.complete.custom;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.WfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.exception.custom.IllegalStateBusinessException;
import it.gov.pagopa.pu.workflow.utilities.ErrorCodeConstants;
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

  public WorkflowCreatedDTO invokeWorkflow(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, WfExecutionParameters wfExecutionParameters) {
    WfExecutionConfig wfExecutionConfig = wfExecutionParameters.getWfExecutionConfig();
    log.debug("Requested complete change on debtPosition {} (paymentEventType {}) related to a custom WF sync (wfConfigClass {})",
      debtPositionDTO.getDebtPositionId(),
      paymentEventRequest!=null? paymentEventRequest.getPaymentEventType() : null,
      wfExecutionConfig.getClass());

    if (wfExecutionConfig instanceof FineWfExecutionConfig fineWfExecutionConfig) {
      return fineClient.synchronizeFineDP(debtPositionDTO, paymentEventRequest, wfExecutionParameters.isMassive(), fineWfExecutionConfig);
    } else {
      throw new IllegalStateBusinessException(ErrorCodeConstants.ERROR_CODE_INVALID_CONFIG, "WfExecutionConfig not supported: " + wfExecutionConfig.getClass());
    }
  }
}
