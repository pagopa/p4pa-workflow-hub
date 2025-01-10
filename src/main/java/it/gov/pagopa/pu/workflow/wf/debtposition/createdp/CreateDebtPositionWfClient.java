package it.gov.pagopa.pu.workflow.wf.debtposition.createdp;

import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionDTO;


public interface CreateDebtPositionWfClient {

  String createDPSync(DebtPositionDTO debtPositionDTO);
}
