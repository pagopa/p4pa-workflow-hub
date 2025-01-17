package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.activity;

import it.gov.pagopa.payhub.activities.dto.classifications.Transfer2ClassifyDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.IufClassificationWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyPaymentsReportingSignalDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

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
    Long organizationId = 1L;
    String iuf = "iuf-123";
    String outcomeCode = "OK";
    List<Transfer2ClassifyDTO> transfers2Classify = Collections.emptyList();

    IufClassificationNotifyPaymentsReportingSignalDTO expectedSignalDTO = IufClassificationNotifyPaymentsReportingSignalDTO.builder()
      .organizationId(organizationId)
      .iuf(iuf)
      .outcomeCode(outcomeCode)
      .transfers2classify(Collections.emptyList())
      .build();

    // When
    notifyPaymentsReportingToIufClassificationActivity.signalIufClassificationWithStart(organizationId, iuf, outcomeCode, transfers2Classify);

    // Then
    verify(iufClassificationWFClientMock).notifyPaymentsReporting(expectedSignalDTO);

  }
}
