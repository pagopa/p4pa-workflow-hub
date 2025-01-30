package it.gov.pagopa.pu.workflow.wf.classification.iuf.classification;

import it.gov.pagopa.payhub.activities.activity.classifications.ClearClassifyIufActivity;
import it.gov.pagopa.payhub.activities.activity.classifications.IufClassificationActivity;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class IufClassificationWFTest {

  @Mock
  private ClearClassifyIufActivity clearClassifyIufActivityMock;
  @Mock
  private IufClassificationActivity iufClassificationActivityMock;
  @Mock
  private StartTransferClassificationActivity startTransferClassificationActivityMock;

  private IufClassificationWFImpl wf;


  @BeforeEach
  void init() {
    IufClassificationWfConfig iufClassificationWfConfigMock = Mockito.mock(IufClassificationWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(iufClassificationWfConfigMock.buildClearClassifyIufActivityStub())
      .thenReturn(clearClassifyIufActivityMock);

    Mockito.when(iufClassificationWfConfigMock.buildIufClassificationActivityStub())
      .thenReturn(iufClassificationActivityMock);

    Mockito.when(iufClassificationWfConfigMock.buildStartTransferClassificationActivityStub())
      .thenReturn(startTransferClassificationActivityMock);

    Mockito.when(applicationContextMock.getBean(IufClassificationWfConfig.class))
      .thenReturn(iufClassificationWfConfigMock);

    wf = new IufClassificationWFImpl();
    wf.setApplicationContext(applicationContextMock);

  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      clearClassifyIufActivityMock,
      iufClassificationActivityMock,
      startTransferClassificationActivityMock
    );
  }

  @Test
  void testSignalAndWfExecution() {
    notifyTreasury("treasuryId1", "iuf1", "iur1", "iuv1");
    notifyTreasury("treasuryId2", "iuf2", "iur2", "iuv2");

    notifyPaymentsReporting("iuf1", "iur1", "iuv1");
    notifyPaymentsReporting("iuf3", "iur3", "iuv3");

    wf.classify();

    Mockito.verify(startTransferClassificationActivityMock)
      .signalTransferClassificationWithStart(1L, "iuv1", "iur1", 1);
    Mockito.verify(startTransferClassificationActivityMock)
      .signalTransferClassificationWithStart(1L, "iuv2", "iur2", 1);
    Mockito.verify(startTransferClassificationActivityMock)
      .signalTransferClassificationWithStart(1L, "iuv3", "iur3", 1);
  }

  void notifyTreasury(String treasuryId, String iuf, String iur, String iuv) {
    // Given
    IufClassificationNotifyTreasurySignalDTO signalDTO =
      IufClassificationNotifyTreasurySignalDTO.builder()
        .organizationId(1L).treasuryId(treasuryId).iuf(iuf).build();

    Mockito.when(clearClassifyIufActivityMock.deleteClassificationByIuf(1L, iuf)).thenReturn(1L);
    Mockito.when(iufClassificationActivityMock.classify(1L, treasuryId, iuf)).thenReturn(
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

  void notifyPaymentsReporting(String iuf, String iur, String iuv) {
    // Given
    IufClassificationNotifyPaymentsReportingSignalDTO signalDTO = IufClassificationNotifyPaymentsReportingSignalDTO.builder()
      .iuf(iuf)
      .organizationId(1L)
      .transfers(List.of(PaymentsReportingTransferDTO.builder()
        .iur(iur)
        .iuv(iuv)
        .transferIndex(1)
        .orgId(1L)
        .paymentOutcomeCode("CODICEESITO")
        .build()))
      .build();

    Mockito.when(clearClassifyIufActivityMock.deleteClassificationByIuf(1L, iuf)).thenReturn(1L);

    // When
    wf.notifyPaymentsReporting(signalDTO);
  }

}
