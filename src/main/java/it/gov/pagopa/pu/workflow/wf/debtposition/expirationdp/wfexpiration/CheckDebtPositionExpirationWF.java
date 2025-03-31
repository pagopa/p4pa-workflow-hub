package it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.wfexpiration;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow to handle Installment expired on a Debt Position
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1471250821/Scadenza+di+un+Installment?force_transition=c9971907-9825-456d-aa49-a9b0b630c5c8>Confluence page</a>
 * */
@WorkflowInterface
public interface CheckDebtPositionExpirationWF {
  @WorkflowMethod
  void checkDpExpiration(Long debtPositionId);
}
