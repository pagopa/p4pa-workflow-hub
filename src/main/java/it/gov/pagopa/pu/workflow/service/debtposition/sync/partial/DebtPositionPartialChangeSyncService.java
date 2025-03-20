package it.gov.pagopa.pu.workflow.service.debtposition.sync.partial;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DebtPositionPartialChangeSyncService {

  public String invokeWorkflow(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType){
    log.info("No sync WF are expected to be executed when partialChange, ignoring sync op requested for debtPositionId {} (paymentEventType: {})", debtPositionDTO.getDebtPositionId(), paymentEventType);
    return null;
  }
}
