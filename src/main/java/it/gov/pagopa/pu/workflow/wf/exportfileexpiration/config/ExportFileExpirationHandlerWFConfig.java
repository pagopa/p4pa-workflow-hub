package it.gov.pagopa.pu.workflow.wf.exportfileexpiration.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.exportflow.ExportFileExpirationHandlerActivity;
import it.gov.pagopa.pu.workflow.config.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.export-file-expiration-handler")
public class ExportFileExpirationHandlerWFConfig extends BaseWfConfig {

  public ExportFileExpirationHandlerActivity buildExportFileExpirationHandlerActivityStub() {
    return Workflow.newActivityStub(ExportFileExpirationHandlerActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }
}
