package it.gov.pagopa.pu.workflow.wf.classification.transfer.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.classifications.TransferClassificationActivity;
import it.gov.pagopa.pu.workflow.config.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.transfer-classification")
public class TransferClassificationWfConfig extends BaseWfConfig {

  public TransferClassificationActivity buildTransferClassificationActivityStub() {
    return Workflow.newActivityStub(TransferClassificationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }
}
