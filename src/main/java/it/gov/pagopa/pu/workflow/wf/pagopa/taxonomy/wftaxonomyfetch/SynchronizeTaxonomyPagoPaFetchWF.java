
package it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy.wftaxonomyfetch;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;


/**
 * Workflow interface for the Synchronize Taxonomy Fetch Workflow
 */

@WorkflowInterface
public interface SynchronizeTaxonomyPagoPaFetchWF {


  /**
   * Workflow method for the Synchronize Taxonomy Fetch Workflow
   */

  @WorkflowMethod
  void synchronize();
}

