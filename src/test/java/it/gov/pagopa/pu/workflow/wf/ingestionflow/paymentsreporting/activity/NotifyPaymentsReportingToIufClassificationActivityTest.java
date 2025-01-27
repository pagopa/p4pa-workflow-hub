package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.activity;

import it.gov.pagopa.payhub.activities.dto.classifications.PaymentsReportingTransferDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.IufClassificationWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyPaymentsReportingSignalDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotifyPaymentsReportingToIufClassificationActivityTest {

  @Mock
  private IufClassificationWFClient iufClassificationWFClientMock;

  private NotifyPaymentsReportingToIufClassificationActivityImpl notifyPaymentsReportingToIufClassificationActivity;

  @BeforeEach
  void setUp() {
    notifyPaymentsReportingToIufClassificationActivity = new NotifyPaymentsReportingToIufClassificationActivityImpl(iufClassificationWFClientMock);
  }

  @Test
  void testSignalIufClassificationWithStart() {
    // Given
    String iuf = "iuf-123";
    PaymentsReportingTransferDTO paymentsReportingTransferDTO = PaymentsReportingTransferDTO.builder()
      .iur("iur-1")
      .iuv("iuv-1")
      .transferIndex(1)
      .orgId(1L)
      .paymentOutcomeCode("CODICEESITO")
      .build();
    IufClassificationNotifyPaymentsReportingSignalDTO expectedSignalDTO = IufClassificationNotifyPaymentsReportingSignalDTO.builder()
      .iuf(iuf)
      .paymentsReportingTransferDTO(paymentsReportingTransferDTO)
      .build();
    // When
    notifyPaymentsReportingToIufClassificationActivity.signalIufClassificationWithStart(iuf, paymentsReportingTransferDTO);

    // Then
    verify(iufClassificationWFClientMock).notifyPaymentsReporting(expectedSignalDTO);

  }
}
