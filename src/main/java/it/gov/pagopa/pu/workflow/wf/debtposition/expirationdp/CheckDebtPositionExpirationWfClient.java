package it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp;

import java.time.LocalDate;

public interface CheckDebtPositionExpirationWfClient {

  String checkDpExpiration(Long debtPositionId);
  void scheduleNextCheckDpExpiration(Long debtPositionId, LocalDate nextDueDate);
  void cancelScheduling(Long debtPositionId);
}
