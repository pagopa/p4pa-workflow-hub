package it.gov.pagopa.pu.workflow.service.wf.send.campaign;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.SendCampaignWFClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SendCampaignServiceImpl implements SendCampaignService {

  private final SendCampaignWFClient sendCampaignWFClient;

  public SendCampaignServiceImpl(SendCampaignWFClient sendCampaignWFClient) {
    this.sendCampaignWFClient = sendCampaignWFClient;
  }

  @Override
  public WorkflowCreatedDTO alignActiveSendCampaignCounters() {
    return sendCampaignWFClient.startAlignActiveSendCampaignCounters();
  }

  @Override
  public WorkflowCreatedDTO alignUpdatedSendCampaignCounters() {
    return sendCampaignWFClient.startAlignUpdatedSendCampaignCounters();
  }

}
