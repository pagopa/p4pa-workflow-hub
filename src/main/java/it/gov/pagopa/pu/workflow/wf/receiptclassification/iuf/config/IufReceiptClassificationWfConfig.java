package it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.classifications.*;

import it.gov.pagopa.pu.workflow.config.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.helper.TransferClassificationWfHelperActivity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "workflow.iuf-receipt-classification")
public class IufReceiptClassificationWfConfig extends BaseWfConfig {

  public ClearClassifyIufActivity buildClearClassifyIufActivityStub() {
    return Workflow.newActivityStub(ClearClassifyIufActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public IufClassificationActivity buildIufClassificationActivityStub() {
    return Workflow.newActivityStub(IufClassificationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public TransferClassificationActivity buildTransferClassificationActivityStub() {
    return Workflow.newActivityStub(TransferClassificationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  // helper activity
  public TransferClassificationWfHelperActivity buildTransferClassificationStarterHelperActivityStub() {
    return Workflow.newActivityStub(TransferClassificationWfHelperActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

}
