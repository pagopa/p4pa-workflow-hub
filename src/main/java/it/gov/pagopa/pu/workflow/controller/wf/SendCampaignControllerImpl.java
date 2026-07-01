package it.gov.pagopa.pu.workflow.controller.wf;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.wf.send.campaign.SendCampaignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class SendCampaignControllerImpl {

  private final SendCampaignService service;

  public SendCampaignControllerImpl(SendCampaignService service) {
    this.service = service;
  }

  public ResponseEntity<WorkflowCreatedDTO> sendCampaignCountersRefresh() {
    log.info("Starting send campaign counters refresh process");
    WorkflowCreatedDTO workflowCreatedDTO = service.sendCampaignCountersRefresh();
    return new ResponseEntity<>(workflowCreatedDTO, HttpStatus.OK);
  }
}
