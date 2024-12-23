package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.paymentsreporting.PaymentsReportingIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.activity.utility.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.dto.paymentsreporting.PaymentsReportingIngestionFlowFileActivityResult;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.config.PaymentsReportingIngestionWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class PaymentsReportingIngestionWFTest {

  @Mock
  private UpdateIngestionFlowStatusActivity updateIngestionFlowStatusActivityMock;
  @Mock
  private PaymentsReportingIngestionFlowFileActivity paymentsReportingIngestionFlowFileActivityMock;
  @Mock
  private SendEmailIngestionFlowActivity sendEmailIngestionFlowActivityMock;

  private PaymentsReportingIngestionWFImpl wf;

  @BeforeEach
  void init(){
    PaymentsReportingIngestionWfConfig paymentsReportingIngestionWfConfigMock = Mockito.mock(PaymentsReportingIngestionWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(paymentsReportingIngestionWfConfigMock.buildUpdateIngestionFlowStatusActivityStub())
        .thenReturn(updateIngestionFlowStatusActivityMock);
    Mockito.when(paymentsReportingIngestionWfConfigMock.buildPaymentsReportingIngestionFlowFileActivityStub())
      .thenReturn(paymentsReportingIngestionFlowFileActivityMock);
    Mockito.when(paymentsReportingIngestionWfConfigMock.buildSendEmailIngestionFlowActivityStub())
      .thenReturn(sendEmailIngestionFlowActivityMock);

    Mockito.when(applicationContextMock.getBean(PaymentsReportingIngestionWfConfig.class))
      .thenReturn(paymentsReportingIngestionWfConfigMock);

    wf = new PaymentsReportingIngestionWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      updateIngestionFlowStatusActivityMock,
      paymentsReportingIngestionFlowFileActivityMock,
      sendEmailIngestionFlowActivityMock);
  }

  @Test
  void givenSuccessfulProcessingConditionWhenIngestThenOk(){
    // Given
    long ingestionFlowFileId = 1L;
    boolean success = true;

    Mockito.when(paymentsReportingIngestionFlowFileActivityMock.processFile(ingestionFlowFileId))
      .thenReturn(new PaymentsReportingIngestionFlowFileActivityResult(Collections.emptyList(), success, null));

    // When
    wf.ingest(ingestionFlowFileId);

    // Then
    Mockito.verify(updateIngestionFlowStatusActivityMock).updateStatus(ingestionFlowFileId, "IMPORT_IN_ELAB");
    Mockito.verify(sendEmailIngestionFlowActivityMock).sendEmail(ingestionFlowFileId, success);
    Mockito.verify(updateIngestionFlowStatusActivityMock).updateStatus(ingestionFlowFileId, "OK");
  }

  @Test
  void givenFailingProcessingConditionWhenIngestThenKo(){
    // Given
    long ingestionFlowFileId = 1L;
    boolean success = false;

    Mockito.when(paymentsReportingIngestionFlowFileActivityMock.processFile(ingestionFlowFileId))
      .thenReturn(new PaymentsReportingIngestionFlowFileActivityResult(Collections.emptyList(), success, null));

    // When
    wf.ingest(ingestionFlowFileId);

    // Then
    Mockito.verify(updateIngestionFlowStatusActivityMock).updateStatus(ingestionFlowFileId, "IMPORT_IN_ELAB");
    Mockito.verify(sendEmailIngestionFlowActivityMock).sendEmail(ingestionFlowFileId, success);
    Mockito.verify(updateIngestionFlowStatusActivityMock).updateStatus(ingestionFlowFileId, "KO");
  }
}
