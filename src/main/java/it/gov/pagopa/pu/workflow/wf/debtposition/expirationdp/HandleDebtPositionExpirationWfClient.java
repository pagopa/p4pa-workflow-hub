package it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;

public interface HandleDebtPositionExpirationWfClient {

  String handleDpExpiration(DebtPositionDTO debtPosition);
}
