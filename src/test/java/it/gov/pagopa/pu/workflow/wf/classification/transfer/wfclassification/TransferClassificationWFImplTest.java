package it.gov.pagopa.pu.workflow.wf.classification.transfer.wfclassification;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.classifications.TransferClassificationActivity;
import it.gov.pagopa.payhub.activities.dto.classifications.TransferSemanticKeyDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentNoPII;
import it.gov.pagopa.pu.debtposition.dto.generated.Transfer;
import it.gov.pagopa.pu.workflow.utils.TestUtils;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.activity.StartAssessmentClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.config.TransferClassificationWfConfig;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.dto.TransferClassificationStartSignalDTO;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.function.Supplier;
import uk.co.jemos.podam.api.PodamFactory;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferClassificationWFImplTest {

  @Mock
  private TransferClassificationActivity transferClassificationActivityMock;
  @Mock
  private StartAssessmentClassificationActivity startAssessmentClassificationActivityMock;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  private TransferClassificationWFImpl wf;

  @BeforeEach
  void setUp() {
    TransferClassificationWfConfig transferClassificationWfConfig = Mockito.mock(TransferClassificationWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    when(applicationContextMock.getBean(TransferClassificationWfConfig.class))
      .thenReturn(transferClassificationWfConfig);
    when(transferClassificationWfConfig.buildTransferClassificationActivityStub())
      .thenReturn(transferClassificationActivityMock);
    when(transferClassificationWfConfig.buildStartAssessmentClassificationActivityStub())
      .thenReturn(startAssessmentClassificationActivityMock);

    wf = new TransferClassificationWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      transferClassificationActivityMock,
      startAssessmentClassificationActivityMock
    );
  }

  @Test
  void testSignalAndWfExecution() {
    signalTransferClassification(1L, "iuv1", "iur1", 1);
    signalTransferClassification(2L, "iuv1", "iur1", 1);
    signalTransferClassification(2L, "iuv2", "iur2", 2);
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    installment.setIuv("IUV");
    installment.setIud("IUD");
    Transfer transfer = podamFactory.manufacturePojo(Transfer.class);

    TransferSemanticKeyDTO semanticKeyDTO = new TransferSemanticKeyDTO(1L, "iuv1", "iur1", 1);
    Pair<InstallmentNoPII, Transfer> expectedResult = Pair.of(installment, transfer);
    try(MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(Workflow::isEveryHandlerFinished).thenReturn(true);
      Mockito.when(transferClassificationActivityMock.classifyTransfer(semanticKeyDTO)).thenReturn(expectedResult);
      wf.classify();

      workflowMock.verify(() -> Workflow.await(Mockito.argThat(Supplier::get)));

      Mockito.verify(transferClassificationActivityMock).classifyTransfer(semanticKeyDTO);
      Mockito.verify(transferClassificationActivityMock)
        .classifyTransfer(new TransferSemanticKeyDTO(2L, "iuv1", "iur1", 1));
      Mockito.verify(transferClassificationActivityMock)
        .classifyTransfer(new TransferSemanticKeyDTO(2L, "iuv2", "iur2", 2));
      Mockito.verify(startAssessmentClassificationActivityMock)
        .signalAssessmentClassificationWithStart(1L, "IUV", "IUD");
    }
  }

  private void signalTransferClassification(Long organizationId, String iuv, String iur, int transferIndex) {
    wf.startTransferClassification(new TransferClassificationStartSignalDTO(organizationId, iuv, iur, transferIndex));
  }
}
