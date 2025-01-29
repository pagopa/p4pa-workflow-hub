package it.gov.pagopa.pu.workflow.wf.debtposition.aligndp;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;


public interface SynchronizeSyncAcaWfClient {

  String synchronizeDPSyncAca(DebtPositionDTO debtPositionDTO);
}
