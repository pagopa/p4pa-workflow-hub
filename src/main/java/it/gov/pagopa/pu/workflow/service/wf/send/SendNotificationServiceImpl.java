package it.gov.pagopa.pu.workflow.service.wf.send;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.SendNotificationWFClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SendNotificationServiceImpl implements SendNotificationService {

  private final SendNotificationWFClient sendNotificationWFClient;

  public SendNotificationServiceImpl(SendNotificationWFClient sendNotificationWFClient) {
    this.sendNotificationWFClient = sendNotificationWFClient;
  }

  @Override
  public WorkflowCreatedDTO sendNotificationProcess(String sendNotificationId) {
    log.debug("Starting send notification process with sendNotificationId: {}", sendNotificationId);
    return sendNotificationWFClient.startSendNotificationProcess(sendNotificationId);
  }

  @Override
  public WorkflowCreatedDTO sendNotificationStreamConsume(String sendStreamId) {
    return sendNotificationWFClient.startSendNotificationStreamConsume(sendStreamId);
  }

  @Override
  public WorkflowCreatedDTO sendNotificationStreamConsume(String sendStreamId) {
    return sendNotificationWFClient.startSendNotificationStreamConsume(sendStreamId);
  }
}
