package it.gov.pagopa.pu.workflow.service.debtposition.custom.fine;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;

public interface DebtPositionFineService {

  WorkflowCreatedDTO expireFineReduction(Long debtPositionId);
}
