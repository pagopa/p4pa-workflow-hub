package it.gov.pagopa.pu.workflow.wf.classification.iuf.wfclassification;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.classifications.ClearClassifyIufActivity;
import it.gov.pagopa.payhub.activities.activity.classifications.ClearClassifyTreasuryActivity;
import it.gov.pagopa.payhub.activities.activity.classifications.IufClassificationActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.receipt.PaymentsReportingImplicitReceiptHandlerActivity;
import it.gov.pagopa.payhub.activities.dto.classifications.IufClassificationActivityResult;
import it.gov.pagopa.payhub.activities.dto.classifications.PaymentsReportingTransferDTO;
import it.gov.pagopa.payhub.activities.dto.classifications.Transfer2ClassifyDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.activity.StartTransferClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.config.IufClassificationWfConfig;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyPaymentsReportingSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyTreasurySignalDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@ExtendWith(MockitoExtension.class)
class IufClassificationWFTest {
  private static final List<String> PAYMENT_OUTCOME_CODES_FOR_DUMMY_RECEIPT = List.of("8", "9");

  @Mock
  private ClearClassifyIufActivity clearClassifyIufActivityMock;
  @Mock
  private ClearClassifyTreasuryActivity clearClassifyTreasuryActivityMock;
  @Mock
  private IufClassificationActivity iufClassificationActivityMock;
  @Mock
  private StartTransferClassificationActivity startTransferClassificationActivityMock;
  @Mock
  private PaymentsReportingImplicitReceiptHandlerActivity paymentsReportingImplicitReceiptHandlerActivityMock;

  private IufClassificationWFImpl wf;


  @BeforeEach
  void init() {
    IufClassificationWfConfig iufClassificationWfConfigMock = Mockito.mock(IufClassificationWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(iufClassificationWfConfigMock.buildClearClassifyIufActivityStub())
      .thenReturn(clearClassifyIufActivityMock);

    Mockito.when(iufClassificationWfConfigMock.buildClearClassifyTreasuryActivityStub())
      .thenReturn(clearClassifyTreasuryActivityMock);

    Mockito.when(iufClassificationWfConfigMock.buildIufClassificationActivityStub())
      .thenReturn(iufClassificationActivityMock);

    Mockito.when(iufClassificationWfConfigMock.buildStartTransferClassificationActivityStub())
      .thenReturn(startTransferClassificationActivityMock);

    Mockito.when(iufClassificationWfConfigMock.buildPaymentsReportingImplicitReceiptHandlerActivityStub())
      .thenReturn(paymentsReportingImplicitReceiptHandlerActivityMock);

    Mockito.when(applicationContextMock.getBean(IufClassificationWfConfig.class))
      .thenReturn(iufClassificationWfConfigMock);

    wf = new IufClassificationWFImpl();
    wf.setApplicationContext(applicationContextMock);

  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      clearClassifyIufActivityMock,
      clearClassifyTreasuryActivityMock,
      iufClassificationActivityMock,
      startTransferClassificationActivityMock,
      paymentsReportingImplicitReceiptHandlerActivityMock
    );
  }

  @Test
  void testSignalAndWfExecution() {
    notifyTreasury("treasuryId1", "iuf1", "iur1", "iuv1");
    notifyTreasury("treasuryId2", "iuf2", "iur2", "iuv2");

    notifyPaymentsReporting("iuf1", "iur1", "iuv1", "9");
    notifyPaymentsReporting("iuf3", "iur3", "iuv3", "0");

    try(MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(Workflow::isEveryHandlerFinished).thenReturn(true);

      wf.classify();

      workflowMock.verify(() -> Workflow.await(Mockito.argThat(Supplier::get)));

      Mockito.verify(startTransferClassificationActivityMock)
        .signalTransferClassificationWithStart(1L, "iuv1", "iur1", 1);
      Mockito.verify(startTransferClassificationActivityMock)
        .signalTransferClassificationWithStart(1L, "iuv2", "iur2", 1);
      Mockito.verify(startTransferClassificationActivityMock)
        .signalTransferClassificationWithStart(1L, "iuv3", "iur3", 1);
    }
  }

  void notifyTreasury(String treasuryId, String iuf, String iur, String iuv) {
    // Given
    IufClassificationNotifyTreasurySignalDTO signalDTO =
      IufClassificationNotifyTreasurySignalDTO.builder()
        .organizationId(1L).treasuryId(treasuryId).iuf(iuf).build();

    Mockito.when(clearClassifyTreasuryActivityMock.deleteClassificationByTreasuryId(1L, treasuryId)).thenReturn(1);
    Mockito.when(iufClassificationActivityMock.classifyIuf(1L, treasuryId, iuf)).thenReturn(
      IufClassificationActivityResult.builder()
        .organizationId(1L)
        .transfers2classify(Collections.singletonList(
          Transfer2ClassifyDTO.builder().iur(iur).iuv(iuv).transferIndex(1).build())
        )
        .build()
    );

    // When
    wf.notifyTreasury(signalDTO);
  }

  void notifyPaymentsReporting(String iuf, String iur, String iuv, String paymentOutcomeCode) {
    // Given
    IufClassificationNotifyPaymentsReportingSignalDTO signalDTO = IufClassificationNotifyPaymentsReportingSignalDTO.builder()
      .iuf(iuf)
      .organizationId(1L)
      .transfers(List.of(PaymentsReportingTransferDTO.builder()
        .iur(iur)
        .iuv(iuv)
        .transferIndex(1)
        .orgId(1L)
        .paymentOutcomeCode(paymentOutcomeCode)
        .build()))
      .build();

    Mockito.when(clearClassifyIufActivityMock.deleteClassificationByIuf(1L, iuf)).thenReturn(1);

    if (PAYMENT_OUTCOME_CODES_FOR_DUMMY_RECEIPT.contains(paymentOutcomeCode)) {
      Mockito.doNothing().when(paymentsReportingImplicitReceiptHandlerActivityMock).handleImplicitReceipt(signalDTO.getTransfers().getFirst());
    }
    // When
    wf.notifyPaymentsReporting(signalDTO);
  }

}
