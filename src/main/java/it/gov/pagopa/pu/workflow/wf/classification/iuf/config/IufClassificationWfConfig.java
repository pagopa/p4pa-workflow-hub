package it.gov.pagopa.pu.workflow.wf.classification.iuf.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.classifications.*;

import it.gov.pagopa.pu.workflow.config.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.activity.StartTransferClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.wfclassification.IufClassificationWFImpl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "workflow.iuf-classification")
public class IufClassificationWfConfig extends BaseWfConfig {

  public ClearClassifyIufActivity buildClearClassifyIufActivityStub() {
    return Workflow.newActivityStub(ClearClassifyIufActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public IufClassificationActivity buildIufClassificationActivityStub() {
    return Workflow.newActivityStub(IufClassificationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public StartTransferClassificationActivity buildStartTransferClassificationActivityStub() {
    return Workflow.newActivityStub(StartTransferClassificationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
      IufClassificationWFImpl.TASK_QUEUE_IUF_CLASSIFICATION_LOCAL_ACTIVITY,
      this));
  }

}

