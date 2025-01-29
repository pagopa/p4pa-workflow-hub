package it.gov.pagopa.pu.workflow.service.debtposition;

import it.gov.pagopa.pu.workflow.dto.generated.DebtPositionRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;

public interface DebtPositionService {

  WorkflowCreatedDTO createDPSync(DebtPositionRequestDTO debtPosition);

  WorkflowCreatedDTO alignDpSyncAca(DebtPositionRequestDTO debtPosition);
}
