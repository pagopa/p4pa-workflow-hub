package it.gov.pagopa.pu.workflow.wf.classification.iuf.classification;

import it.gov.pagopa.payhub.activities.activity.classifications.ClearClassifyIufActivity;
import it.gov.pagopa.payhub.activities.activity.classifications.IufClassificationActivity;
import it.gov.pagopa.payhub.activities.activity.classifications.TransferClassificationActivity;
import it.gov.pagopa.payhub.activities.dto.classifications.IufClassificationActivityResult;
import it.gov.pagopa.payhub.activities.dto.classifications.Transfer2ClassifyDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.config.IufClassificationWfConfig;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyPaymentsReportingSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyTreasurySignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.activity.StartTransferClassificationActivity;
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
class IufClassificationWFTest {

  @Mock
  private ClearClassifyIufActivity clearClassifyIufActivity;
  @Mock
  private IufClassificationActivity iufClassificationActivity;
  @Mock
  private TransferClassificationActivity transferClassificationActivity;
  @Mock
  private StartTransferClassificationActivity startTransferClassificationActivity;

  private IufClassificationWFImpl wf;


  @BeforeEach
  void init() {
    IufClassificationWfConfig iufClassificationWfConfigMock = Mockito.mock(IufClassificationWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(iufClassificationWfConfigMock.buildClearClassifyIufActivityStub())
      .thenReturn(clearClassifyIufActivity);

    Mockito.when(iufClassificationWfConfigMock.buildIufClassificationActivityStub())
      .thenReturn(iufClassificationActivity);

    Mockito.when(iufClassificationWfConfigMock.buildStartTransferClassificationActivityStub())
      .thenReturn(startTransferClassificationActivity);

    Mockito.when(applicationContextMock.getBean(IufClassificationWfConfig.class))
      .thenReturn(iufClassificationWfConfigMock);

    wf = new IufClassificationWFImpl();
    wf.setApplicationContext(applicationContextMock);

  }

  @Test
  void testClassify() {
    // When
    wf.classify();

    // Then
    verify(startTransferClassificationActivity, times(0)).signalTransferClassificationWithStart(
      eq(1L), any(String.class), any(String.class), any(Integer.class));
  }

  @Test
  void testNotifyTreasury() {
    // Given
    IufClassificationNotifyTreasurySignalDTO signalDTO =
      IufClassificationNotifyTreasurySignalDTO.builder()
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
    wf.notifyTreasury(signalDTO);

    // Then
    Mockito.verify(clearClassifyIufActivity).deleteClassificationByIuf(1L, "iuf123");
    Mockito.verify(iufClassificationActivity).classify(1L, "2T", "iuf123");
  }


  @Test
  void testNotifyPaymentsReporting() {
    // Given
    IufClassificationNotifyPaymentsReportingSignalDTO signalDTO = IufClassificationNotifyPaymentsReportingSignalDTO.builder()
      .organizationId(1L)
      .iuf("iuf123")
      .outcomeCode("outcome123")
      .build();

    Mockito.when(clearClassifyIufActivity.deleteClassificationByIuf(1L, "iuf123")).thenReturn(true);

    // When
    wf.notifyPaymentsReporting(signalDTO);

    // Then
    Mockito.verify(clearClassifyIufActivity).deleteClassificationByIuf(1L, "iuf123");
  }

}
