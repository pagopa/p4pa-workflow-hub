package it.gov.pagopa.pu.workflow.wf.debtposition.createdp.wfsync;

import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionDTO;

public interface CreateDebtPositionSyncWF {

  void createDPSync(DebtPositionDTO debtPosition);
}
