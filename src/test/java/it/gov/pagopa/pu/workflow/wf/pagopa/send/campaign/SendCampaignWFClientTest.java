package it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.wf.AlignSendCampaignCountersWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.wf.AlignSendCampaignCountersWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class SendCampaignWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private AlignSendCampaignCountersWF alignSendCampaignCountersWF;

  private SendCampaignWFClient client;

  @BeforeEach
  void setUp() {
    client = new SendCampaignWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void startAlignSendCampaignCounters() {
    // Given
    String taskQueue = TaskQueueConstants.TASK_QUEUE_SEND_MEDIUM_PRIORITY;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("AlignSendCampaignCountersWF-1", "RUNID");

    Mockito.when(workflowServiceMock.buildWorkflowStubToStartNew(Mockito.eq(AlignSendCampaignCountersWF.class), Mockito.eq(taskQueue), Mockito.startsWith("AlignSendCampaignCountersWF-")))
      .thenReturn(alignSendCampaignCountersWF);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, null);

    // When
    WorkflowCreatedDTO result = client.startAlignSendCampaignCounters();

    // Then
    assertEquals(expectedResult, result);
    verify(alignSendCampaignCountersWF).alignCountersForAllActiveCampaigns(Mockito.isNull());

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, AlignSendCampaignCountersWFImpl.class);
  }
}
