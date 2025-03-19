package it.gov.pagopa.pu.workflow.service.debtposition;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;

public interface DebtPositionService {
  WorkflowCreatedDTO syncDebtPosition(DebtPositionDTO debtPosition, PaymentEventType paymentEventType, WfExecutionParameters wfExecutionParameters, String accessToken);
  WorkflowCreatedDTO checkDpExpiration(Long debtPositionId);
}
