package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.wfingestion;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.activity.treasury.TreasuryOpiIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.treasury.TreasuryIufResult;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.activity.StartIufClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.config.TreasuryOpiIngestionWfConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.List;

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
  private StartIufClassificationActivity startIufClassificationActivityMock;

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
    when(treasuryOpiIngestionWfConfigMock.buildStartIufClassificationActivityStub())
      .thenReturn(startIufClassificationActivityMock);

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
      List.of("iuf-1"), success, null, null);

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
    verify(updateIngestionFlowStatusActivityMock).updateStatus(ingestionFlowId, "IMPORT_IN_ELAB", null);
    verify(sendEmailIngestionFlowActivityMock).sendEmail(ingestionFlowId, success);
    verify(updateIngestionFlowStatusActivityMock).updateStatus(ingestionFlowId, "OK", null);
    verify(startIufClassificationActivityMock).signalIufClassificationWithStart(organizationId, treasuryIufResult.getIufs().getFirst(), treasuryId);
  }

  @Test
  void givenFailingProcessingConditionWhenIngestThenKo(){
    // Given
    long ingestionFlowFileId = 1L;
    boolean success = false;

    when(treasuryOpiIngestionActivityMock.processFile(ingestionFlowFileId))
      .thenReturn(new TreasuryIufResult(Collections.emptyList(), success, "error", "discardedFileName"));

    // When
    wf.ingest(ingestionFlowFileId);

    // Then
    verify(updateIngestionFlowStatusActivityMock).updateStatus(ingestionFlowFileId, "IMPORT_IN_ELAB", null);
    verify(sendEmailIngestionFlowActivityMock).sendEmail(ingestionFlowFileId, success);
    verify(updateIngestionFlowStatusActivityMock).updateStatus(ingestionFlowFileId, "KO", "discardedFileName");
  }
}
