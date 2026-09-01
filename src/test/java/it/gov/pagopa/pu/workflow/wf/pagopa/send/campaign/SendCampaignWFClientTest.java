package it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.wf.AlignCountersAllCampaignsWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.wf.AlignCountersAllCampaignsWFImpl;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.wf.AlignCountersUpdatedCampaignsWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.wf.AlignCountersUpdatedCampaignsWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendCampaignWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private AlignCountersAllCampaignsWF alignCountersAllCampaignsWF;
  @Mock
  private AlignCountersUpdatedCampaignsWF alignCountersUpdatedCampaignsWF;

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
  void startAlignAllCampaignCounters() {
    // Given
    String taskQueue = TaskQueueConstants.TASK_QUEUE_SEND_MEDIUM_PRIORITY;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("AlignCountersAllCampaignsWF-1", "RUNID");

    when(workflowServiceMock.buildWorkflowStubToStartNew(Mockito.eq(AlignCountersAllCampaignsWF.class), Mockito.eq(taskQueue), Mockito.startsWith("AlignCountersAllCampaignsWF-")))
      .thenReturn(alignCountersAllCampaignsWF);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, null);

    // When
    WorkflowCreatedDTO result = client.startAlignActiveSendCampaignCounters();

    // Then
    assertEquals(expectedResult, result);
    verify(alignCountersAllCampaignsWF).alignCountersForAllActiveCampaigns(Mockito.isNull());

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, AlignCountersAllCampaignsWFImpl.class);
  }

  @Test
  void startAlignUpdatedCampaignCounters() {
    // Given
    String taskQueue = TaskQueueConstants.TASK_QUEUE_SEND_MEDIUM_PRIORITY;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("AlignCountersUpdatedCampaignsWF-1", "RUNID");

    when(workflowServiceMock.buildWorkflowStubToStartNew(Mockito.eq(AlignCountersUpdatedCampaignsWF.class), Mockito.eq(taskQueue), Mockito.startsWith("AlignCountersUpdatedCampaignsWF-")))
      .thenReturn(alignCountersUpdatedCampaignsWF);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, null, null, null);

    // When
    WorkflowCreatedDTO result = client.startAlignUpdatedSendCampaignCounters();

    // Then
    assertEquals(expectedResult, result);
    verify(alignCountersUpdatedCampaignsWF).alignCountersForUpdatedCampaigns(Mockito.isNull(), Mockito.isNull(), Mockito.isNull());

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, AlignCountersUpdatedCampaignsWFImpl.class);
  }
}
