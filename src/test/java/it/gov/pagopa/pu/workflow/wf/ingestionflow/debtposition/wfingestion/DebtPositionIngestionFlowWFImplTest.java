package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.wfingestion;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.IngestionFlowFileProcessingLockerActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtposition.InstallmentIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.debtposition.InstallmentIngestionFlowFileResult;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFile;
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
  private UpdateIngestionFlowStatusActivity updateIngestionFlowStatusActivity;
  @Mock
  private SendEmailIngestionFlowActivity sendEmailIngestionFlowActivity;

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
      .thenReturn(updateIngestionFlowStatusActivity);
    Mockito.when(debtPositionIngestionFlowWfConfigMock.buildSendEmailIngestionFlowActivityStub())
      .thenReturn(sendEmailIngestionFlowActivity);

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

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);

      wf.ingest(ingestionFlowFileId);

      Mockito.verify(ingestionFlowFileProcessingLockerActivityMock).acquireProcessingLock(ingestionFlowFileId);
      Mockito.verify(installmentIngestionFlowFileActivityMock).processFile(ingestionFlowFileId);
      Mockito.verify(updateIngestionFlowStatusActivity).updateStatus(
        Mockito.eq(ingestionFlowFileId),
        Mockito.eq(IngestionFlowFile.StatusEnum.PROCESSING),
        Mockito.eq(IngestionFlowFile.StatusEnum.COMPLETED),
        Mockito.isNull(),
        Mockito.isNull()
      );
      Mockito.verify(sendEmailIngestionFlowActivity).sendEmail(ingestionFlowFileId, true);
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

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);

      wf.ingest(ingestionFlowFileId);

      Mockito.verify(ingestionFlowFileProcessingLockerActivityMock).acquireProcessingLock(ingestionFlowFileId);
      Mockito.verify(installmentIngestionFlowFileActivityMock).processFile(ingestionFlowFileId);
      Mockito.verify(updateIngestionFlowStatusActivity).updateStatus(
        ingestionFlowFileId,
        IngestionFlowFile.StatusEnum.PROCESSING,
        IngestionFlowFile.StatusEnum.ERROR,
        "Unexpected error when processing DebtPositionIngestion file: DUMMY",
        null
      );
      Mockito.verify(sendEmailIngestionFlowActivity).sendEmail(ingestionFlowFileId, false);
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

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);

      wf.ingest(ingestionFlowFileId);

      Mockito.verify(ingestionFlowFileProcessingLockerActivityMock, Mockito.times(3)).acquireProcessingLock(ingestionFlowFileId);
      Mockito.verify(installmentIngestionFlowFileActivityMock).processFile(ingestionFlowFileId);
      Mockito.verify(updateIngestionFlowStatusActivity).updateStatus(
        Mockito.eq(ingestionFlowFileId),
        Mockito.eq(IngestionFlowFile.StatusEnum.PROCESSING),
        Mockito.eq(IngestionFlowFile.StatusEnum.COMPLETED),
        Mockito.isNull(),
        Mockito.isNull()
      );
      Mockito.verify(sendEmailIngestionFlowActivity).sendEmail(ingestionFlowFileId, true);
    }
  }
}
