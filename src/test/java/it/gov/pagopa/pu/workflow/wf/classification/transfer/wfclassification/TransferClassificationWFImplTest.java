package it.gov.pagopa.pu.workflow.wf.classification.transfer.wfclassification;

import it.gov.pagopa.payhub.activities.activity.classifications.TransferClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.config.TransferClassificationWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferClassificationWFImplTest {
  private static final Long ORGANIZATION = 123L;
  private static final String IUV = "01011112222333345";
  private static final String IUR = "IUR";
  private static final int INDEX = 1;

  @Mock
  private TransferClassificationActivity transferClassificationActivityMock;

  private TransferClassificationWFImpl wf;

  @BeforeEach
  void setUp() {
    TransferClassificationWfConfig transferClassificationWfConfig = Mockito.mock(TransferClassificationWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    when(transferClassificationWfConfig.buildTransferClassificationActivityStub())
      .thenReturn(transferClassificationActivityMock);

    when(applicationContextMock.getBean(TransferClassificationWfConfig.class))
      .thenReturn(transferClassificationWfConfig);

    wf = new TransferClassificationWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(transferClassificationActivityMock);
  }

  @Test
  void givenClassifyThenSuccess() {
    doNothing().when(transferClassificationActivityMock)
      .classify(ORGANIZATION, IUV, IUR, INDEX);
    // when
     wf.classify(ORGANIZATION, IUV, IUR, INDEX);

    // then
    verify(transferClassificationActivityMock).classify(ORGANIZATION, IUV, IUR, INDEX);
  }
}
