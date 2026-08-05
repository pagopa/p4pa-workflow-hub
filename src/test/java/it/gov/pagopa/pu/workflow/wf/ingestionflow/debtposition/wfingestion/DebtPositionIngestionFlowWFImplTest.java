package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.wfingestion;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.IngestionFlowFileProcessingLockerActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtposition.InstallmentIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtposition.SynchronizeIngestedDebtPositionActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.IngestionFlowFileResult;
import it.gov.pagopa.payhub.activities.dto.ingestion.debtposition.InstallmentIngestionFlowFileResult;
import it.gov.pagopa.payhub.activities.dto.ingestion.debtposition.SyncIngestedDebtPositionDTO;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFileStatus;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.activity.NotifyIngestionActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.config.BaseIngestionFlowFileWFConfig;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.activity.StartMassiveNoticesGenerationWFActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.config.DebtPositionIngestionFlowWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
  @Mock
  private StartMassiveNoticesGenerationWFActivity startMassiveNoticesGenerationWFActivityMock;
  @Mock
  private NotifyIngestionActivity notifyIngestionActivityMock;

  private DebtPositionIngestionFlowWFImpl wf;

  @BeforeEach
  void setUp() {
    BaseIngestionFlowFileWFConfig baseIngestionFlowFileWFConfigMock = mock(BaseIngestionFlowFileWFConfig.class);
    DebtPositionIngestionFlowWfConfig debtPositionIngestionFlowWfConfigMock = mock(DebtPositionIngestionFlowWfConfig.class);
    ApplicationContext applicationContextMock = mock(ApplicationContext.class);

    doReturn(debtPositionIngestionFlowWfConfigMock)
      .when(applicationContextMock)
      .getBean(DebtPositionIngestionFlowWfConfig.class);

    doReturn(baseIngestionFlowFileWFConfigMock)
      .when(applicationContextMock)
      .getBean(BaseIngestionFlowFileWFConfig.class);

    when(baseIngestionFlowFileWFConfigMock.buildUpdateIngestionFlowStatusActivityStub())
      .thenReturn(updateIngestionFlowStatusActivityMock);
    when(baseIngestionFlowFileWFConfigMock.buildSendEmailIngestionFlowActivityStub())
      .thenReturn(sendEmailIngestionFlowActivityMock);

    when(debtPositionIngestionFlowWfConfigMock.buildIngestionFlowFileProcessingLockerActivityStub())
      .thenReturn(ingestionFlowFileProcessingLockerActivityMock);
    when(debtPositionIngestionFlowWfConfigMock.buildInstallmentIngestionFlowFileActivityStub())
      .thenReturn(installmentIngestionFlowFileActivityMock);
    when(debtPositionIngestionFlowWfConfigMock.buildSynchronizeIngestedDebtPositionActivityStub())
      .thenReturn(synchronizeIngestedDebtPositionActivityMock);
    when(debtPositionIngestionFlowWfConfigMock.buildStartMassiveNoticesGenerationWFActivityStub())
      .thenReturn(startMassiveNoticesGenerationWFActivityMock);
    when(baseIngestionFlowFileWFConfigMock.buildNotifyIngestionActivityStub())
      .thenReturn(notifyIngestionActivityMock);

    wf = new DebtPositionIngestionFlowWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      ingestionFlowFileProcessingLockerActivityMock,
      installmentIngestionFlowFileActivityMock,
      updateIngestionFlowStatusActivityMock,
      sendEmailIngestionFlowActivityMock,
      synchronizeIngestedDebtPositionActivityMock,
      startMassiveNoticesGenerationWFActivityMock,
      notifyIngestionActivityMock
    );
  }

  @Test
  void givenSuccessfulProcessingConditionWhenIngestThenOk() {
    long ingestionFlowFileId = 1L;
    String pdfGeneratedId = "generatedId";

    InstallmentIngestionFlowFileResult installmentIngestionFlowFileResult = new InstallmentIngestionFlowFileResult();
    installmentIngestionFlowFileResult.setProcessedRows(5L);
    installmentIngestionFlowFileResult.setTotalRows(10L);
    installmentIngestionFlowFileResult.setOrganizationId(123L);

    when(ingestionFlowFileProcessingLockerActivityMock.acquireIngestionFlowFileProcessingLock(ingestionFlowFileId)).thenReturn(true);
    when(installmentIngestionFlowFileActivityMock.processFile(ingestionFlowFileId)).thenReturn(installmentIngestionFlowFileResult);

    when(synchronizeIngestedDebtPositionActivityMock.synchronizeIngestedDebtPosition(ingestionFlowFileId))
      .thenReturn(new SyncIngestedDebtPositionDTO("", pdfGeneratedId, null));

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);

      wf.ingest(ingestionFlowFileId);

      verify(ingestionFlowFileProcessingLockerActivityMock).acquireIngestionFlowFileProcessingLock(ingestionFlowFileId);
      verify(installmentIngestionFlowFileActivityMock).processFile(ingestionFlowFileId);
      verify(startMassiveNoticesGenerationWFActivityMock).startMassiveNoticesGenerationWF(ingestionFlowFileId);
      verify(updateIngestionFlowStatusActivityMock).updateIngestionFlowFileStatus(
        Mockito.eq(ingestionFlowFileId),
        Mockito.eq(IngestionFlowFileStatus.PROCESSING),
        Mockito.eq(IngestionFlowFileStatus.COMPLETED),
        Mockito.same(installmentIngestionFlowFileResult)
      );
      verify(sendEmailIngestionFlowActivityMock).sendIngestionFlowFileCompleteEmail(ingestionFlowFileId, true);
      verify(notifyIngestionActivityMock).notifyIngestionEvent(any(), any());
    }
  }

  @Test
  void givenErrorsOnSynchronizeIngestedDebtPositionWhenIngestThenKo() {
    long ingestionFlowFileId = 1L;

    InstallmentIngestionFlowFileResult installmentIngestionFlowFileResult = new InstallmentIngestionFlowFileResult();
    installmentIngestionFlowFileResult.setProcessedRows(5L);
    installmentIngestionFlowFileResult.setTotalRows(10L);

    when(ingestionFlowFileProcessingLockerActivityMock.acquireIngestionFlowFileProcessingLock(ingestionFlowFileId)).thenReturn(true);
    when(installmentIngestionFlowFileActivityMock.processFile(ingestionFlowFileId)).thenReturn(installmentIngestionFlowFileResult);

    when(synchronizeIngestedDebtPositionActivityMock.synchronizeIngestedDebtPosition(ingestionFlowFileId))
      .thenReturn(new SyncIngestedDebtPositionDTO("\nError on synchronizeIngestedDebtPositionActivity", null, null));

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);

      wf.ingest(ingestionFlowFileId);

      String errorDescription = """
        There were errors during the synchronization of the ingested file:
        Error on synchronizeIngestedDebtPositionActivity
        """.stripTrailing();

      verify(ingestionFlowFileProcessingLockerActivityMock).acquireIngestionFlowFileProcessingLock(ingestionFlowFileId);
      verify(installmentIngestionFlowFileActivityMock).processFile(ingestionFlowFileId);
      verify(updateIngestionFlowStatusActivityMock).updateIngestionFlowFileStatus(
        ingestionFlowFileId,
        IngestionFlowFileStatus.PROCESSING,
        IngestionFlowFileStatus.WARNING,
        InstallmentIngestionFlowFileResult.builder()
          .processedRows(installmentIngestionFlowFileResult.getProcessedRows())
          .totalRows(installmentIngestionFlowFileResult.getTotalRows())
          .errorDescription(errorDescription)
          .build()
      );
      verify(sendEmailIngestionFlowActivityMock).sendIngestionFlowFileCompleteEmail(ingestionFlowFileId, false);
    }
  }

  @Test
  void givenFailingProcessingConditionWhenIngestThenAddErrorDescriptionAndStartMassiveNoticesGenerationWF() {
    long ingestionFlowFileId = 1L;

    InstallmentIngestionFlowFileResult installmentIngestionFlowFileResult = new InstallmentIngestionFlowFileResult();
    installmentIngestionFlowFileResult.setProcessedRows(5L);
    installmentIngestionFlowFileResult.setTotalRows(10L);
    installmentIngestionFlowFileResult.setErrorDescription("Some rows have errors");

    when(ingestionFlowFileProcessingLockerActivityMock.acquireIngestionFlowFileProcessingLock(ingestionFlowFileId)).thenReturn(true);
    when(installmentIngestionFlowFileActivityMock.processFile(ingestionFlowFileId))
      .thenReturn(installmentIngestionFlowFileResult);

    when(synchronizeIngestedDebtPositionActivityMock.synchronizeIngestedDebtPosition(ingestionFlowFileId))
      .thenReturn(new SyncIngestedDebtPositionDTO("\nError on synchronizeIngestedDebtPositionActivity", "generatedId", null));

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);

      wf.ingest(ingestionFlowFileId);

      String errorDescription = """
        Some rows have errors

        There were errors during the synchronization of the ingested file:
        Error on synchronizeIngestedDebtPositionActivity
        """.stripTrailing();

      verify(startMassiveNoticesGenerationWFActivityMock).startMassiveNoticesGenerationWF(ingestionFlowFileId);
      verify(ingestionFlowFileProcessingLockerActivityMock).acquireIngestionFlowFileProcessingLock(ingestionFlowFileId);
      verify(installmentIngestionFlowFileActivityMock).processFile(ingestionFlowFileId);
      verify(updateIngestionFlowStatusActivityMock).updateIngestionFlowFileStatus(
        ingestionFlowFileId,
        IngestionFlowFileStatus.PROCESSING,
        IngestionFlowFileStatus.WARNING,
        InstallmentIngestionFlowFileResult.builder()
          .processedRows(5L)
          .totalRows(10L)
          .errorDescription(errorDescription)
          .build()
      );
      verify(sendEmailIngestionFlowActivityMock).sendIngestionFlowFileCompleteEmail(ingestionFlowFileId, false);
    }
  }

  @Test
  void givenExceptionDuringFileProcessWhenIngestThenAddErrorDescriptionAndStartMassiveNoticesGenerationWF() {
    long ingestionFlowFileId = 1L;

    when(ingestionFlowFileProcessingLockerActivityMock.acquireIngestionFlowFileProcessingLock(ingestionFlowFileId)).thenReturn(true);
    when(installmentIngestionFlowFileActivityMock.processFile(ingestionFlowFileId)).thenThrow(new RuntimeException("DUMMY"));

    when(synchronizeIngestedDebtPositionActivityMock.synchronizeIngestedDebtPosition(ingestionFlowFileId))
      .thenReturn(new SyncIngestedDebtPositionDTO("\nError on synchronizeIngestedDebtPositionActivity", "generatedId", null));

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);

      wf.ingest(ingestionFlowFileId);

      String errorDescription = """
        DUMMY

        There were errors during the synchronization of the ingested file:
        Error on synchronizeIngestedDebtPositionActivity
        """.stripTrailing();

      verify(startMassiveNoticesGenerationWFActivityMock).startMassiveNoticesGenerationWF(ingestionFlowFileId);
      verify(ingestionFlowFileProcessingLockerActivityMock).acquireIngestionFlowFileProcessingLock(ingestionFlowFileId);
      verify(installmentIngestionFlowFileActivityMock).processFile(ingestionFlowFileId);
      verify(updateIngestionFlowStatusActivityMock).updateIngestionFlowFileStatus(
        ingestionFlowFileId,
        IngestionFlowFileStatus.PROCESSING,
        IngestionFlowFileStatus.ERROR,
        IngestionFlowFileResult.builder()
          .errorDescription(errorDescription)
          .build()
      );
      verify(sendEmailIngestionFlowActivityMock).sendIngestionFlowFileCompleteEmail(ingestionFlowFileId, false);
    }
  }

  @Test
  void whenIngestWithLockRetriesThenOk() {
    Long ingestionFlowFileId = 1L;
    String pdfGeneratedId = "generatedId";

    InstallmentIngestionFlowFileResult installmentIngestionFlowFileResult = new InstallmentIngestionFlowFileResult();
    installmentIngestionFlowFileResult.setProcessedRows(5L);
    installmentIngestionFlowFileResult.setTotalRows(10L);
    installmentIngestionFlowFileResult.setOrganizationId(123L);

    when(ingestionFlowFileProcessingLockerActivityMock.acquireIngestionFlowFileProcessingLock(ingestionFlowFileId))
      .thenReturn(false)
      .thenReturn(false)
      .thenReturn(true);
    when(installmentIngestionFlowFileActivityMock.processFile(ingestionFlowFileId)).thenReturn(installmentIngestionFlowFileResult);

    when(synchronizeIngestedDebtPositionActivityMock.synchronizeIngestedDebtPosition(ingestionFlowFileId))
      .thenReturn(new SyncIngestedDebtPositionDTO("", pdfGeneratedId, null));

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);

      wf.ingest(ingestionFlowFileId);

      verify(ingestionFlowFileProcessingLockerActivityMock, times(3)).acquireIngestionFlowFileProcessingLock(ingestionFlowFileId);
      verify(installmentIngestionFlowFileActivityMock).processFile(ingestionFlowFileId);
      verify(startMassiveNoticesGenerationWFActivityMock).startMassiveNoticesGenerationWF(ingestionFlowFileId);
      verify(updateIngestionFlowStatusActivityMock).updateIngestionFlowFileStatus(
        Mockito.eq(ingestionFlowFileId),
        Mockito.eq(IngestionFlowFileStatus.PROCESSING),
        Mockito.eq(IngestionFlowFileStatus.COMPLETED),
        Mockito.same(installmentIngestionFlowFileResult)
      );
      verify(sendEmailIngestionFlowActivityMock).sendIngestionFlowFileCompleteEmail(ingestionFlowFileId, true);
      verify(notifyIngestionActivityMock).notifyIngestionEvent(any(), any());
    }
  }

  @Test
  void whenIngestWithLockRetriesThenContinueAsNew() {
    Long ingestionFlowFileId = 1L;
    String pdfGeneratedId = "generatedId";

    InstallmentIngestionFlowFileResult installmentIngestionFlowFileResult = new InstallmentIngestionFlowFileResult();
    installmentIngestionFlowFileResult.setProcessedRows(5L);
    installmentIngestionFlowFileResult.setTotalRows(10L);
    installmentIngestionFlowFileResult.setOrganizationId(123L);

    AtomicInteger attemptCounter = new AtomicInteger(0);
    doAnswer(invocation -> {
      if (attemptCounter.incrementAndGet() <= 1000) {
        return false;
      }
      return true;
    }).when(ingestionFlowFileProcessingLockerActivityMock).acquireIngestionFlowFileProcessingLock(ingestionFlowFileId);
    when(installmentIngestionFlowFileActivityMock.processFile(ingestionFlowFileId)).thenReturn(installmentIngestionFlowFileResult);

    when(synchronizeIngestedDebtPositionActivityMock.synchronizeIngestedDebtPosition(ingestionFlowFileId))
      .thenReturn(new SyncIngestedDebtPositionDTO("", pdfGeneratedId, null));

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);
      workflowMock.when(() -> Workflow.continueAsNew(Mockito.any())).then(invocation -> null);

      wf.ingest(ingestionFlowFileId);

      verify(ingestionFlowFileProcessingLockerActivityMock, times(1001)).acquireIngestionFlowFileProcessingLock(ingestionFlowFileId);
      verify(installmentIngestionFlowFileActivityMock).processFile(ingestionFlowFileId);
      verify(startMassiveNoticesGenerationWFActivityMock).startMassiveNoticesGenerationWF(ingestionFlowFileId);
      verify(updateIngestionFlowStatusActivityMock).updateIngestionFlowFileStatus(
        Mockito.eq(ingestionFlowFileId),
        Mockito.eq(IngestionFlowFileStatus.PROCESSING),
        Mockito.eq(IngestionFlowFileStatus.COMPLETED),
        Mockito.same(installmentIngestionFlowFileResult)
      );
      verify(sendEmailIngestionFlowActivityMock).sendIngestionFlowFileCompleteEmail(ingestionFlowFileId, true);
      verify(notifyIngestionActivityMock).notifyIngestionEvent(any(), any());
    }
  }
}
