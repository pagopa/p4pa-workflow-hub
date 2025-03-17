
package it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy.wftaxonomyfetch;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;


/**
 * Workflow to synchronize Taxonomy with PagoPa
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1519386843/Aggiornamento+Tassonomia>Confluence page</a>
 */

@WorkflowInterface
public interface SynchronizeTaxonomyPagoPaFetchWF {
  @WorkflowMethod
  void synchronize();
}

