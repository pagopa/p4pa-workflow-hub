package it.gov.pagopa.pu.workflow.service.debtposition.sync;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import org.springframework.stereotype.Service;

@Service
public class DebtPositionSyncService {
  private final WfExecutionConfigHandlerService wfExecutionConfigHandlerService;

  public DebtPositionSyncService(WfExecutionConfigHandlerService wfExecutionConfigHandlerService) {
    this.wfExecutionConfigHandlerService = wfExecutionConfigHandlerService;
  }

  public String invokeWorkflow(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType, WfExecutionParameters wfExecutionParameters, String accessToken) {
    wfExecutionConfigHandlerService.persistAndConfigure(debtPositionDTO, wfExecutionParameters);
    return null; // TODO P4ADEV-2448
  }
}
