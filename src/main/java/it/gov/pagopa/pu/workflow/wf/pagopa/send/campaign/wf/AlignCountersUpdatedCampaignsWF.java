package it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.wf;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.time.OffsetDateTime;

@WorkflowInterface
public interface AlignCountersUpdatedCampaignsWF {
  @WorkflowMethod
  void alignCountersForUpdatedCampaigns(OffsetDateTime lastFullRecalculationDate, OffsetDateTime newFullRecalculationDate, String idOfLatestAlignedCampaign);
}
