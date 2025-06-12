package it.gov.pagopa.pu.workflow.wf.classification.iud.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.classifications.ClearClassifyIudActivity;
import it.gov.pagopa.payhub.activities.activity.classifications.IudClassificationActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.activity.StartTransferClassificationActivity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.iud-classification")
public class IudClassificationWfConfig extends BaseWfConfig {

  public ClearClassifyIudActivity buildClearClassifyIudActivityStub() {
    return Workflow.newActivityStub(ClearClassifyIudActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public IudClassificationActivity buildIudClassificationActivityStub() {
    return Workflow.newActivityStub(IudClassificationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public StartTransferClassificationActivity buildStartTransferClassificationActivityStub() {
    return Workflow.newActivityStub(StartTransferClassificationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
      TaskQueueConstants.TASK_QUEUE_CLASSIFICATION_MEDIUM_PRIORITY_LOCAL,
      this));
  }
}

