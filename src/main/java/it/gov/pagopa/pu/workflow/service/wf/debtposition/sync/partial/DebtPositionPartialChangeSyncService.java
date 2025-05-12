package it.gov.pagopa.pu.workflow.service.wf.debtposition.sync.partial;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DebtPositionPartialChangeSyncService {

  public WorkflowCreatedDTO invokeWorkflow(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest) {
    log.info("No sync WF are expected to be executed when partialChange, ignoring sync op requested for debtPositionId {} (paymentEventType: {})",
      debtPositionDTO.getDebtPositionId(),
      paymentEventRequest != null ? paymentEventRequest.getPaymentEventType() : null);
    return null;
  }
}
