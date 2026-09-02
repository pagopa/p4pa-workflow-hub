package it.gov.pagopa.pu.workflow.service.wf.send.campaign;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.SendCampaignWFClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendCampaignServiceImplTest {
  @Mock
  private SendCampaignWFClient sendCampaignWFClientMock;

  private SendCampaignService sendCampaignService;

  @BeforeEach
  void init(){
    sendCampaignService = new SendCampaignServiceImpl(
      sendCampaignWFClientMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      sendCampaignWFClientMock
    );
  }

  @Test
  void whenAlignActiveSendCampaignCountersThenOk() {
    // Given
    WorkflowCreatedDTO expectedResult = WorkflowCreatedDTO.builder()
      .workflowId("WFID")
      .runId("RUNID")
      .build();

    when(sendCampaignWFClientMock.startAlignActiveSendCampaignCounters())
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = sendCampaignService.alignActiveSendCampaignCounters();

    // Then
    Assertions.assertEquals(expectedResult, result);
  }

  @Test
  void whenAlignUpdatedSendCampaignCountersThenOk() {
    // Given
    WorkflowCreatedDTO expectedResult = WorkflowCreatedDTO.builder()
      .workflowId("WFID")
      .runId("RUNID")
      .build();

    when(sendCampaignWFClientMock.startAlignUpdatedSendCampaignCounters())
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = sendCampaignService.alignUpdatedSendCampaignCounters();

    // Then
    Assertions.assertEquals(expectedResult, result);
  }
}
