package it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.wfexpiration;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;

import java.time.OffsetDateTime;

/**
 * Workflow interface for handling Debt Position Expiration Workflow
 * */
@WorkflowInterface
public interface HandleDebtPositionExpirationWF {

  /**
   * Workflow method for the Debt Position Expiration Workflow
   * @param debtPosition the debt position whose expiration is to be verified
   * */
  @WorkflowMethod
  OffsetDateTime handleDpExpiration(DebtPositionDTO debtPosition);

}
