
package it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy.wftaxonomyfetch;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.taxonomy.SynchronizeTaxonomyActivity;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy.config.SynchronizeTaxonomyPagoPaFetchWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
@WorkflowImpl(taskQueues = {SynchronizeTaxonomyPagoPaFetchWFImpl.TASK_QUEUE_SYNCHRONIZE_TAXONOMY_PAGOPA_FETCH})
public class SynchronizeTaxonomyPagoPaFetchWFImpl implements SynchronizeTaxonomyPagoPaFetchWF, ApplicationContextAware {
  public static final String TASK_QUEUE_SYNCHRONIZE_TAXONOMY_PAGOPA_FETCH = "SynchronizeTaxonomyPagoPaFetchWF";

  private SynchronizeTaxonomyActivity synchronizeTaxonomyActivity;


/**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    SynchronizeTaxonomyPagoPaFetchWfConfig wfConfig = applicationContext.getBean(SynchronizeTaxonomyPagoPaFetchWfConfig.class);
    synchronizeTaxonomyActivity = wfConfig.buildSynchronizeTaxonomyActivityStub();

  }

  @Override
  public void synchronize() {
    log.info("Synchronize Taxonomies");
    Integer synchronizedTaxonomies = synchronizeTaxonomyActivity.syncTaxonomy();
    log.info("Synchronized taxonomies: {}", synchronizedTaxonomies);

  }
}

