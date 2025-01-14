package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.wfingestion;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.activity.treasury.TreasuryOpiIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.treasury.TreasuryIufResult;
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

    when(treasuryOpiIngestionActivityMock.processFile(ingestionFlowId))
      .thenReturn(new TreasuryIufResult(List.of("iuf-1"), success, null,null));

    // When
    wf.ingest(ingestionFlowId);

    // Then
    verify(updateIngestionFlowStatusActivityMock).updateStatus(ingestionFlowId, "IMPORT_IN_ELAB", null);
    verify(sendEmailIngestionFlowActivityMock).sendEmail(ingestionFlowId, success);
    verify(updateIngestionFlowStatusActivityMock).updateStatus(ingestionFlowId, "OK", null);
  }

  @Test
  void givenFailingProcessingConditionWhenIngestThenKo(){
    // Given
    long ingestionFlowId = 1L;
    boolean success = false;

    when(treasuryOpiIngestionActivityMock.processFile(ingestionFlowId))
      .thenReturn(new TreasuryIufResult(Collections.emptyList(), success, "error", "discardedFileName"));

    // When
    wf.ingest(ingestionFlowId);

    // Then
    verify(updateIngestionFlowStatusActivityMock).updateStatus(ingestionFlowId, "IMPORT_IN_ELAB", null);
    verify(sendEmailIngestionFlowActivityMock).sendEmail(ingestionFlowId, success);
    verify(updateIngestionFlowStatusActivityMock).updateStatus(ingestionFlowId, "KO", "discardedFileName");
  }
}
