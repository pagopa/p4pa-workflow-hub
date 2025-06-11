package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.treasury.TreasuryOpiIngestionActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.activity.NotifyTreasuryToIufClassificationActivity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.treasury-opi-ingestion")
public class TreasuryOpiIngestionWfConfig extends BaseWfConfig {

  public TreasuryOpiIngestionActivity buildTreasuryOpiIngestionActivityStub() {
    return Workflow.newActivityStub(TreasuryOpiIngestionActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public NotifyTreasuryToIufClassificationActivity buildNotifyTreasuryToIufClassificationActivityStub() {
    return Workflow.newActivityStub(NotifyTreasuryToIufClassificationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
      TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY_LOCAL,
      this));
  }
}
