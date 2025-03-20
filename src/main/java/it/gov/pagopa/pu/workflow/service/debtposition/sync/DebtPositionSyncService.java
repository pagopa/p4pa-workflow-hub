package it.gov.pagopa.pu.workflow.service.debtposition.sync;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.service.debtposition.sync.complete.DebtPositionCompleteChangeSyncService;
import it.gov.pagopa.pu.workflow.service.debtposition.sync.config.WfExecutionConfigHandlerService;
import it.gov.pagopa.pu.workflow.service.debtposition.sync.partial.DebtPositionPartialChangeSyncService;
import org.springframework.stereotype.Service;

@Service
public class DebtPositionSyncService {

  private final WfExecutionConfigHandlerService wfExecutionConfigHandlerService;
  private final DebtPositionPartialChangeSyncService partialChangeSyncService;
  private final DebtPositionCompleteChangeSyncService completeChangeSyncService;

  public DebtPositionSyncService(WfExecutionConfigHandlerService wfExecutionConfigHandlerService, DebtPositionPartialChangeSyncService partialChangeSyncService, DebtPositionCompleteChangeSyncService completeChangeSyncService) {
    this.wfExecutionConfigHandlerService = wfExecutionConfigHandlerService;
    this.partialChangeSyncService = partialChangeSyncService;
    this.completeChangeSyncService = completeChangeSyncService;
  }

  public String invokeWorkflow(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType, WfExecutionParameters wfExecutionParameters, String accessToken) {
    wfExecutionConfigHandlerService.persistAndConfigure(debtPositionDTO, wfExecutionParameters);
    if(wfExecutionParameters.isPartialChange()){
      return partialChangeSyncService.invokeWorkflow(debtPositionDTO, paymentEventType);
    } else {
      return completeChangeSyncService.invokeWorkflow(debtPositionDTO, paymentEventType, wfExecutionParameters, accessToken);
    }
  }
}
