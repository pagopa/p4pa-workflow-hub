package it.gov.pagopa.pu.workflow.wf.pagopa.paidinstallments.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.receipt.DeletePaidInstallmentsOnPagoPaActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.delete-paid-installments-pagopa")
public class DeletePaidInstallmentsOnPagoPaWfConfig extends BaseWfConfig {

  public DeletePaidInstallmentsOnPagoPaActivity buildDeletePaidInstallmentsOnPagoActivityStub() {
    return Workflow.newActivityStub(DeletePaidInstallmentsOnPagoPaActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

}
