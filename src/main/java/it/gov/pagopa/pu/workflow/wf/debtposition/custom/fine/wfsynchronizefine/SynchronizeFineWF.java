package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.wfsynchronizefine;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;

/**
 * Workflow to synchronize a Debt Position Fine
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1584627929/Multe#4.1.-Workflow-Custom>Confluence page</a>
 * */
@WorkflowInterface
public interface SynchronizeFineWF {

  @WorkflowMethod
  void synchronizeFineDP(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, Boolean massivo, FineWfExecutionConfig executionParams);
}
