package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.IngestionFlowFileProcessingLockerActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.paymentsreporting.HandlePaymentsReportingDeletionActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.paymentsreporting.PaymentsReportingIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.dto.classifications.PaymentsReportingTransferDTO;
import it.gov.pagopa.payhub.activities.dto.ingestion.paymentsreporting.PaymentsReportingIngestionFlowFileActivityResult;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFileStatus;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.activity.NotifyIngestionActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.config.BaseIngestionFlowFileWFConfig;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.activity.NotifyPaymentsReportingToIufClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.config.PaymentsReportingIngestionWfConfig;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentsReportingIngestionWFTest {

  @Mock
  private UpdateIngestionFlowStatusActivity updateIngestionFlowStatusActivityMock;
  @Mock
  private SendEmailIngestionFlowActivity sendEmailIngestionFlowActivityMock;
  @Mock
  private IngestionFlowFileProcessingLockerActivity ingestionFlowFileProcessingLockerActivityMock;
  @Mock
  private PaymentsReportingIngestionFlowFileActivity paymentsReportingIngestionFlowFileActivityMock;
  @Mock
  private NotifyPaymentsReportingToIufClassificationActivity notifyPaymentsReportingToIufClassificationActivityMock;
  @Mock
  private HandlePaymentsReportingDeletionActivity handlePaymentsReportingDeletionActivityMock;
  @Mock
  private NotifyIngestionActivity notifyIngestionActivityMock;

  private PaymentsReportingIngestionWFImpl wf;

  @BeforeEach
  void setUp() {
    BaseIngestionFlowFileWFConfig baseIngestionFlowFileWFConfigMock = mock(BaseIngestionFlowFileWFConfig.class);
    PaymentsReportingIngestionWfConfig paymentsReportingIngestionWfConfig = mock(PaymentsReportingIngestionWfConfig.class);
    ApplicationContext applicationContextMock = mock(ApplicationContext.class);

    doReturn(paymentsReportingIngestionWfConfig)
      .when(applicationContextMock)
      .getBean(PaymentsReportingIngestionWfConfig.class);

    doReturn(baseIngestionFlowFileWFConfigMock)
      .when(applicationContextMock)
      .getBean(BaseIngestionFlowFileWFConfig.class);

    when(baseIngestionFlowFileWFConfigMock.buildUpdateIngestionFlowStatusActivityStub())
      .thenReturn(updateIngestionFlowStatusActivityMock);
    when(baseIngestionFlowFileWFConfigMock.buildSendEmailIngestionFlowActivityStub())
      .thenReturn(sendEmailIngestionFlowActivityMock);

    when(paymentsReportingIngestionWfConfig.buildIngestionFlowFileProcessingLockerActivityStub())
      .thenReturn(ingestionFlowFileProcessingLockerActivityMock);
    when(paymentsReportingIngestionWfConfig.buildPaymentsReportingIngestionFlowFileActivityStub())
      .thenReturn(paymentsReportingIngestionFlowFileActivityMock);
    when(paymentsReportingIngestionWfConfig.buildNotifyPaymentsReportingToIufClassificationActivityStub())
      .thenReturn(notifyPaymentsReportingToIufClassificationActivityMock);
    when(paymentsReportingIngestionWfConfig.buildHandlePaymentsReportingDeletionActivity())
        .thenReturn(handlePaymentsReportingDeletionActivityMock);
    when(baseIngestionFlowFileWFConfigMock.buildNotifyIngestionActivityStub())
      .thenReturn(notifyIngestionActivityMock);

    wf = new PaymentsReportingIngestionWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      updateIngestionFlowStatusActivityMock,
      sendEmailIngestionFlowActivityMock,
      ingestionFlowFileProcessingLockerActivityMock,
      paymentsReportingIngestionFlowFileActivityMock,
      notifyPaymentsReportingToIufClassificationActivityMock,
      handlePaymentsReportingDeletionActivityMock,
      notifyIngestionActivityMock
    );
  }

  @Test
  void givenSuccessfulProcessingConditionWhenIngestThenOk() {
    long ingestionFlowFileId = 1L;
    long organizationId = 2L;

    PaymentsReportingTransferDTO paymentsReportingTransferDTO = PaymentsReportingTransferDTO.builder()
      .iur("iur-1")
      .iuv("iuv-1")
      .transferIndex(1)
      .orgId(organizationId)
      .paymentOutcomeCode("CODICEESITO")
      .build();

    PaymentsReportingTransferDTO paymentsReportingTransferDTODeleted = PaymentsReportingTransferDTO.builder()
      .iur("iur-2")
      .iuv("iuv-2")
      .transferIndex(1)
      .orgId(organizationId)
      .paymentOutcomeCode("CODICEESITO")
      .build();

    PaymentsReportingIngestionFlowFileActivityResult expectedResult = PaymentsReportingIngestionFlowFileActivityResult.builder()
      .iuf("iuf-1")
      .organizationId(organizationId)
      .transfers(List.of(paymentsReportingTransferDTO))
      .build();

    when(ingestionFlowFileProcessingLockerActivityMock.acquireIngestionFlowFileProcessingLock(ingestionFlowFileId)).thenReturn(true);
    when(handlePaymentsReportingDeletionActivityMock.handlePaymentsReportingDeletion(organizationId, "iuf-1", ingestionFlowFileId))
        .thenReturn(List.of(paymentsReportingTransferDTODeleted));
    when(paymentsReportingIngestionFlowFileActivityMock.processFile(ingestionFlowFileId)).thenReturn(expectedResult);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);

      wf.ingest(ingestionFlowFileId);

      verify(ingestionFlowFileProcessingLockerActivityMock).acquireIngestionFlowFileProcessingLock(ingestionFlowFileId);
      verify(paymentsReportingIngestionFlowFileActivityMock).processFile(ingestionFlowFileId);
      verify(handlePaymentsReportingDeletionActivityMock).handlePaymentsReportingDeletion(organizationId, "iuf-1", ingestionFlowFileId);
      verify(notifyPaymentsReportingToIufClassificationActivityMock).signalPaymentsReportingIufClassificationWithStart(
        organizationId, "iuf-1", List.of(paymentsReportingTransferDTO, paymentsReportingTransferDTODeleted)
      );
      verify(updateIngestionFlowStatusActivityMock).updateIngestionFlowFileStatus(
        Mockito.eq(ingestionFlowFileId),
        Mockito.eq(IngestionFlowFileStatus.PROCESSING),
        Mockito.eq(IngestionFlowFileStatus.COMPLETED),
        Mockito.same(expectedResult)
      );
      verify(sendEmailIngestionFlowActivityMock).sendIngestionFlowFileCompleteEmail(ingestionFlowFileId, true);
      verify(notifyIngestionActivityMock).notifyIngestionEvent(any(), any());
    }
  }

  @Test
  void whenIngestWithLockRetriesThenOk() {
    long ingestionFlowFileId = 1L;
    long organizationId = 2L;

    PaymentsReportingTransferDTO paymentsReportingTransferDTO = PaymentsReportingTransferDTO.builder()
      .iur("iur-1")
      .iuv("iuv-1")
      .transferIndex(1)
      .orgId(organizationId)
      .paymentOutcomeCode("CODICEESITO")
      .build();

    PaymentsReportingIngestionFlowFileActivityResult expectedResult = PaymentsReportingIngestionFlowFileActivityResult.builder()
      .iuf("iuf-1")
      .organizationId(organizationId)
      .transfers(List.of(paymentsReportingTransferDTO))
      .build();

    when(ingestionFlowFileProcessingLockerActivityMock.acquireIngestionFlowFileProcessingLock(ingestionFlowFileId))
      .thenReturn(false)
      .thenReturn(false)
      .thenReturn(true);
    when(paymentsReportingIngestionFlowFileActivityMock.processFile(ingestionFlowFileId)).thenReturn(expectedResult);
    when(handlePaymentsReportingDeletionActivityMock.handlePaymentsReportingDeletion(organizationId, "iuf-1", ingestionFlowFileId))
      .thenReturn(List.of());

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);

      wf.ingest(ingestionFlowFileId);

      verify(ingestionFlowFileProcessingLockerActivityMock, times(3)).acquireIngestionFlowFileProcessingLock(ingestionFlowFileId);
      verify(paymentsReportingIngestionFlowFileActivityMock).processFile(ingestionFlowFileId);
      verify(notifyPaymentsReportingToIufClassificationActivityMock).signalPaymentsReportingIufClassificationWithStart(
        organizationId,
        "iuf-1",
        List.of(paymentsReportingTransferDTO)
      );
      verify(updateIngestionFlowStatusActivityMock).updateIngestionFlowFileStatus(
        Mockito.eq(ingestionFlowFileId),
        Mockito.eq(IngestionFlowFileStatus.PROCESSING),
        Mockito.eq(IngestionFlowFileStatus.COMPLETED),
        Mockito.same(expectedResult)
      );
      verify(sendEmailIngestionFlowActivityMock).sendIngestionFlowFileCompleteEmail(ingestionFlowFileId, true);
      verify(notifyIngestionActivityMock).notifyIngestionEvent(any(), any());
    }
  }

  @Test
  void whenIngestWithLockRetriesThenContinueAsNew() {
    long ingestionFlowFileId = 1L;
    long organizationId = 2L;

    PaymentsReportingTransferDTO paymentsReportingTransferDTO = PaymentsReportingTransferDTO.builder()
      .iur("iur-1")
      .iuv("iuv-1")
      .transferIndex(1)
      .orgId(organizationId)
      .paymentOutcomeCode("CODICEESITO")
      .build();

    PaymentsReportingIngestionFlowFileActivityResult expectedResult = PaymentsReportingIngestionFlowFileActivityResult.builder()
      .iuf("iuf-1")
      .organizationId(organizationId)
      .transfers(List.of(paymentsReportingTransferDTO))
      .build();

    AtomicInteger attemptCounter = new AtomicInteger(0);
    Mockito.doAnswer(invocation -> {
      if (attemptCounter.incrementAndGet() <= 1000) {
        return false;
      }
      return true;
    }).when(ingestionFlowFileProcessingLockerActivityMock).acquireIngestionFlowFileProcessingLock(ingestionFlowFileId);
    when(paymentsReportingIngestionFlowFileActivityMock.processFile(ingestionFlowFileId)).thenReturn(expectedResult);
    when(handlePaymentsReportingDeletionActivityMock.handlePaymentsReportingDeletion(organizationId, "iuf-1", ingestionFlowFileId))
      .thenReturn(List.of());

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);
      workflowMock.when(() -> Workflow.continueAsNew(Mockito.any())).then(invocation -> null);

      wf.ingest(ingestionFlowFileId);

      verify(ingestionFlowFileProcessingLockerActivityMock, times(1001)).acquireIngestionFlowFileProcessingLock(ingestionFlowFileId);
      verify(paymentsReportingIngestionFlowFileActivityMock).processFile(ingestionFlowFileId);
      verify(notifyPaymentsReportingToIufClassificationActivityMock).signalPaymentsReportingIufClassificationWithStart(
        organizationId,
        "iuf-1",
        List.of(paymentsReportingTransferDTO)
      );
      verify(updateIngestionFlowStatusActivityMock).updateIngestionFlowFileStatus(
        Mockito.eq(ingestionFlowFileId),
        Mockito.eq(IngestionFlowFileStatus.PROCESSING),
        Mockito.eq(IngestionFlowFileStatus.COMPLETED),
        Mockito.same(expectedResult)
      );
      verify(sendEmailIngestionFlowActivityMock).sendIngestionFlowFileCompleteEmail(ingestionFlowFileId, true);
      verify(notifyIngestionActivityMock).notifyIngestionEvent(any(), any());
    }
  }
}
