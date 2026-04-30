package it.gov.pagopa.pu.workflow.wf.ingestionflow.assessmentsregistry.wfingestion;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.assessmentsregistry.AssessmentsRegistryIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.assessmentsregistry.AssessmentsRegistryIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFTest;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.assessmentsregistry.config.AssessmentsRegistryIngestionWFConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
class AssessmentsRegistryIngestionWFTest extends BaseIngestionFlowFileWFTest<AssessmentsRegistryIngestionFlowFileResult> {

  @Mock
  private AssessmentsRegistryIngestionActivity assessmentsRegistryIngestionActivityMock;

  @Override
  protected Pair<Object, Function<Long, AssessmentsRegistryIngestionFlowFileResult>> configureIngestionFlowFileProcessorActivityMock(ApplicationContext applicationContextMock) {
    AssessmentsRegistryIngestionWFConfig organizationIngestionWFConfigMock = Mockito.mock(AssessmentsRegistryIngestionWFConfig.class);

    Mockito.doReturn(organizationIngestionWFConfigMock)
      .when(applicationContextMock)
      .getBean(AssessmentsRegistryIngestionWFConfig.class);

    Mockito.when(organizationIngestionWFConfigMock.buildAssessmentsRegistryIngestionActivityStub())
      .thenReturn(assessmentsRegistryIngestionActivityMock);

    return Pair.of(assessmentsRegistryIngestionActivityMock, assessmentsRegistryIngestionActivityMock::processFile);
  }

  @Override
  protected BaseIngestionFlowFileWFImpl<AssessmentsRegistryIngestionFlowFileResult> buildWf() {
    return new AssessmentsRegistryIngestionWFImpl();
  }

  @Override
  protected AssessmentsRegistryIngestionFlowFileResult buildExpectedIngestionFlowFileResult() {
    return AssessmentsRegistryIngestionFlowFileResult.builder().organizationId(1L).build();
  }

}
