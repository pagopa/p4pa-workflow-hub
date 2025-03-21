package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.wfingestion;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.IngestionFlowFileProcessingLockerActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtposition.InstallmentIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtposition.SynchronizeIngestedDebtPositionActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.debtposition.InstallmentIngestionFlowFileResult;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFileStatus;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.config.DebtPositionIngestionFlowWfConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.time.Duration;

@ExtendWith(MockitoExtension.class)
class DebtPositionIngestionFlowWFImplTest {

  @Mock
  private IngestionFlowFileProcessingLockerActivity ingestionFlowFileProcessingLockerActivityMock;
  @Mock
  private InstallmentIngestionFlowFileActivity installmentIngestionFlowFileActivityMock;
  @Mock
  private UpdateIngestionFlowStatusActivity updateIngestionFlowStatusActivityMock;
  @Mock
  private SendEmailIngestionFlowActivity sendEmailIngestionFlowActivityMock;
  @Mock
  private SynchronizeIngestedDebtPositionActivity synchronizeIngestedDebtPositionActivityMock;

  private DebtPositionIngestionFlowWFImpl wf;

  @BeforeEach
  void setUp() {
    DebtPositionIngestionFlowWfConfig debtPositionIngestionFlowWfConfigMock = Mockito.mock(DebtPositionIngestionFlowWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(debtPositionIngestionFlowWfConfigMock.buildIngestionFlowFileProcessingLockerActivityStub())
      .thenReturn(ingestionFlowFileProcessingLockerActivityMock);
    Mockito.when(debtPositionIngestionFlowWfConfigMock.buildInstallmentIngestionFlowFileActivityStub())
      .thenReturn(installmentIngestionFlowFileActivityMock);
    Mockito.when(debtPositionIngestionFlowWfConfigMock.buildUpdateIngestionFlowStatusActivityStub())
      .thenReturn(updateIngestionFlowStatusActivityMock);
    Mockito.when(debtPositionIngestionFlowWfConfigMock.buildSendEmailIngestionFlowActivityStub())
      .thenReturn(sendEmailIngestionFlowActivityMock);
    Mockito.when(debtPositionIngestionFlowWfConfigMock.buildSynchronizeIngestedDebtPositionActivityStub())
      .thenReturn(synchronizeIngestedDebtPositionActivityMock);

    Mockito.when(applicationContextMock.getBean(DebtPositionIngestionFlowWfConfig.class))
      .thenReturn(debtPositionIngestionFlowWfConfigMock);

    wf = new DebtPositionIngestionFlowWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @Test
  void givenSuccessfulProcessingConditionWhenIngestThenOk() {
    long ingestionFlowFileId = 1L;

    InstallmentIngestionFlowFileResult installmentIngestionFlowFileResult = new InstallmentIngestionFlowFileResult();
    installmentIngestionFlowFileResult.setProcessedRows(5L);
    installmentIngestionFlowFileResult.setTotalRows(10L);

    Mockito.when(ingestionFlowFileProcessingLockerActivityMock.acquireProcessingLock(ingestionFlowFileId)).thenReturn(true);
    Mockito.when(installmentIngestionFlowFileActivityMock.processFile(ingestionFlowFileId)).thenReturn(installmentIngestionFlowFileResult);

    Mockito.when(synchronizeIngestedDebtPositionActivityMock.synchronizeIngestedDebtPosition(ingestionFlowFileId))
      .thenReturn("");

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);

      wf.ingest(ingestionFlowFileId);

      Mockito.verify(ingestionFlowFileProcessingLockerActivityMock).acquireProcessingLock(ingestionFlowFileId);
      Mockito.verify(installmentIngestionFlowFileActivityMock).processFile(ingestionFlowFileId);
      Mockito.verify(updateIngestionFlowStatusActivityMock).updateStatus(
        Mockito.eq(ingestionFlowFileId),
        Mockito.eq(IngestionFlowFileStatus.PROCESSING),
        Mockito.eq(IngestionFlowFileStatus.COMPLETED),
        Mockito.isNull(),
        Mockito.isNull()
      );
      Mockito.verify(sendEmailIngestionFlowActivityMock).sendEmail(ingestionFlowFileId, true);
    }
  }

  @Test
  void givenErrorsOnSynchronizeIngestedDebtPositionWhenIngestThenKo() {
    long ingestionFlowFileId = 1L;

    InstallmentIngestionFlowFileResult installmentIngestionFlowFileResult = new InstallmentIngestionFlowFileResult();
    installmentIngestionFlowFileResult.setProcessedRows(5L);
    installmentIngestionFlowFileResult.setTotalRows(10L);

    Mockito.when(ingestionFlowFileProcessingLockerActivityMock.acquireProcessingLock(ingestionFlowFileId)).thenReturn(true);
    Mockito.when(installmentIngestionFlowFileActivityMock.processFile(ingestionFlowFileId)).thenReturn(installmentIngestionFlowFileResult);

    Mockito.when(synchronizeIngestedDebtPositionActivityMock.synchronizeIngestedDebtPosition(ingestionFlowFileId))
      .thenReturn("\nError on synchronizeIngestedDebtPositionActivity");

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);

      wf.ingest(ingestionFlowFileId);

      String errorDescription = """
        There were errors during the synchronization of the ingested Debt Position:
        Error on synchronizeIngestedDebtPositionActivity
        """.stripTrailing();

      Mockito.verify(ingestionFlowFileProcessingLockerActivityMock).acquireProcessingLock(ingestionFlowFileId);
      Mockito.verify(installmentIngestionFlowFileActivityMock).processFile(ingestionFlowFileId);
      Mockito.verify(updateIngestionFlowStatusActivityMock).updateStatus(
        ingestionFlowFileId,
        IngestionFlowFileStatus.PROCESSING,
        IngestionFlowFileStatus.ERROR,
        errorDescription,
        null
      );
      Mockito.verify(sendEmailIngestionFlowActivityMock).sendEmail(ingestionFlowFileId, false);
    }
  }

  @Test
  void givenFailingProcessingConditionWhenIngestThenKo() {
    long ingestionFlowFileId = 1L;

    InstallmentIngestionFlowFileResult installmentIngestionFlowFileResult = new InstallmentIngestionFlowFileResult();
    installmentIngestionFlowFileResult.setProcessedRows(5L);
    installmentIngestionFlowFileResult.setTotalRows(10L);

    Mockito.when(ingestionFlowFileProcessingLockerActivityMock.acquireProcessingLock(ingestionFlowFileId)).thenReturn(true);
    Mockito.when(installmentIngestionFlowFileActivityMock.processFile(ingestionFlowFileId)).thenThrow(new RuntimeException("DUMMY"));

    Mockito.when(synchronizeIngestedDebtPositionActivityMock.synchronizeIngestedDebtPosition(ingestionFlowFileId))
      .thenReturn("\nError on synchronizeIngestedDebtPositionActivity");

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);

      wf.ingest(ingestionFlowFileId);

      String errorDescription = """
        Unexpected error when processing DebtPositionIngestion file: DUMMY

        There were errors during the synchronization of the ingested Debt Position:
        Error on synchronizeIngestedDebtPositionActivity
        """.stripTrailing();

      Mockito.verify(ingestionFlowFileProcessingLockerActivityMock).acquireProcessingLock(ingestionFlowFileId);
      Mockito.verify(installmentIngestionFlowFileActivityMock).processFile(ingestionFlowFileId);
      Mockito.verify(updateIngestionFlowStatusActivityMock).updateStatus(
        ingestionFlowFileId,
        IngestionFlowFileStatus.PROCESSING,
        IngestionFlowFileStatus.ERROR,
        errorDescription,
        null
      );
      Mockito.verify(sendEmailIngestionFlowActivityMock).sendEmail(ingestionFlowFileId, false);
    }
  }

  @Test
  void testIngestWithLockRetries() {
    Long ingestionFlowFileId = 1L;

    InstallmentIngestionFlowFileResult installmentIngestionFlowFileResult = new InstallmentIngestionFlowFileResult();
    installmentIngestionFlowFileResult.setProcessedRows(5L);
    installmentIngestionFlowFileResult.setTotalRows(10L);

    Mockito.when(ingestionFlowFileProcessingLockerActivityMock.acquireProcessingLock(ingestionFlowFileId))
      .thenReturn(false)
      .thenReturn(false)
      .thenReturn(true);
    Mockito.when(installmentIngestionFlowFileActivityMock.processFile(ingestionFlowFileId)).thenReturn(installmentIngestionFlowFileResult);

    Mockito.when(synchronizeIngestedDebtPositionActivityMock.synchronizeIngestedDebtPosition(ingestionFlowFileId))
      .thenReturn("");

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);

      wf.ingest(ingestionFlowFileId);

      Mockito.verify(ingestionFlowFileProcessingLockerActivityMock, Mockito.times(3)).acquireProcessingLock(ingestionFlowFileId);
      Mockito.verify(installmentIngestionFlowFileActivityMock).processFile(ingestionFlowFileId);
      Mockito.verify(updateIngestionFlowStatusActivityMock).updateStatus(
        Mockito.eq(ingestionFlowFileId),
        Mockito.eq(IngestionFlowFileStatus.PROCESSING),
        Mockito.eq(IngestionFlowFileStatus.COMPLETED),
        Mockito.isNull(),
        Mockito.isNull()
      );
      Mockito.verify(sendEmailIngestionFlowActivityMock).sendEmail(ingestionFlowFileId, true);
    }
  }
}
