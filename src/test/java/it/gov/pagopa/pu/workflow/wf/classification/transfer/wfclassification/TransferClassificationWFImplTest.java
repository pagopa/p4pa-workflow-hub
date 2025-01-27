package it.gov.pagopa.pu.workflow.wf.classification.transfer.wfclassification;

import it.gov.pagopa.pu.workflow.wf.classification.transfer.config.TransferClassificationWfConfig;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.dto.TransferClassificationStartSignalDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class TransferClassificationWFImplTest {
  private static final Long ORGANIZATION = 123L;
  private static final String IUV = "01011112222333345";
  private static final String IUR = "IUR";
  private static final int TRANSFER_INDEX = 1;

  private TransferClassificationWFImpl wf;

  @BeforeEach
  void setUp() {
    TransferClassificationWfConfig transferClassificationWfConfig = Mockito.mock(TransferClassificationWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    when(applicationContextMock.getBean(TransferClassificationWfConfig.class))
      .thenReturn(transferClassificationWfConfig);

    wf = new TransferClassificationWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @Test
  void givenClassifyThenSuccess() {
    assertDoesNotThrow(() -> wf.startTransferClassification(new TransferClassificationStartSignalDTO(ORGANIZATION, IUV, IUR, TRANSFER_INDEX)));
  }
}
