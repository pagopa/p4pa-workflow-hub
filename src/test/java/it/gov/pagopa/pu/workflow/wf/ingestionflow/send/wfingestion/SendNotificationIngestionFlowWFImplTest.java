package it.gov.pagopa.pu.workflow.wf.ingestionflow.send.wfingestion;


import it.gov.pagopa.payhub.activities.activity.ingestionflow.sendnotification.SendNotificationIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.sendnotification.SendNotificationIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFTest;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.send.config.SendNotificationIngestionWFConfig;
import java.util.function.Function;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class SendNotificationIngestionFlowWFImplTest extends
  BaseIngestionFlowFileWFTest<SendNotificationIngestionFlowFileResult> {

  @Mock
  private SendNotificationIngestionActivity sendNotificationIngestionActivityMock;

  private SendNotificationIngestionFlowWFImpl wf;

  @Override
  protected Pair<Object, Function<Long, SendNotificationIngestionFlowFileResult>> configureIngestionFlowFileProcessorActivityMock(
    ApplicationContext applicationContextMock) {
    SendNotificationIngestionWFConfig sendNotificationIngestionWFConfigMock = Mockito.mock(SendNotificationIngestionWFConfig.class);

    Mockito.doReturn(sendNotificationIngestionWFConfigMock).when(applicationContextMock)
      .getBean(SendNotificationIngestionWFConfig.class);

    Mockito.when(sendNotificationIngestionWFConfigMock.buildSendNotificationIngestionActivityStub())
      .thenReturn(sendNotificationIngestionActivityMock);

    return Pair.of(sendNotificationIngestionActivityMock, sendNotificationIngestionActivityMock::processFile);
  }

  @Override
  protected BaseIngestionFlowFileWFImpl<SendNotificationIngestionFlowFileResult> buildWf() {
    return new SendNotificationIngestionFlowWFImpl();
  }

  @Override
  protected SendNotificationIngestionFlowFileResult buildExpectedIngestionFlowFileResult() {
    return SendNotificationIngestionFlowFileResult.builder().organizationId(1L).build();
  }

}
