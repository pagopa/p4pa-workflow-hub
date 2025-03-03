package it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.taxonomy.SynchronizeTaxonomyActivity;
import it.gov.pagopa.pu.workflow.config.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.synchronize-taxonomy-pagopa-fetch")
public class SynchronizeTaxonomyPagoPaFetchWfConfig extends BaseWfConfig {

  public SynchronizeTaxonomyActivity buildSynchronizeTaxonomyActivityStub() {
    return Workflow.newActivityStub(SynchronizeTaxonomyActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }
}
