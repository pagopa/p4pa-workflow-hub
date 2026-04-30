package it.gov.pagopa.pu.workflow.service.wf.send;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.create.SendNotificationProcessWFClient;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.SendNotificationStreamWFClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SendNotificationServiceImpl implements SendNotificationService {

  private final SendNotificationProcessWFClient sendNotificationProcessWFClient;
  private final SendNotificationStreamWFClient sendNotificationStreamWFClient;

  public SendNotificationServiceImpl(SendNotificationProcessWFClient sendNotificationProcessWFClient, SendNotificationStreamWFClient sendNotificationStreamWFClient) {
    this.sendNotificationProcessWFClient = sendNotificationProcessWFClient;
    this.sendNotificationStreamWFClient = sendNotificationStreamWFClient;
  }

  @Override
  public WorkflowCreatedDTO sendNotificationProcess(String sendNotificationId) {
    log.debug("Starting send notification process with sendNotificationId: {}", sendNotificationId);
    return sendNotificationProcessWFClient.startSendNotificationProcess(sendNotificationId);
  }

  @Override
  public WorkflowCreatedDTO sendNotificationStreamConsume(String sendStreamId) {
    return sendNotificationStreamWFClient.startSendNotificationStreamConsume(sendStreamId);
  }

}
