package it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.wf;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.campaign.AlignSendCampaignActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.campaign.FetchSendCampaignsActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.config.SendCampaignWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static it.gov.pagopa.pu.workflow.utilities.Constants.THRESHOLD_TEMPORAL_EVENTS_BEFORE_CONTINUE_AS_NEW;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlignCountersAllCampaignsWFImplTest {

  @Mock
  private FetchSendCampaignsActivity fetchSendCampaignsActivityMock;
  @Mock
  private AlignSendCampaignActivity alignSendCampaignActivityMock;

  private AlignCountersAllCampaignsWFImpl wf;

  @BeforeEach
  void setUp() {
    SendCampaignWfConfig wfConfigMock = mock(SendCampaignWfConfig.class);
    ApplicationContext applicationContextMock = mock(ApplicationContext.class);

    when(wfConfigMock.buildFetchSendCampaignsActivityStub()).thenReturn(fetchSendCampaignsActivityMock);
    when(wfConfigMock.buildAlignSendCampaignActivityStub()).thenReturn(alignSendCampaignActivityMock);

    when(applicationContextMock.getBean(SendCampaignWfConfig.class)).thenReturn(wfConfigMock);

    wf = new AlignCountersAllCampaignsWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      fetchSendCampaignsActivityMock,
      alignSendCampaignActivityMock
    );
  }

  @Test
  void givenNoIdOfLatestAlignedCampaignWhenAlignCountersForAllActiveCampaignsThenAlign() {
    //GIVEN
    when(fetchSendCampaignsActivityMock.fetchSendCampaignIds())
      .thenReturn(List.of("0","1","2","3"));
    //WHEN
    wf.alignCountersForAllActiveCampaigns(null);
    //THEN
    verify(alignSendCampaignActivityMock).alignSendCampaign(Mockito.eq("0"), Mockito.any(OffsetDateTime.class));
    verify(alignSendCampaignActivityMock).alignSendCampaign(Mockito.eq("1"), Mockito.any(OffsetDateTime.class));
    verify(alignSendCampaignActivityMock).alignSendCampaign(Mockito.eq("2"), Mockito.any(OffsetDateTime.class));
    verify(alignSendCampaignActivityMock).alignSendCampaign(Mockito.eq("3"), Mockito.any(OffsetDateTime.class));
  }

  @Test
  void givenIdOfLatestAlignedCampaignWhenAlignCountersForAllActiveCampaignsThenAlignRemaining() {
    //GIVEN
    when(fetchSendCampaignsActivityMock.fetchSendCampaignIds())
      .thenReturn(List.of("0","1","2","3"));
    //WHEN
    wf.alignCountersForAllActiveCampaigns("1");
    //THEN
    verify(alignSendCampaignActivityMock).alignSendCampaign(Mockito.eq("2"), Mockito.any(OffsetDateTime.class));
    verify(alignSendCampaignActivityMock).alignSendCampaign(Mockito.eq("3"), Mockito.any(OffsetDateTime.class));
  }

  @Test
  void givenMoreThenThresholdWhenAlignCountersForActiveCampaignsThenContinueAsNew() {
    //GIVEN
    List<String> campaignIdList =
      IntStream.rangeClosed(1, THRESHOLD_TEMPORAL_EVENTS_BEFORE_CONTINUE_AS_NEW + 1)
        .mapToObj(String::valueOf)
        .toList();
    when(fetchSendCampaignsActivityMock.fetchSendCampaignIds())
      .thenReturn(campaignIdList);
    Mockito.doNothing()
      .when(alignSendCampaignActivityMock).alignSendCampaign(Mockito.argThat(campaignIdList::contains), Mockito.any(OffsetDateTime.class));

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      //WHEN
      wf.alignCountersForAllActiveCampaigns(null);
      //THEN
      workflowMock.verify(() -> Workflow.continueAsNew(String.valueOf(THRESHOLD_TEMPORAL_EVENTS_BEFORE_CONTINUE_AS_NEW)));
    }
  }

  @Test
  void givenErrorInAlignCampaignsWhenAlignCountersForActiveCampaignsThenSkipErrors() {
    //GIVEN
    when(fetchSendCampaignsActivityMock.fetchSendCampaignIds())
      .thenReturn(List.of("0","1","2","3"));
    doThrow(new RuntimeException("error"))
      .when(alignSendCampaignActivityMock)
      .alignSendCampaign(Mockito.eq("1"), Mockito.any(OffsetDateTime.class));
    doThrow(new RuntimeException("error"))
      .when(alignSendCampaignActivityMock)
      .alignSendCampaign(Mockito.eq("2"), Mockito.any(OffsetDateTime.class));
    //WHEN
    wf.alignCountersForAllActiveCampaigns(null);
    //THEN
    verify(alignSendCampaignActivityMock).alignSendCampaign(Mockito.eq("0"), Mockito.any(OffsetDateTime.class));
    verify(alignSendCampaignActivityMock).alignSendCampaign(Mockito.eq("1"), Mockito.any(OffsetDateTime.class));
    verify(alignSendCampaignActivityMock).alignSendCampaign(Mockito.eq("2"), Mockito.any(OffsetDateTime.class));
    verify(alignSendCampaignActivityMock).alignSendCampaign(Mockito.eq("3"), Mockito.any(OffsetDateTime.class));
  }
}
