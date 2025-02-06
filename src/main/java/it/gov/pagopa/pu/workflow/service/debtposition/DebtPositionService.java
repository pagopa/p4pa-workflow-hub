package it.gov.pagopa.pu.workflow.service.debtposition;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;

public interface DebtPositionService {

  WorkflowCreatedDTO handleDPSync(DebtPositionDTO debtPosition);

  WorkflowCreatedDTO alignDpSyncAca(DebtPositionDTO debtPosition);

  WorkflowCreatedDTO checkDpExpiration(Long debtPositionId);
}
