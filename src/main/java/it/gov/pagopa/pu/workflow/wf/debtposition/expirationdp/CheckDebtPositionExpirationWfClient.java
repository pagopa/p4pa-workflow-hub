package it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;

import java.time.LocalDate;

public interface CheckDebtPositionExpirationWfClient {

  WorkflowCreatedDTO checkDpExpiration(Long debtPositionId);
  void scheduleNextCheckDpExpiration(Long debtPositionId, LocalDate nextDueDate);
  void cancelScheduling(Long debtPositionId);
}
