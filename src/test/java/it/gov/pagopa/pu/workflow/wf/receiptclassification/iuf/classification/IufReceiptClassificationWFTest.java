package it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.classification;

import it.gov.pagopa.payhub.activities.activity.classifications.ClearClassifyIufActivity;
import it.gov.pagopa.payhub.activities.activity.classifications.IufClassificationActivity;
import it.gov.pagopa.payhub.activities.activity.classifications.TransferClassificationActivity;
import it.gov.pagopa.payhub.activities.dto.classifications.IufClassificationActivityResult;
import it.gov.pagopa.payhub.activities.dto.classifications.Transfer2ClassifyDTO;
import it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.config.IufReceiptClassificationWfConfig;
import it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.dto.IufReceiptClassificationForReportingSignalDTO;
import it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.dto.IufReceiptClassificationForTreasurySignalDTO;
import it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.helper.TransferClassificationWfHelperActivity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IufReceiptClassificationWFTest {

  @Mock
  private ClearClassifyIufActivity clearClassifyIufActivity;
  @Mock
  private IufClassificationActivity iufClassificationActivity;
  @Mock
  private TransferClassificationActivity transferClassificationActivity;
  @Mock
  private TransferClassificationWfHelperActivity transferClassificationWfHelperActivity;

  private IufReceiptClassificationWFImpl wf;


  @BeforeEach
  void init() {
    IufReceiptClassificationWfConfig iufReceiptClassificationWfConfigMock = Mockito.mock(IufReceiptClassificationWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(iufReceiptClassificationWfConfigMock.buildClearClassifyIufActivityStub())
      .thenReturn(clearClassifyIufActivity);

    Mockito.when(iufReceiptClassificationWfConfigMock.buildIufClassificationActivityStub())
      .thenReturn(iufClassificationActivity);

    Mockito.when(iufReceiptClassificationWfConfigMock.buildTransferClassificationActivityStub())
      .thenReturn(transferClassificationActivity);

    Mockito.when(iufReceiptClassificationWfConfigMock.buildTransferClassificationStarterHelperActivityStub())
      .thenReturn(transferClassificationWfHelperActivity);

    Mockito.when(applicationContextMock.getBean(IufReceiptClassificationWfConfig.class))
      .thenReturn(iufReceiptClassificationWfConfigMock);

    wf = new IufReceiptClassificationWFImpl();
    wf.setApplicationContext(applicationContextMock);

  }

@Test
void testClassify() {
  // When
  wf.classify();

  // Then
  verify(transferClassificationWfHelperActivity, times(0)).signalTransferClassificationWithStart(
    eq(1L), any(String.class), any(String.class), any(Integer.class));
}

  @Test
  void testSignalForTreasury() {
    // Given
    IufReceiptClassificationForTreasurySignalDTO signalDTO =
      IufReceiptClassificationForTreasurySignalDTO.builder()
        .organizationId(1L).treasuryId("2T").iuf("iuf123").build();

    Mockito.when(clearClassifyIufActivity.deleteClassificationByIuf(1L, "iuf123")).thenReturn(true);
    Mockito.when(iufClassificationActivity.classify(1L, "2T", "iuf123")).thenReturn(
      IufClassificationActivityResult.builder()
        .organizationId(1L)
        .success(true)
        .transfers2classify(Collections.singletonList(new Transfer2ClassifyDTO()))
        .build()
    );

    // When
    wf.signalForTreasury(signalDTO);

    // Then
    Mockito.verify(clearClassifyIufActivity).deleteClassificationByIuf(1L, "iuf123");
    Mockito.verify(iufClassificationActivity).classify(1L, "2T", "iuf123");
  }

  @Test
  void testSignalForReporting() {
    // Given
    IufReceiptClassificationForReportingSignalDTO signalDTO = IufReceiptClassificationForReportingSignalDTO.builder()
      .organizationId(1L)
      .iuf("iuf123")
      .outcomeCode("outcome123")
      .build();

    Mockito.when(clearClassifyIufActivity.deleteClassificationByIuf(1L, "iuf123")).thenReturn(true);

    // When
    wf.signalForReporting(signalDTO);

    // Then
    Mockito.verify(clearClassifyIufActivity).deleteClassificationByIuf(1L, "iuf123");
  }
}
