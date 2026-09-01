package it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;

import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.wf.AlignCountersAllCampaignsWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.wf.AlignCountersUpdatedCampaignsWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.service.temporal.WorkflowScheduleServiceImpl.ON_DEMAND_SCHEDULE_SUFFIX;
import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class SendCampaignWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public SendCampaignWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public WorkflowCreatedDTO startAlignActiveSendCampaignCounters() {
    String taskQueue = TaskQueueConstants.TASK_QUEUE_SEND_MEDIUM_PRIORITY;
    String workflowId = generateWorkflowId(ON_DEMAND_SCHEDULE_SUFFIX, AlignCountersAllCampaignsWF.class);

    AlignCountersAllCampaignsWF workflow = workflowService.buildWorkflowStubToStartNew(
      AlignCountersAllCampaignsWF.class,
      taskQueue,
      workflowId
    );
    return workflowClientService.start(workflow::alignCountersForAllActiveCampaigns, null); //for starting we do not pass any campaignId
  }

  public WorkflowCreatedDTO startAlignUpdatedSendCampaignCounters() {
    String taskQueue = TaskQueueConstants.TASK_QUEUE_SEND_MEDIUM_PRIORITY;
    String workflowId = generateWorkflowId(ON_DEMAND_SCHEDULE_SUFFIX, AlignCountersUpdatedCampaignsWF.class);

    AlignCountersUpdatedCampaignsWF workflow = workflowService.buildWorkflowStubToStartNew(
      AlignCountersUpdatedCampaignsWF.class,
      taskQueue,
      workflowId
    );
    return workflowClientService.start(workflow::alignCountersForUpdatedCampaigns, null, null, null); //for starting we do not pass any parameter
  }
}
