package it.gov.pagopa.pu.workflow.wf.ingestionflow;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.IngestionFlowFileResult;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFileStatus;
import it.gov.pagopa.pu.workflow.wf.dataevents.activity.PublishDataEventsActivity;
import it.gov.pagopa.pu.workflow.wf.dataevents.config.DataEventsWFConfig;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.config.BaseIngestionFlowFileWFConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

public abstract class BaseIngestionFlowFileWFTest<R extends IngestionFlowFileResult> {

  @Mock
  private UpdateIngestionFlowStatusActivity updateIngestionFlowStatusActivityMock;
  @Mock
  private SendEmailIngestionFlowActivity sendEmailIngestionFlowActivityMock;
  @Mock
  private PublishDataEventsActivity publishDataEventsActivityMock;

  private Object ingestionFlowFileProcessorActivityMock;
  private Function<Long, R> activityInvoker;

  private BaseIngestionFlowFileWFImpl<R> wf;

  @BeforeEach
  void init() {
    BaseIngestionFlowFileWFConfig configMock = mock(BaseIngestionFlowFileWFConfig.class);
    DataEventsWFConfig dataEventsWFConfig = mock(DataEventsWFConfig.class);

    ApplicationContext applicationContextMock = mock(ApplicationContext.class);

    Mockito.doReturn(configMock)
      .when(applicationContextMock)
      .getBean(BaseIngestionFlowFileWFConfig.class);

    Mockito.doReturn(dataEventsWFConfig)
      .when(applicationContextMock)
      .getBean(DataEventsWFConfig.class);

    Mockito.when(configMock.buildUpdateIngestionFlowStatusActivityStub()).thenReturn(updateIngestionFlowStatusActivityMock);
    Mockito.when(configMock.buildSendEmailIngestionFlowActivityStub()).thenReturn(sendEmailIngestionFlowActivityMock);
    Mockito.when(dataEventsWFConfig.buildPublishDataEventActivityStub()).thenReturn(publishDataEventsActivityMock);

    Pair<Object, Function<Long, R>> mock2Invoker = configureIngestionFlowFileProcessorActivityMock(applicationContextMock);
    ingestionFlowFileProcessorActivityMock = mock2Invoker.getKey();
    activityInvoker = mock2Invoker.getValue();

    wf = buildWf();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      updateIngestionFlowStatusActivityMock,
      sendEmailIngestionFlowActivityMock,
      ingestionFlowFileProcessorActivityMock,
      publishDataEventsActivityMock
    );
  }

  /** It will return the activity processor mock and a function which will invoke it */
  protected abstract Pair<Object, Function<Long, R>> configureIngestionFlowFileProcessorActivityMock(ApplicationContext applicationContextMock);

  protected abstract BaseIngestionFlowFileWFImpl<R> buildWf();

  @Test
  void givenExceptionDuringFileHandlingWhenIngestThenSetErrorStatus() {
    // Given
    long ingestionFlowFileId = 0L;
    IngestionFlowFileResult expectedResult = IngestionFlowFileResult.builder()
      .errorDescription("DUMMY")
      .build();

    Mockito.when(activityInvoker.apply(ingestionFlowFileId))
      .thenThrow(new RuntimeException("DUMMY"));

    // When
    wf.ingest(ingestionFlowFileId);

    // Then
    Mockito.verify(updateIngestionFlowStatusActivityMock).updateIngestionFlowFileStatus(ingestionFlowFileId, IngestionFlowFileStatus.UPLOADED, IngestionFlowFileStatus.PROCESSING, null);
    Mockito.verify(updateIngestionFlowStatusActivityMock).updateIngestionFlowFileStatus(ingestionFlowFileId, IngestionFlowFileStatus.PROCESSING, IngestionFlowFileStatus.ERROR, expectedResult);
    Mockito.verify(sendEmailIngestionFlowActivityMock).sendIngestionFlowFileCompleteEmail(ingestionFlowFileId, false);
  }

  @Test
  void whenIngestThenOk() {
    // Given
    long ingestionFlowFileId = 10L;

    R expectedResult = buildExpectedIngestionFlowFileResult();

    Mockito.when(activityInvoker.apply(ingestionFlowFileId))
      .thenReturn(expectedResult);

    // When
    wf.ingest(ingestionFlowFileId);

    // Then
    Mockito.verify(updateIngestionFlowStatusActivityMock)
      .updateIngestionFlowFileStatus(Mockito.eq(ingestionFlowFileId), Mockito.eq(IngestionFlowFileStatus.UPLOADED), Mockito.eq(IngestionFlowFileStatus.PROCESSING), Mockito.isNull());
    Mockito.verify(updateIngestionFlowStatusActivityMock)
      .updateIngestionFlowFileStatus(Mockito.eq(ingestionFlowFileId), Mockito.eq(IngestionFlowFileStatus.PROCESSING), Mockito.eq(IngestionFlowFileStatus.COMPLETED), Mockito.same(expectedResult));
    Mockito.verify(sendEmailIngestionFlowActivityMock)
      .sendIngestionFlowFileCompleteEmail(ingestionFlowFileId, true);
    Mockito.verify(publishDataEventsActivityMock).publishIngestionFlowFileEventActivity(any(), any());
    verifyExtraMocks(ingestionFlowFileId, expectedResult);
  }

  protected abstract R buildExpectedIngestionFlowFileResult();

  /** To override in order to verify extra mocks */
  protected void verifyExtraMocks(long ingestionFlowFileId, R expectedResult){
    // Do Nothing
  }
}
