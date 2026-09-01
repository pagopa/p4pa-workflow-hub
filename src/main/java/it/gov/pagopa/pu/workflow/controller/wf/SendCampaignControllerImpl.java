package it.gov.pagopa.pu.workflow.controller.wf;

import it.gov.pagopa.pu.workflow.controller.generated.SendCampaignApi;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.wf.send.campaign.SendCampaignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class SendCampaignControllerImpl implements SendCampaignApi {

  private final SendCampaignService service;

  public SendCampaignControllerImpl(SendCampaignService service) {
    this.service = service;
  }

  @Override
  public ResponseEntity<WorkflowCreatedDTO> alignActiveSendCampaignCounters() {
    log.info("Starting active send campaign counters alignment process");
    WorkflowCreatedDTO workflowCreatedDTO = service.alignActiveSendCampaignCounters();
    return new ResponseEntity<>(workflowCreatedDTO, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<WorkflowCreatedDTO> alignUpdatedSendCampaignCounters() {
    log.info("Starting updated send campaign counters alignment process");
    WorkflowCreatedDTO workflowCreatedDTO = service.alignUpdatedSendCampaignCounters();
    return new ResponseEntity<>(workflowCreatedDTO, HttpStatus.OK);
  }
}
