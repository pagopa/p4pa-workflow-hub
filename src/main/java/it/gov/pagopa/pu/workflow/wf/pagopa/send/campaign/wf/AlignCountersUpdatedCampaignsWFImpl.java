package it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.wf;

import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.campaign.AlignSendCampaignActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.campaign.FetchSendCampaignsLastFullRecalculationDateActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.campaign.FetchUpdatedSendCampaignsActivity;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.config.SendCampaignWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.ListIterator;

import static it.gov.pagopa.pu.workflow.utilities.Constants.THRESHOLD_TEMPORAL_EVENTS_BEFORE_CONTINUE_AS_NEW;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_SEND_MEDIUM_PRIORITY)
public class AlignCountersUpdatedCampaignsWFImpl implements AlignCountersUpdatedCampaignsWF, ApplicationContextAware {

  private FetchSendCampaignsLastFullRecalculationDateActivity fetchSendCampaignsLastFullRecalculationDateActivity;
  private FetchUpdatedSendCampaignsActivity fetchUpdatedSendCampaignsActivity;
  private AlignSendCampaignActivity alignSendCampaignActivity;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    SendCampaignWfConfig wfConfig = applicationContext.getBean(SendCampaignWfConfig.class);

    fetchSendCampaignsLastFullRecalculationDateActivity = wfConfig.buildFetchSendCampaignsLastFullRecalculationDateActivityStub();
    fetchUpdatedSendCampaignsActivity = wfConfig.buildFetchUpdatedSendCampaignsActivityStub();
    alignSendCampaignActivity = wfConfig.buildAlignSendCampaignActivityStub();
  }

  @Override
  public void alignCountersForUpdatedCampaigns(OffsetDateTime lastFullRecalculationDate, OffsetDateTime newFullRecalculationDate, String idOfLatestAlignedCampaign) {
    log.info("Start alignCountersForUpdatedCampaigns workflow");

    if(newFullRecalculationDate == null) {
      newFullRecalculationDate = Utilities.getWorkflowDeterministicOffsetDateTime();
    }
    if(lastFullRecalculationDate == null) {
      lastFullRecalculationDate = fetchSendCampaignsLastFullRecalculationDateActivity.fetchSendCampaignsLastFullRecalculationDate();
    }
    List<String> updatedCampaignIds = fetchUpdatedSendCampaignsActivity.fetchIdsForUpdatedSendCampaigns(lastFullRecalculationDate);
    int indexOfLatestAlignedCampaign = idOfLatestAlignedCampaign != null ? updatedCampaignIds.indexOf(idOfLatestAlignedCampaign) : -1;
    ListIterator<String> updatedCampaignIterator = updatedCampaignIds.listIterator(indexOfLatestAlignedCampaign + 1);
    int activityCounter = 0;
    while (updatedCampaignIterator.hasNext() && activityCounter < THRESHOLD_TEMPORAL_EVENTS_BEFORE_CONTINUE_AS_NEW) {
      try {
        alignSendCampaignActivity.alignSendCampaign(updatedCampaignIterator.next(), newFullRecalculationDate);
      } catch (Exception e) {
        log.warn("Something went wrong during counters alignment for send campaign with id {}; error message: {}", updatedCampaignIds.get(updatedCampaignIterator.previousIndex()), Utilities.getWorkflowExceptionMessage(e));
      } finally {
        activityCounter++;
      }
    }
    if(updatedCampaignIterator.hasNext()) { //if there are more campaigns to be aligned, start a new workflow run
      Workflow.continueAsNew(lastFullRecalculationDate, newFullRecalculationDate, updatedCampaignIterator.previous()); //start new workflow run from id of latest aligned campaign, same lastFullRecalculationDate fix the campaignIds list
    }
  }
}
