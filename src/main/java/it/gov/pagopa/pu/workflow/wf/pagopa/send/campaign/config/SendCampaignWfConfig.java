package it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.campaign.AlignSendCampaignActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.campaign.FetchSendCampaignsActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.campaign.FetchSendCampaignsLastFullRecalculationDateActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.campaign.FetchUpdatedSendCampaignsActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.send-campaign")
public class SendCampaignWfConfig extends BaseWfConfig {

  public AlignSendCampaignActivity buildAlignSendCampaignActivityStub() {
    return Workflow.newActivityStub(AlignSendCampaignActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public FetchSendCampaignsActivity buildFetchSendCampaignsActivityStub() {
    return Workflow.newActivityStub(FetchSendCampaignsActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public FetchUpdatedSendCampaignsActivity buildFetchUpdatedSendCampaignsActivityStub() {
    return Workflow.newActivityStub(FetchUpdatedSendCampaignsActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public FetchSendCampaignsLastFullRecalculationDateActivity buildFetchSendCampaignsLastFullRecalculationDateActivityStub() {
    return Workflow.newActivityStub(FetchSendCampaignsLastFullRecalculationDateActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

}
