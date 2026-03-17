package it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.notice.FetchAndMergeNoticesActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.massive-notices-generation")
public class MassiveNoticesGenerationWFConfig extends BaseWfConfig {
  public FetchAndMergeNoticesActivity buildFetchAndMergeNoticesActivityStub() {
    return Workflow.newActivityStub(FetchAndMergeNoticesActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }
}
