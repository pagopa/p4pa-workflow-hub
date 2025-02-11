package it.gov.pagopa.pu.workflow.service.debtposition;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.event.payments.enums.PaymentEventType;

public interface DebtPositionService {
  WorkflowCreatedDTO syncDebtPosition(DebtPositionDTO debtPosition, PaymentEventType paymentEventType, boolean massive, String accessToken);
  WorkflowCreatedDTO checkDpExpiration(Long debtPositionId);
}
