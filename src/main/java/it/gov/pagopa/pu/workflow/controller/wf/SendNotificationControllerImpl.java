package it.gov.pagopa.pu.workflow.controller.wf;

import it.gov.pagopa.pu.workflow.controller.generated.SendNotificationApi;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.wf.send.SendNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class SendNotificationControllerImpl implements SendNotificationApi {

  private final SendNotificationService service;

  public SendNotificationControllerImpl(SendNotificationService service) {
    this.service = service;
  }

  @Override
  public ResponseEntity<WorkflowCreatedDTO> sendNotificationProcess(String sendNotificationId) {
    log.info("Starting send notification process for sendNotificationId: {}", sendNotificationId);
    WorkflowCreatedDTO createWorkflowResponseDTO = service.sendNotificationProcess(sendNotificationId);
    return new ResponseEntity<>(createWorkflowResponseDTO, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<WorkflowCreatedDTO> consumeSendStream(String sendStreamId) {
    log.info("Starting stream consuming workflow for sendStreamId: {}", sendStreamId);
    WorkflowCreatedDTO createWorkflowResponseDTO = service.sendNotificationStreamConsume(sendStreamId);
    return new ResponseEntity<>(createWorkflowResponseDTO, HttpStatus.OK);
  }

}
