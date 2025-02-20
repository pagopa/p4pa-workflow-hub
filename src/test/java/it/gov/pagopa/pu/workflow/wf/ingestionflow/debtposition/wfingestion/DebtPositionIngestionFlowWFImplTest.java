package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.wfingestion;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.IngestionFlowFileProcessingLockerActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtposition.InstallmentIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.dto.debtposition.InstallmentIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.config.DebtPositionIngestionFlowWfConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class DebtPositionIngestionFlowWFImplTest {

  @Mock
  private IngestionFlowFileProcessingLockerActivity ingestionFlowFileProcessingLockerActivityMock;
  @Mock
  private InstallmentIngestionFlowFileActivity installmentIngestionFlowFileActivityMock;

  private DebtPositionIngestionFlowWFImpl wf;

  @BeforeEach
  void setUp() {
    DebtPositionIngestionFlowWfConfig debtPositionIngestionFlowWfConfigMock = Mockito.mock(DebtPositionIngestionFlowWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(debtPositionIngestionFlowWfConfigMock.buildIngestionFlowFileProcessingLockerActivityStub())
      .thenReturn(ingestionFlowFileProcessingLockerActivityMock);
    Mockito.when(debtPositionIngestionFlowWfConfigMock.buildInstallmentIngestionFlowFileActivityStub())
      .thenReturn(installmentIngestionFlowFileActivityMock);

    Mockito.when(applicationContextMock.getBean(DebtPositionIngestionFlowWfConfig.class))
      .thenReturn(debtPositionIngestionFlowWfConfigMock);

    wf = new DebtPositionIngestionFlowWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @Test
  void givenSuccessfulProcessingConditionWhenIngestThenOk() {
    // Given
    long ingestionFlowFileId = 1L;

    InstallmentIngestionFlowFileResult installmentIngestionFlowFileResult = new InstallmentIngestionFlowFileResult();
    installmentIngestionFlowFileResult.setProcessedRows(5L);
    installmentIngestionFlowFileResult.setTotalRows(10L);

    Mockito.when(ingestionFlowFileProcessingLockerActivityMock.acquireProcessingLock(ingestionFlowFileId)).thenReturn(Boolean.TRUE);
    Mockito.when(installmentIngestionFlowFileActivityMock.processFile(ingestionFlowFileId)).thenReturn(installmentIngestionFlowFileResult);

    // When
    wf.ingest(ingestionFlowFileId);

    // Then
    Mockito.verify(ingestionFlowFileProcessingLockerActivityMock).acquireProcessingLock(ingestionFlowFileId);
    Mockito.verify(installmentIngestionFlowFileActivityMock).processFile(ingestionFlowFileId);
  }

  @Test
  void givenFailingAcquireConditionWhenIngestThenKo() {
    // Given
    long ingestionFlowFileId = 1L;
    Mockito.when(ingestionFlowFileProcessingLockerActivityMock.acquireProcessingLock(ingestionFlowFileId)).thenReturn(Boolean.FALSE);

    // When
    wf.ingest(ingestionFlowFileId);

    // Then
    Mockito.verify(ingestionFlowFileProcessingLockerActivityMock).acquireProcessingLock(ingestionFlowFileId);
  }
}
