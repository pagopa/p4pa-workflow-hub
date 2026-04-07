package it.gov.pagopa.pu.workflow.wf.debtposition.massive.wfmassiveibanupdate;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;

/**
 * Workflow to update IBAN massively
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/2403565636/Aggiornamento+IBAN+massivo>Confluence page</a>
 */
@WorkflowInterface
public interface MassiveIbanUpdateWF {
  @WorkflowMethod
  void massiveIbanUpdate(Long orgId, Long dptoId, String oldIban, String newIban, String oldPostalIban, String newPostalIban);
}
