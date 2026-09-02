package it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.wf;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface AlignCountersAllCampaignsWF {
  @WorkflowMethod
  void alignCountersForAllActiveCampaigns(String idOfLatestAlignedCampaign);
}
