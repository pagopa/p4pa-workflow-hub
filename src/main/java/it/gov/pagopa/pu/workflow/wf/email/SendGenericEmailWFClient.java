package it.gov.pagopa.pu.workflow.wf.email;

import it.gov.pagopa.payhub.activities.dto.email.EmailDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.email.wf.SendGenericEmailWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class SendGenericEmailWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public SendGenericEmailWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public WorkflowCreatedDTO sendEmail(EmailDTO emailDTO, Long brokerId) {
    log.info("Sending email");

    String taskQueue = TaskQueueConstants.TASK_QUEUE_LOW_PRIORITY;
    String workflowId  = generateWorkflowId(String.valueOf(emailDTO.hashCode()), SendGenericEmailWF.class);

    SendGenericEmailWF workflow = workflowService.buildWorkflowStubToStartNew(
      SendGenericEmailWF.class,
      taskQueue,
      workflowId);
    return workflowClientService.start(workflow::sendGenericEmail, emailDTO, brokerId);
  }
}
