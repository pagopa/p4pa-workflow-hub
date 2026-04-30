package it.gov.pagopa.pu.workflow.service.wf.debtposition;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.MassiveDebtPositionIbanUpdateRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;

public interface DebtPositionService {
  WorkflowCreatedDTO syncDebtPosition(DebtPositionDTO debtPosition, PaymentEventRequestDTO paymentEventRequest, WfExecutionParameters wfExecutionParameters, String accessToken);
  WorkflowCreatedDTO checkDpExpiration(Long debtPositionId);
  WorkflowCreatedDTO massiveIbanUpdate(Long orgId, MassiveDebtPositionIbanUpdateRequestDTO massiveDebtPositionIbanUpdateRequestDTO);
}
