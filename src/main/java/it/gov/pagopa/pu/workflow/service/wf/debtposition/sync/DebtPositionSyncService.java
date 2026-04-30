package it.gov.pagopa.pu.workflow.service.wf.debtposition.sync;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.wf.debtposition.sync.complete.DebtPositionCompleteChangeSyncService;
import it.gov.pagopa.pu.workflow.service.wf.debtposition.sync.config.WfExecutionConfigHandlerService;
import it.gov.pagopa.pu.workflow.service.wf.debtposition.sync.partial.DebtPositionPartialChangeSyncService;
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

  public WorkflowCreatedDTO invokeWorkflow(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, WfExecutionParameters wfExecutionParameters, String accessToken) {
    wfExecutionConfigHandlerService.persistAndConfigure(debtPositionDTO, wfExecutionParameters);
    if(wfExecutionParameters.isPartialChange()){
      return partialChangeSyncService.invokeWorkflow(debtPositionDTO, paymentEventRequest);
    } else {
      return completeChangeSyncService.invokeWorkflow(debtPositionDTO, paymentEventRequest, wfExecutionParameters, accessToken);
    }
  }
}
