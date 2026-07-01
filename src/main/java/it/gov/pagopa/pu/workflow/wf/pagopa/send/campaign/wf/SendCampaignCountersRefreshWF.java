package it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.wf;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface SendCampaignCountersRefreshWF {
  @WorkflowMethod
  void refreshCountersForAllActiveCampaigns(String idOfLatestAlignedCampaign);
}
