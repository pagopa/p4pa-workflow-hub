package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.wfingestion;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.activity.treasury.TreasuryOpiIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.treasury.TreasuryIufResult;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.activity.NotifyTreasuryToIufClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.config.TreasuryOpiIngestionWfConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

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
    boolean success = true;

    TreasuryIufResult treasuryIufResult = new TreasuryIufResult(
      Map.of("iuf-1", "iuf"), 1L, success, null, null);

    when(treasuryOpiIngestionActivityMock.processFile(ingestionFlowId))
      .thenReturn(treasuryIufResult);

    // TODO P4ADEV-1936 replace fake values with real ones
    // treasuryIufResult.getTreasuryId()
    // treasuryIufResult.getOrganizationId()

    String treasuryId = "123A";
    Long organizationId = 1L;

    // When
    wf.ingest(ingestionFlowId);

    // Then
    verify(updateIngestionFlowStatusActivityMock).updateStatus(ingestionFlowId, IngestionFlowFile.StatusEnum.PROCESSING, null, null);
    verify(sendEmailIngestionFlowActivityMock).sendEmail(ingestionFlowId, success);
    verify(updateIngestionFlowStatusActivityMock).updateStatus(ingestionFlowId, IngestionFlowFile.StatusEnum.COMPLETED, null, null);
    verify(notifyTreasuryToIufClassificationActivityMock).signalIufClassificationWithStart(organizationId, "iuf", treasuryId);
  }

  @Test
  void givenFailingProcessingConditionWhenIngestThenKo(){
    // Given
    long ingestionFlowFileId = 1L;
    boolean success = false;

    when(treasuryOpiIngestionActivityMock.processFile(ingestionFlowFileId))
      .thenReturn(new TreasuryIufResult(Map.of(), null, success, "error", "discardedFileName"));

    // When
    wf.ingest(ingestionFlowFileId);

    // Then
    verify(updateIngestionFlowStatusActivityMock).updateStatus(ingestionFlowFileId, IngestionFlowFile.StatusEnum.PROCESSING, null, null);
    verify(sendEmailIngestionFlowActivityMock).sendEmail(ingestionFlowFileId, success);
    verify(updateIngestionFlowStatusActivityMock).updateStatus(ingestionFlowFileId, IngestionFlowFile.StatusEnum.ERROR, "error", "discardedFileName");
  }
}
