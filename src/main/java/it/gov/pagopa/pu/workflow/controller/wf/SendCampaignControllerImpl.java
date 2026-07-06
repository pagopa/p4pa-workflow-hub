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
  public ResponseEntity<WorkflowCreatedDTO> alignSendCampaignCounters() {
    log.info("Starting send campaign counters alignment process");
    WorkflowCreatedDTO workflowCreatedDTO = service.alignSendCampaignCounters();
    return new ResponseEntity<>(workflowCreatedDTO, HttpStatus.OK);
  }
}
