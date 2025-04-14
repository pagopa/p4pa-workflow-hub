package it.gov.pagopa.pu.workflow.wf.classification.iud.wfclassification;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.classifications.ClearClassifyIudActivity;
import it.gov.pagopa.payhub.activities.activity.classifications.IudClassificationActivity;
import it.gov.pagopa.payhub.activities.dto.classifications.IudClassificationActivityResult;
import it.gov.pagopa.payhub.activities.dto.classifications.Transfer2ClassifyDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iud.config.IudClassificationWfConfig;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyPaymentNotificationSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyReceiptSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.activity.StartTransferClassificationActivity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IudClassificationWFTest {

  @Mock
  private IudClassificationActivity iudClassificationActivityMock;
  @Mock
  private ClearClassifyIudActivity clearClassifyIudActivityMock;
  @Mock
  private StartTransferClassificationActivity startTransferClassificationActivityMock;

  private IudClassificationWFImpl wf;

  @BeforeEach
  void setUp() {
    IudClassificationWfConfig iudClassificationWfConfigMock = mock(IudClassificationWfConfig.class);
    ApplicationContext applicationContextMock = mock(ApplicationContext.class);

    when(iudClassificationWfConfigMock.buildClearClassifyIudActivityStub())
        .thenReturn(clearClassifyIudActivityMock);

    when(iudClassificationWfConfigMock.buildIudClassificationActivityStub())
        .thenReturn(iudClassificationActivityMock);

    when(iudClassificationWfConfigMock.buildStartTransferClassificationActivityStub())
        .thenReturn(startTransferClassificationActivityMock);

    when(applicationContextMock.getBean(IudClassificationWfConfig.class))
        .thenReturn(iudClassificationWfConfigMock);

    wf = new IudClassificationWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(
        iudClassificationActivityMock,
        clearClassifyIudActivityMock,
        startTransferClassificationActivityMock
    );
  }

  @Test
  void classify() {
    notifyReceipt(1L, "iud1", "iuv1", "iur1", 1);
    notifyReceipt(1L, "iud2", "iuv2", "iur2", 1);

    notifyPaymentNotification(1L, "iud3");

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

  void notifyReceipt(Long orgId, String iud, String iuv, String iur, int transferIndex) {
    // Given
    IudClassificationNotifyReceiptSignalDTO signalDTO = IudClassificationNotifyReceiptSignalDTO.builder()
      .orgId(orgId)
      .iud(iud)
      .iuv(iuv)
      .iur(iur)
      .transferIndex(transferIndex)
      .build();

    when(clearClassifyIudActivityMock.deleteClassificationByIud(orgId, iud))
      .thenReturn(1L);

    // When
    wf.notifyReceipt(signalDTO);
  }

  void notifyPaymentNotification(Long orgId, String iud) {
    // Given
    IudClassificationNotifyPaymentNotificationSignalDTO signalDTO = IudClassificationNotifyPaymentNotificationSignalDTO.builder()
      .organizationId(orgId)
      .iud(iud)
      .build();
    Transfer2ClassifyDTO transfer2ClassifyDTO = Transfer2ClassifyDTO.builder()
      .iur("iur3")
      .iuv("iuv3")
      .transferIndex(1)
      .build();
    IudClassificationActivityResult activityResult = IudClassificationActivityResult.builder()
      .organizationId(orgId)
      .transfers2classify(List.of(transfer2ClassifyDTO))
      .build();

    when(clearClassifyIudActivityMock.deleteClassificationByIud(orgId, iud))
      .thenReturn(1L);
    when(iudClassificationActivityMock.classify(orgId, iud))
      .thenReturn(activityResult);

    // When
    wf.notifyPaymentNotification(signalDTO);
  }
}
