package it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;

import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.wf.AlignSendCampaignCountersWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

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

  public WorkflowCreatedDTO startAlignSendCampaignCounters() {
    String taskQueue = TaskQueueConstants.TASK_QUEUE_SEND_MEDIUM_PRIORITY;
    String uuid = UUID.randomUUID().toString();
    String workflowId = generateWorkflowId(uuid, AlignSendCampaignCountersWF.class);

    AlignSendCampaignCountersWF workflow = workflowService.buildWorkflowStubToStartNew(
      AlignSendCampaignCountersWF.class,
      taskQueue,
      workflowId
    );
    return workflowClientService.start(workflow::alignCountersForAllActiveCampaigns, null); //for starting we do not pass any campaignId
  }
}
