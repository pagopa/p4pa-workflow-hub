package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.wfingestion;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.IngestionFlowFileProcessingLockerActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtposition.InstallmentIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtposition.MassiveNoticeGenerationStatusRetrieverActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtposition.SynchronizeIngestedDebtPositionActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.debtposition.InstallmentIngestionFlowFileResult;
import it.gov.pagopa.payhub.activities.dto.ingestion.debtposition.SyncIngestedDebtPositionDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.SignedUrlResultDTO;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFileStatus;
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
  private MassiveNoticeGenerationStatusRetrieverActivity massiveNoticeGenerationStatusRetrieverActivityMock;

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
    Mockito.when(debtPositionIngestionFlowWfConfigMock.buildMassiveNoticeGenerationStatusRetrieverActivity())
      .thenReturn(massiveNoticeGenerationStatusRetrieverActivityMock);

    Mockito.when(applicationContextMock.getBean(DebtPositionIngestionFlowWfConfig.class))
      .thenReturn(debtPositionIngestionFlowWfConfigMock);

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
      massiveNoticeGenerationStatusRetrieverActivityMock
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

    Mockito.when(ingestionFlowFileProcessingLockerActivityMock.acquireProcessingLock(ingestionFlowFileId)).thenReturn(true);
    Mockito.when(installmentIngestionFlowFileActivityMock.processFile(ingestionFlowFileId)).thenReturn(installmentIngestionFlowFileResult);

    Mockito.when(synchronizeIngestedDebtPositionActivityMock.synchronizeIngestedDebtPosition(ingestionFlowFileId))
      .thenReturn(new SyncIngestedDebtPositionDTO("", pdfGeneratedId));

    Mockito.when(massiveNoticeGenerationStatusRetrieverActivityMock.retrieveNoticesGenerationStatus(
        installmentIngestionFlowFileResult.getOrganizationId(), pdfGeneratedId))
      .thenReturn(new SignedUrlResultDTO());

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);

      wf.ingest(ingestionFlowFileId);

      Mockito.verify(ingestionFlowFileProcessingLockerActivityMock).acquireProcessingLock(ingestionFlowFileId);
      Mockito.verify(installmentIngestionFlowFileActivityMock).processFile(ingestionFlowFileId);
      Mockito.verify(massiveNoticeGenerationStatusRetrieverActivityMock).retrieveNoticesGenerationStatus(
        installmentIngestionFlowFileResult.getOrganizationId(), pdfGeneratedId);
      Mockito.verify(updateIngestionFlowStatusActivityMock).updateStatus(
        Mockito.eq(ingestionFlowFileId),
        Mockito.eq(IngestionFlowFileStatus.PROCESSING),
        Mockito.eq(IngestionFlowFileStatus.COMPLETED),
        Mockito.same(installmentIngestionFlowFileResult)
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
      .thenReturn(new SyncIngestedDebtPositionDTO("\nError on synchronizeIngestedDebtPositionActivity", null));

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
        InstallmentIngestionFlowFileResult.builder()
          .processedRows(installmentIngestionFlowFileResult.getProcessedRows())
          .totalRows(installmentIngestionFlowFileResult.getTotalRows())
          .errorDescription(errorDescription)
          .build()
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
      .thenReturn(new SyncIngestedDebtPositionDTO("\nError on synchronizeIngestedDebtPositionActivity", "generatedId"));

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
        InstallmentIngestionFlowFileResult.builder()
          .errorDescription(errorDescription)
          .build()
      );
      Mockito.verify(sendEmailIngestionFlowActivityMock).sendEmail(ingestionFlowFileId, false);
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

    Mockito.when(ingestionFlowFileProcessingLockerActivityMock.acquireProcessingLock(ingestionFlowFileId))
      .thenReturn(false)
      .thenReturn(false)
      .thenReturn(true);
    Mockito.when(installmentIngestionFlowFileActivityMock.processFile(ingestionFlowFileId)).thenReturn(installmentIngestionFlowFileResult);

    Mockito.when(synchronizeIngestedDebtPositionActivityMock.synchronizeIngestedDebtPosition(ingestionFlowFileId))
      .thenReturn(new SyncIngestedDebtPositionDTO("", pdfGeneratedId));

    Mockito.when(massiveNoticeGenerationStatusRetrieverActivityMock.retrieveNoticesGenerationStatus(
        installmentIngestionFlowFileResult.getOrganizationId(), pdfGeneratedId))
      .thenReturn(null)
      .thenReturn(null)
      .thenReturn(new SignedUrlResultDTO());

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);

      wf.ingest(ingestionFlowFileId);

      Mockito.verify(ingestionFlowFileProcessingLockerActivityMock, Mockito.times(3)).acquireProcessingLock(ingestionFlowFileId);
      Mockito.verify(installmentIngestionFlowFileActivityMock).processFile(ingestionFlowFileId);
      Mockito.verify(massiveNoticeGenerationStatusRetrieverActivityMock, Mockito.times(3)).retrieveNoticesGenerationStatus(
        installmentIngestionFlowFileResult.getOrganizationId(), pdfGeneratedId);
      Mockito.verify(updateIngestionFlowStatusActivityMock).updateStatus(
        Mockito.eq(ingestionFlowFileId),
        Mockito.eq(IngestionFlowFileStatus.PROCESSING),
        Mockito.eq(IngestionFlowFileStatus.COMPLETED),
        Mockito.same(installmentIngestionFlowFileResult)
      );
      Mockito.verify(sendEmailIngestionFlowActivityMock).sendEmail(ingestionFlowFileId, true);
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
    Mockito.doAnswer(invocation -> {
      if (attemptCounter.incrementAndGet() <= 1000) {
        return false;
      }
      return true;
    }).when(ingestionFlowFileProcessingLockerActivityMock).acquireProcessingLock(ingestionFlowFileId);
    Mockito.when(installmentIngestionFlowFileActivityMock.processFile(ingestionFlowFileId)).thenReturn(installmentIngestionFlowFileResult);

    Mockito.when(synchronizeIngestedDebtPositionActivityMock.synchronizeIngestedDebtPosition(ingestionFlowFileId))
      .thenReturn(new SyncIngestedDebtPositionDTO("", pdfGeneratedId));

    Mockito.when(massiveNoticeGenerationStatusRetrieverActivityMock.retrieveNoticesGenerationStatus(
        installmentIngestionFlowFileResult.getOrganizationId(), pdfGeneratedId))
      .thenReturn(new SignedUrlResultDTO());

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);
      workflowMock.when(() -> Workflow.continueAsNew(Mockito.any())).then(invocation -> null);

      wf.ingest(ingestionFlowFileId);

      Mockito.verify(ingestionFlowFileProcessingLockerActivityMock, Mockito.times(1001)).acquireProcessingLock(ingestionFlowFileId);
      Mockito.verify(installmentIngestionFlowFileActivityMock).processFile(ingestionFlowFileId);
      Mockito.verify(massiveNoticeGenerationStatusRetrieverActivityMock).retrieveNoticesGenerationStatus(
        installmentIngestionFlowFileResult.getOrganizationId(), pdfGeneratedId);
      Mockito.verify(updateIngestionFlowStatusActivityMock).updateStatus(
        Mockito.eq(ingestionFlowFileId),
        Mockito.eq(IngestionFlowFileStatus.PROCESSING),
        Mockito.eq(IngestionFlowFileStatus.COMPLETED),
        Mockito.same(installmentIngestionFlowFileResult)
      );
      Mockito.verify(sendEmailIngestionFlowActivityMock).sendEmail(ingestionFlowFileId, true);
    }
  }

  @Test
  void whenMassiveNoticeGenerationStatusRetriesMaxAttemptsReached() {
    Long ingestionFlowFileId = 1L;
    String pdfGeneratedId = "generatedId";

    InstallmentIngestionFlowFileResult installmentIngestionFlowFileResult = new InstallmentIngestionFlowFileResult();
    installmentIngestionFlowFileResult.setProcessedRows(5L);
    installmentIngestionFlowFileResult.setTotalRows(10L);
    installmentIngestionFlowFileResult.setOrganizationId(123L);

    Mockito.when(ingestionFlowFileProcessingLockerActivityMock.acquireProcessingLock(ingestionFlowFileId)).thenReturn(true);
    Mockito.when(installmentIngestionFlowFileActivityMock.processFile(ingestionFlowFileId)).thenReturn(installmentIngestionFlowFileResult);

    Mockito.when(synchronizeIngestedDebtPositionActivityMock.synchronizeIngestedDebtPosition(ingestionFlowFileId))
      .thenReturn(new SyncIngestedDebtPositionDTO("", pdfGeneratedId));

    AtomicInteger attemptCounter = new AtomicInteger(0);
    Mockito.doAnswer(invocation -> {
        if (attemptCounter.incrementAndGet() <= 30) {
          return null;
        }
        return new SignedUrlResultDTO();
      }).when(massiveNoticeGenerationStatusRetrieverActivityMock)
      .retrieveNoticesGenerationStatus(installmentIngestionFlowFileResult.getOrganizationId(), pdfGeneratedId);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);

      wf.ingest(ingestionFlowFileId);

      String errorDescription = String.format("""
        There were errors during the synchronization of the ingested Debt Position:Max attempts reached for pdfGeneratedId %s. Unable to retrieve generation status.
        """, pdfGeneratedId).stripTrailing();

      Mockito.verify(ingestionFlowFileProcessingLockerActivityMock).acquireProcessingLock(ingestionFlowFileId);
      Mockito.verify(installmentIngestionFlowFileActivityMock).processFile(ingestionFlowFileId);
      Mockito.verify(massiveNoticeGenerationStatusRetrieverActivityMock, Mockito.times(30))
        .retrieveNoticesGenerationStatus(installmentIngestionFlowFileResult.getOrganizationId(), pdfGeneratedId);
      Mockito.verify(updateIngestionFlowStatusActivityMock).updateStatus(
        ingestionFlowFileId,
        IngestionFlowFileStatus.PROCESSING,
        IngestionFlowFileStatus.COMPLETED,
        InstallmentIngestionFlowFileResult.builder()
          .processedRows(installmentIngestionFlowFileResult.getProcessedRows())
          .totalRows(installmentIngestionFlowFileResult.getTotalRows())
          .errorDescription(errorDescription)
          .organizationId(123L)
          .build());
      Mockito.verify(sendEmailIngestionFlowActivityMock).sendEmail(ingestionFlowFileId, true);
    }
  }

}
