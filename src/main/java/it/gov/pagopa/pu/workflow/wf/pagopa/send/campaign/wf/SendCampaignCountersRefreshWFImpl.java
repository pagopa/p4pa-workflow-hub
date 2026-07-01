package it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.wf;

import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.campaign.AlignSendCampaignActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.campaign.FetchSendCampaignsActivity;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.config.SendCampaignWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.ListIterator;

import static it.gov.pagopa.pu.workflow.utilities.Constants.THRESHOLD_TEMPORAL_EVENTS_BEFORE_CONTINUE_AS_NEW;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_NOTIFICATION) //TODO create/refer new task queue
public class SendCampaignCountersRefreshWFImpl implements SendCampaignCountersRefreshWF, ApplicationContextAware {

  private FetchSendCampaignsActivity fetchSendCampaignsActivity;
  private AlignSendCampaignActivity alignSendCampaignActivity;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    SendCampaignWfConfig wfConfig = applicationContext.getBean(SendCampaignWfConfig.class);

    fetchSendCampaignsActivity = wfConfig.buildFetchSendCampaignsActivityStub();
    alignSendCampaignActivity = wfConfig.buildAlignSendCampaignActivityStub();
  }

  @Override
  public void refreshCountersForAllActiveCampaigns(String idOfLatestAlignedCampaign) {
    log.info("Start refreshCountersForAllActiveCampaigns Workflow, starting from campaign with id {}", idOfLatestAlignedCampaign);

    List<String> campaignIds = fetchSendCampaignsActivity.fetchSendCampaignIds()
      .stream()
      .sorted()
      .toList();
    int indexOfLatestAlignedCampaign = idOfLatestAlignedCampaign != null ? campaignIds.indexOf(idOfLatestAlignedCampaign) : -1;
    ListIterator<String> campaignIterator = campaignIds.listIterator(indexOfLatestAlignedCampaign + 1);
    int activityCounter = 0;
    while (campaignIterator.hasNext() && activityCounter < THRESHOLD_TEMPORAL_EVENTS_BEFORE_CONTINUE_AS_NEW) {
      try {
        alignSendCampaignActivity.alignSendCampaign(campaignIterator.next());
      } catch (Exception e) {
        log.error("Something when wrong during counters alignment for send campaign with id {}; error message: {}", campaignIds.get(campaignIterator.previousIndex()), e.getMessage());
      } finally {
        activityCounter++;
      }
    }
    if(campaignIterator.hasNext()) { //if there are more campaign to be aligned, start a new workflow run
      Workflow.continueAsNew(campaignIterator.previous()); //start new workflow run from id of latest aligned campaign
    }
  }

}
