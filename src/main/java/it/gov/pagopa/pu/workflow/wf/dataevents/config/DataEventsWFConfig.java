package it.gov.pagopa.pu.workflow.wf.dataevents.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.wf.dataevents.activity.PublishDataEventsActivity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.default")
public class DataEventsWFConfig extends BaseWfConfig {

  public PublishDataEventsActivity buildPublishDataEventActivityStub() {
    return Workflow.newActivityStub(PublishDataEventsActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }
}
