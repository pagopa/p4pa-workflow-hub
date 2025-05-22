package it.gov.pagopa.pu.workflow.wf.ingestionflow.organization.wfingestion;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.organization.OrganizationIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.organization.OrganizationIngestionFlowFileResult;
import it.gov.pagopa.payhub.activities.exception.NotRetryableActivityException;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFileStatus;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.organization.config.OrganizationIngestionWFConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationIngestionWFTest {

  @Mock
  private UpdateIngestionFlowStatusActivity updateIngestionFlowStatusActivityMock;
  @Mock
  private OrganizationIngestionActivity organizationIngestionActivityMock;
  @Mock
  private SendEmailIngestionFlowActivity sendEmailIngestionFlowActivityMock;

  private OrganizationIngestionWFImpl wf;

  @BeforeEach
  void init() {
    OrganizationIngestionWFConfig organizationIngestionWfConfigMock = Mockito.mock(OrganizationIngestionWFConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(organizationIngestionWfConfigMock.buildUpdateIngestionFlowStatusActivityStub())
      .thenReturn(updateIngestionFlowStatusActivityMock);
    Mockito.when(organizationIngestionWfConfigMock.buildOrganizationIngestionActivityStub())
      .thenReturn(organizationIngestionActivityMock);
    Mockito.when(organizationIngestionWfConfigMock.buildSendEmailIngestionFlowActivityStub())
      .thenReturn(sendEmailIngestionFlowActivityMock);

    Mockito.when(applicationContextMock.getBean(OrganizationIngestionWFConfig.class))
      .thenReturn(organizationIngestionWfConfigMock);

    wf = new OrganizationIngestionWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      updateIngestionFlowStatusActivityMock,
      organizationIngestionActivityMock,
      sendEmailIngestionFlowActivityMock);
  }

  @Test
  void givenSuccessfulProcessingConditionWhenIngestThenOk() {
    // Given
    long ingestionFlowFileId = 1L;
    long brokerId = 2L;
    String brokerCF = "CF-1";

    OrganizationIngestionFlowFileResult result =
      new OrganizationIngestionFlowFileResult(List.of("IPACODE1"), brokerCF,brokerId);

    when(organizationIngestionActivityMock.processFile(ingestionFlowFileId))
      .thenReturn(result);

    // When
    wf.ingest(ingestionFlowFileId);

    // Then
    Mockito.verify(updateIngestionFlowStatusActivityMock)
      .updateStatus(ingestionFlowFileId, IngestionFlowFileStatus.UPLOADED, IngestionFlowFileStatus.PROCESSING, null);

    Mockito.verify(sendEmailIngestionFlowActivityMock)
      .sendEmail(ingestionFlowFileId, true);
    Mockito.verify(updateIngestionFlowStatusActivityMock)
      .updateStatus(ingestionFlowFileId, IngestionFlowFileStatus.PROCESSING, IngestionFlowFileStatus.COMPLETED, result);

  }

  @Test
  void givenFailingProcessingConditionWhenIngestThenKo() {
    // Given
    long ingestionFlowFileId = 1L;

    Mockito.when(organizationIngestionActivityMock.processFile(ingestionFlowFileId))
      .thenThrow(new NotRetryableActivityException("DUMMY"));

    OrganizationIngestionFlowFileResult ingestionFlowFileResult = OrganizationIngestionFlowFileResult.builder()
      .errorDescription("DUMMY")
      .build();

    // When
    wf.ingest(ingestionFlowFileId);

    // Then
    Mockito.verify(updateIngestionFlowStatusActivityMock)
      .updateStatus(ingestionFlowFileId, IngestionFlowFileStatus.UPLOADED, IngestionFlowFileStatus.PROCESSING, null);
    Mockito.verify(sendEmailIngestionFlowActivityMock)
      .sendEmail(ingestionFlowFileId, false);

    Mockito.verify(updateIngestionFlowStatusActivityMock)
      .updateStatus(ingestionFlowFileId, IngestionFlowFileStatus.PROCESSING, IngestionFlowFileStatus.ERROR, ingestionFlowFileResult);
  }
}
