package it.gov.pagopa.pu.workflow.wf.debtposition.handledp;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;


public interface HandleDebtPositionWfClient {

  String handleDPSync(DebtPositionDTO debtPositionDTO);

}
