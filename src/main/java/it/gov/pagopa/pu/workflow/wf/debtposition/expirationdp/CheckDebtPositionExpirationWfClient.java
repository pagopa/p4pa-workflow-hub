package it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp;

import java.time.OffsetDateTime;

public interface CheckDebtPositionExpirationWfClient {

  String checkDpExpiration(Long debtPositionId);

  void scheduleNextCheckDpExpiration(Long debtPositionId, OffsetDateTime dateTime);
}
