package it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.wfexpiration;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow interface for checking Debt Position Expiration Workflow
 * */
@WorkflowInterface
public interface CheckDebtPositionExpirationWF {

  /**
   * Workflow method for the Debt Position Expiration Workflow
   * @param debtPositionId the id of debt position whose expiration is to be checked
   * */
  @WorkflowMethod
  void checkDpExpiration(Long debtPositionId);

}
