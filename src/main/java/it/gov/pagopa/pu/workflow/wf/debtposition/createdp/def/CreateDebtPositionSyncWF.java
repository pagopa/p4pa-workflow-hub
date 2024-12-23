package it.gov.pagopa.pu.workflow.wf.debtposition.createdp.def;

import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionDTO;

public interface CreateDebtPositionSyncWF {

  void ingest(DebtPositionDTO debtPosition);
}
