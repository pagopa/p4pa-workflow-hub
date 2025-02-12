package it.gov.pagopa.pu.workflow.service.debtposition;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;

public interface DebtPositionService {
  WorkflowCreatedDTO syncDebtPosition(DebtPositionDTO debtPosition, PaymentEventType paymentEventType, boolean massive, String accessToken);
  WorkflowCreatedDTO checkDpExpiration(Long debtPositionId);
}
