package it.gov.pagopa.pu.workflow.wf.debtposition.createdp;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;


public interface CreateDebtPositionWfClient {

  String createDPSync(DebtPositionDTO debtPositionDTO);
}
