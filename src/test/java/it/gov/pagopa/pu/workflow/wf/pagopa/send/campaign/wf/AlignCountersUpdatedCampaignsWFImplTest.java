package it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.wf;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.campaign.AlignSendCampaignActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.campaign.FetchSendCampaignsLastFullRecalculationDateActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.campaign.FetchUpdatedSendCampaignsActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign.config.SendCampaignWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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
class AlignCountersUpdatedCampaignsWFImplTest {

  @Mock
  private FetchSendCampaignsLastFullRecalculationDateActivity fetchSendCampaignsLastFullRecalculationDateActivityMock;
  @Mock
  private FetchUpdatedSendCampaignsActivity fetchUpdatedSendCampaignsActivityMock;
  @Mock
  private AlignSendCampaignActivity alignSendCampaignActivityMock;

  private AlignCountersUpdatedCampaignsWFImpl wf;

  @BeforeEach
  void setUp() {
    SendCampaignWfConfig wfConfigMock = mock(SendCampaignWfConfig.class);
    ApplicationContext applicationContextMock = mock(ApplicationContext.class);

    when(wfConfigMock.buildFetchSendCampaignsLastFullRecalculationDateActivityStub()).thenReturn(fetchSendCampaignsLastFullRecalculationDateActivityMock);
    when(wfConfigMock.buildFetchUpdatedSendCampaignsActivityStub()).thenReturn(fetchUpdatedSendCampaignsActivityMock);
    when(wfConfigMock.buildAlignSendCampaignActivityStub()).thenReturn(alignSendCampaignActivityMock);

    when(applicationContextMock.getBean(SendCampaignWfConfig.class)).thenReturn(wfConfigMock);

    wf = new AlignCountersUpdatedCampaignsWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      fetchSendCampaignsLastFullRecalculationDateActivityMock,
      fetchUpdatedSendCampaignsActivityMock,
      alignSendCampaignActivityMock
    );
  }

  @Test
  void givenNoIdOfLatestAlignedCampaignWhenAlignCountersForUpdatedCampaignsThenAlign() {
    //GIVEN
    OffsetDateTime lastFullRecalculationDate = OffsetDateTime.now(it.gov.pagopa.payhub.activities.util.Utilities.ZONEID);
    when(fetchSendCampaignsLastFullRecalculationDateActivityMock.fetchSendCampaignsLastFullRecalculationDate())
      .thenReturn(lastFullRecalculationDate);
    when(fetchUpdatedSendCampaignsActivityMock.fetchIdsForUpdatedSendCampaigns(lastFullRecalculationDate))
      .thenReturn(List.of("0","1","2","3"));
    //WHEN
    wf.alignCountersForUpdatedCampaigns(null, null, null);
    //THEN
    verify(alignSendCampaignActivityMock).alignSendCampaign(Mockito.eq("0"), Mockito.any(OffsetDateTime.class));
    verify(alignSendCampaignActivityMock).alignSendCampaign(Mockito.eq("1"), Mockito.any(OffsetDateTime.class));
    verify(alignSendCampaignActivityMock).alignSendCampaign(Mockito.eq("2"), Mockito.any(OffsetDateTime.class));
    verify(alignSendCampaignActivityMock).alignSendCampaign(Mockito.eq("3"), Mockito.any(OffsetDateTime.class));
  }

  @Test
  void givenNoPreviousAlignmentWhenAlignCountersForUpdatedCampaignsThenThrowException() {
    //GIVEN
    when(fetchSendCampaignsLastFullRecalculationDateActivityMock.fetchSendCampaignsLastFullRecalculationDate())
      .thenReturn(null);
    //WHEN
    Assertions.assertThrows(
      IllegalStateException.class,
      () -> wf.alignCountersForUpdatedCampaigns(null, null, null)
    );
  }

  @Test
  void givenIdOfLatestAlignedCampaignWhenAlignCountersForUpdatedCampaignsThenAlignRemaining() {
    //GIVEN
    OffsetDateTime lastFullRecalculationDate = OffsetDateTime.now(it.gov.pagopa.payhub.activities.util.Utilities.ZONEID);
    OffsetDateTime newFullRecalculationDate = OffsetDateTime.now(it.gov.pagopa.payhub.activities.util.Utilities.ZONEID);
    when(fetchUpdatedSendCampaignsActivityMock.fetchIdsForUpdatedSendCampaigns(lastFullRecalculationDate))
      .thenReturn(List.of("0","1","2","3"));
    //WHEN
    wf.alignCountersForUpdatedCampaigns(lastFullRecalculationDate, newFullRecalculationDate, "1");
    //THEN
    verify(fetchSendCampaignsLastFullRecalculationDateActivityMock, Mockito.times(0)).fetchSendCampaignsLastFullRecalculationDate();
    verify(alignSendCampaignActivityMock, Mockito.times(0)).alignSendCampaign("0", newFullRecalculationDate);
    verify(alignSendCampaignActivityMock, Mockito.times(0)).alignSendCampaign("1", newFullRecalculationDate);
    verify(alignSendCampaignActivityMock).alignSendCampaign("2", newFullRecalculationDate);
    verify(alignSendCampaignActivityMock).alignSendCampaign("3", newFullRecalculationDate);
  }

  @Test
  void givenMoreThenThresholdWhenAlignCountersForUpdatedCampaignsThenContinueAsNew() {
    //GIVEN
    List<String> campaignIdList =
      IntStream.rangeClosed(1, THRESHOLD_TEMPORAL_EVENTS_BEFORE_CONTINUE_AS_NEW + 1)
        .mapToObj(String::valueOf)
        .toList();
    OffsetDateTime lastFullRecalculationDate = OffsetDateTime.now(it.gov.pagopa.payhub.activities.util.Utilities.ZONEID);
    when(fetchSendCampaignsLastFullRecalculationDateActivityMock.fetchSendCampaignsLastFullRecalculationDate())
      .thenReturn(lastFullRecalculationDate);
    when(fetchUpdatedSendCampaignsActivityMock.fetchIdsForUpdatedSendCampaigns(lastFullRecalculationDate))
      .thenReturn(campaignIdList);
    Mockito.doNothing()
      .when(alignSendCampaignActivityMock).alignSendCampaign(Mockito.argThat(campaignIdList::contains), Mockito.any(OffsetDateTime.class));

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      //WHEN
      wf.alignCountersForUpdatedCampaigns(null, null, null);
      //THEN
      workflowMock.verify(
        () -> Workflow.continueAsNew(
          Mockito.isA(OffsetDateTime.class),
          Mockito.isA(OffsetDateTime.class),
          Mockito.eq(String.valueOf(THRESHOLD_TEMPORAL_EVENTS_BEFORE_CONTINUE_AS_NEW))
        )
      );
    }
  }

  @Test
  void givenErrorInAlignCampaignsWhenAlignCountersForUpdatedCampaignsThenSkipErrors() {
    //GIVEN
    OffsetDateTime lastFullRecalculationDate = OffsetDateTime.now(it.gov.pagopa.payhub.activities.util.Utilities.ZONEID);
    when(fetchSendCampaignsLastFullRecalculationDateActivityMock.fetchSendCampaignsLastFullRecalculationDate())
      .thenReturn(lastFullRecalculationDate);
    when(fetchUpdatedSendCampaignsActivityMock.fetchIdsForUpdatedSendCampaigns(lastFullRecalculationDate))
      .thenReturn(List.of("0","1","2","3"));
    doThrow(new RuntimeException("error"))
      .when(alignSendCampaignActivityMock)
      .alignSendCampaign(Mockito.eq("1"), Mockito.isA(OffsetDateTime.class));
    doThrow(new RuntimeException("error"))
      .when(alignSendCampaignActivityMock)
      .alignSendCampaign(Mockito.eq("2"), Mockito.isA(OffsetDateTime.class));
    //WHEN
    wf.alignCountersForUpdatedCampaigns(null, null, null);
    //THEN
    verify(alignSendCampaignActivityMock).alignSendCampaign(Mockito.eq("0"), Mockito.isA(OffsetDateTime.class));
    verify(alignSendCampaignActivityMock).alignSendCampaign(Mockito.eq("1"), Mockito.isA(OffsetDateTime.class));
    verify(alignSendCampaignActivityMock).alignSendCampaign(Mockito.eq("2"), Mockito.isA(OffsetDateTime.class));
    verify(alignSendCampaignActivityMock).alignSendCampaign(Mockito.eq("3"), Mockito.isA(OffsetDateTime.class));
  }
}
