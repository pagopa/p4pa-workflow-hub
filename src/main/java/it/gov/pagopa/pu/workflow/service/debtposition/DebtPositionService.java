package it.gov.pagopa.pu.workflow.service.debtposition;

import it.gov.pagopa.pu.workflow.dto.generated.CreateDpSyncResponseDTO;
import it.gov.pagopa.pu.workflow.dto.generated.DebtPositionRequestDTO;

public interface DebtPositionService {

  CreateDpSyncResponseDTO createDPSync(DebtPositionRequestDTO debtPosition);
}
