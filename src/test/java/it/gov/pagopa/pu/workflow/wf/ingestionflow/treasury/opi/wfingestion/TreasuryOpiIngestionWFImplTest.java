package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.wfingestion;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.treasury.TreasuryOpiIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.treasury.TreasuryIufIngestionFlowFileResult;
import it.gov.pagopa.payhub.activities.exception.NotRetryableActivityException;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFileStatus;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.activity.NotifyTreasuryToIufClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.config.TreasuryOpiIngestionWfConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TreasuryOpiIngestionWFImplTest {
  @Mock
  private UpdateIngestionFlowStatusActivity updateIngestionFlowStatusActivityMock;
  @Mock
  private SendEmailIngestionFlowActivity sendEmailIngestionFlowActivityMock;
  @Mock
  private TreasuryOpiIngestionActivity treasuryOpiIngestionActivityMock;
  @Mock
  private NotifyTreasuryToIufClassificationActivity notifyTreasuryToIufClassificationActivityMock;

  private TreasuryOpiIngestionWFImpl wf;

  @BeforeEach
  void setUp() {
    TreasuryOpiIngestionWfConfig treasuryOpiIngestionWfConfigMock = mock(TreasuryOpiIngestionWfConfig.class);
    ApplicationContext applicationContextMock = mock(ApplicationContext.class);

    when(treasuryOpiIngestionWfConfigMock.buildUpdateIngestionFlowStatusActivityStub())
      .thenReturn(updateIngestionFlowStatusActivityMock);
    when(treasuryOpiIngestionWfConfigMock.buildTreasuryOpiIngestionActivityStub())
      .thenReturn(treasuryOpiIngestionActivityMock);
    when(treasuryOpiIngestionWfConfigMock.buildSendEmailIngestionFlowActivityStub())
      .thenReturn(sendEmailIngestionFlowActivityMock);
    when(treasuryOpiIngestionWfConfigMock.buildNotifyTreasuryToIufClassificationActivityStub())
      .thenReturn(notifyTreasuryToIufClassificationActivityMock);

    when(applicationContextMock.getBean(TreasuryOpiIngestionWfConfig.class))
      .thenReturn(treasuryOpiIngestionWfConfigMock);

    wf = new TreasuryOpiIngestionWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @Test
  void givenSuccessfulProcessingConditionWhenIngestThenOk(){
    // Given
    long ingestionFlowId = 1L;
    Long organizationId = 1L;
    String treasuryId = "treasuryid-1";
    String iuf = "iuf-1";

    Map<String, String> iufTreasuryIdMap = Map.of(iuf, treasuryId);
    TreasuryIufIngestionFlowFileResult treasuryIufResult = TreasuryIufIngestionFlowFileResult.builder()
      .iuf2TreasuryIdMap(iufTreasuryIdMap)
      .organizationId(organizationId)
      .processedRows(10L)
      .totalRows(100L)
      .build();

    when(treasuryOpiIngestionActivityMock.processFile(ingestionFlowId))
      .thenReturn(treasuryIufResult);

    // When
    wf.ingest(ingestionFlowId);

    // Then
    verify(updateIngestionFlowStatusActivityMock)
      .updateStatus(ingestionFlowId, IngestionFlowFileStatus.UPLOADED, IngestionFlowFileStatus.PROCESSING, null);
    verify(sendEmailIngestionFlowActivityMock)
      .sendEmail(ingestionFlowId, true);
    verify(updateIngestionFlowStatusActivityMock)
      .updateStatus(ingestionFlowId, IngestionFlowFileStatus.PROCESSING, IngestionFlowFileStatus.COMPLETED, treasuryIufResult);

    verify(notifyTreasuryToIufClassificationActivityMock).signalIufClassificationWithStart(organizationId, iuf, treasuryId);
  }

  @Test
  void givenFailingProcessingConditionWhenIngestThenKo(){
    // Given
    long ingestionFlowFileId = 1L;

    TreasuryIufIngestionFlowFileResult result = TreasuryIufIngestionFlowFileResult.builder()
      .iuf2TreasuryIdMap(Map.of())
      .errorDescription("error")
      .discardedFileName("discardedFileName")
      .build();

    when(treasuryOpiIngestionActivityMock.processFile(ingestionFlowFileId))
      .thenReturn(result);

    // When
    wf.ingest(ingestionFlowFileId);

    // Then
    verify(updateIngestionFlowStatusActivityMock)
      .updateStatus(ingestionFlowFileId, IngestionFlowFileStatus.UPLOADED, IngestionFlowFileStatus.PROCESSING, null);
    verify(sendEmailIngestionFlowActivityMock)
      .sendEmail(ingestionFlowFileId, false);
    verify(updateIngestionFlowStatusActivityMock)
      .updateStatus(ingestionFlowFileId, IngestionFlowFileStatus.PROCESSING, IngestionFlowFileStatus.ERROR, result);
  }

  @Test
  void givenUnexpectedExceptionWhenIngestThenKo(){
    // Given
    long ingestionFlowFileId = 1L;

    when(treasuryOpiIngestionActivityMock.processFile(ingestionFlowFileId))
      .thenThrow(new NotRetryableActivityException("DUMMY"));

    TreasuryIufIngestionFlowFileResult ingestionFlowFileResult = TreasuryIufIngestionFlowFileResult.builder()
      .iuf2TreasuryIdMap(Collections.emptyMap())
      .errorDescription("Unexpected error when processing TreasuryOPI file: DUMMY")
      .build();

    // When
    wf.ingest(ingestionFlowFileId);

    // Then
    verify(updateIngestionFlowStatusActivityMock)
      .updateStatus(ingestionFlowFileId, IngestionFlowFileStatus.UPLOADED, IngestionFlowFileStatus.PROCESSING, null);
    verify(sendEmailIngestionFlowActivityMock)
      .sendEmail(ingestionFlowFileId, false);
    verify(updateIngestionFlowStatusActivityMock)
      .updateStatus(ingestionFlowFileId, IngestionFlowFileStatus.PROCESSING, IngestionFlowFileStatus.ERROR, ingestionFlowFileResult);
  }
}
