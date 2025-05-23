package it.gov.pagopa.pu.workflow.wf.ingestionflow;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.IngestionFlowFileProcessorActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.IngestionFlowFileResult;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFileStatus;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.config.BaseIngestionFlowFileWFConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import static org.mockito.Mockito.mock;

public abstract class BaseIngestionFlowFileWFTest<A extends IngestionFlowFileProcessorActivity<R>, R extends IngestionFlowFileResult> {

  @Mock
  private UpdateIngestionFlowStatusActivity updateIngestionFlowStatusActivityMock;
  @Mock
  private SendEmailIngestionFlowActivity sendEmailIngestionFlowActivityMock;
  private A ingestionFlowFileProcessorActivityMock;

  private BaseIngestionFlowFileWFImpl<R> wf;

  @BeforeEach
  void init() {
    BaseIngestionFlowFileWFConfig configMock = mock(BaseIngestionFlowFileWFConfig.class);
    ApplicationContext applicationContextMock = mock(ApplicationContext.class);

    Mockito.doReturn(configMock)
      .when(applicationContextMock)
      .getBean(BaseIngestionFlowFileWFConfig.class);

    Mockito.when(configMock.buildUpdateIngestionFlowStatusActivityStub()).thenReturn(updateIngestionFlowStatusActivityMock);
    Mockito.when(configMock.buildSendEmailIngestionFlowActivityStub()).thenReturn(sendEmailIngestionFlowActivityMock);

    ingestionFlowFileProcessorActivityMock = configureIngestionFlowFileProcessorActivityMock(applicationContextMock);

    wf = buildWf();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      updateIngestionFlowStatusActivityMock,
      sendEmailIngestionFlowActivityMock,
      ingestionFlowFileProcessorActivityMock
    );
  }

  protected abstract A configureIngestionFlowFileProcessorActivityMock(ApplicationContext applicationContextMock);

  protected abstract BaseIngestionFlowFileWFImpl<R> buildWf();

  @Test
  void givenExceptionDuringFileHandlingWhenIngestThenSetErrorStatus() {
    // Given
    long ingestionFlowFileId = 0L;
    IngestionFlowFileResult expectedResult = IngestionFlowFileResult.builder()
      .errorDescription("DUMMY")
      .build();

    Mockito.when(ingestionFlowFileProcessorActivityMock.processFile(ingestionFlowFileId))
      .thenThrow(new RuntimeException("DUMMY"));

    // When
    wf.ingest(ingestionFlowFileId);

    // Then
    Mockito.verify(updateIngestionFlowStatusActivityMock).updateStatus(ingestionFlowFileId, IngestionFlowFileStatus.UPLOADED, IngestionFlowFileStatus.PROCESSING, null);
    Mockito.verify(updateIngestionFlowStatusActivityMock).updateStatus(ingestionFlowFileId, IngestionFlowFileStatus.PROCESSING, IngestionFlowFileStatus.ERROR, expectedResult);
    Mockito.verify(sendEmailIngestionFlowActivityMock).sendEmail(ingestionFlowFileId, false);
  }

  @Test
  void whenIngestThenOk() {
    // Given
    long ingestionFlowFileId = 10L;

    R expectedResult = buildExpectedIngestionFlowFileResult();

    Mockito.when(ingestionFlowFileProcessorActivityMock.processFile(ingestionFlowFileId))
      .thenReturn(expectedResult);

    // When
    wf.ingest(ingestionFlowFileId);

    // Then
    Mockito.verify(updateIngestionFlowStatusActivityMock)
      .updateStatus(Mockito.eq(ingestionFlowFileId), Mockito.eq(IngestionFlowFileStatus.UPLOADED), Mockito.eq(IngestionFlowFileStatus.PROCESSING), Mockito.isNull());
    Mockito.verify(updateIngestionFlowStatusActivityMock)
      .updateStatus(Mockito.eq(ingestionFlowFileId), Mockito.eq(IngestionFlowFileStatus.PROCESSING), Mockito.eq(IngestionFlowFileStatus.COMPLETED), Mockito.same(expectedResult));
    Mockito.verify(sendEmailIngestionFlowActivityMock)
      .sendEmail(ingestionFlowFileId, true);

    verifyExtraMocks(ingestionFlowFileId, expectedResult);
  }

  protected abstract R buildExpectedIngestionFlowFileResult();

  /** To override in order to verify extra mocks */
  protected void verifyExtraMocks(long ingestionFlowFileId, R expectedResult){
    // Do Nothing
  }
}
