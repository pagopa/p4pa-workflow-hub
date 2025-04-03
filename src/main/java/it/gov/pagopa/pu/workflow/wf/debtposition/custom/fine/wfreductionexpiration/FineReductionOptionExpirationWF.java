package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.wfreductionexpiration;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;

/**
 * Workflow to handle Debt Position Fine reduction expiration
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1584627929/Multe#3.2.-Workflow-schedulati%3A-Scadenza-periodo-di-sconto>Confluence page</a>
 * */
@WorkflowInterface
public interface FineReductionOptionExpirationWF {

  @WorkflowMethod
  String expireFineReduction(Long debtPositionId, FineWfExecutionConfig executionParams);
}
