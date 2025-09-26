package it.gov.pagopa.pu.workflow.wf.ingestionflow.assessments.wfingestion;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.assessments.AssessmentsIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.assessments.AssessmentsIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFTest;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.assessments.config.AssessmentsIngestionWFConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
class AssessmentsIngestionWFTest extends BaseIngestionFlowFileWFTest<AssessmentsIngestionFlowFileResult> {

  @Mock
  private AssessmentsIngestionActivity assessmentsIngestionActivityMock;

  @Override
  protected Pair<Object, Function<Long, AssessmentsIngestionFlowFileResult>> configureIngestionFlowFileProcessorActivityMock(ApplicationContext applicationContextMock) {
    AssessmentsIngestionWFConfig organizationIngestionWFConfigMock = Mockito.mock(AssessmentsIngestionWFConfig.class);

    Mockito.doReturn(organizationIngestionWFConfigMock)
      .when(applicationContextMock)
      .getBean(AssessmentsIngestionWFConfig.class);

    Mockito.when(organizationIngestionWFConfigMock.buildAssessmentsIngestionActivityStub())
      .thenReturn(assessmentsIngestionActivityMock);

    return Pair.of(assessmentsIngestionActivityMock, assessmentsIngestionActivityMock::processFile);
  }

  @Override
  protected BaseIngestionFlowFileWFImpl<AssessmentsIngestionFlowFileResult> buildWf() {
    return new AssessmentsIngestionWFImpl();
  }

  @Override
  protected AssessmentsIngestionFlowFileResult buildExpectedIngestionFlowFileResult() {
    return AssessmentsIngestionFlowFileResult.builder().organizationId(1L).build();
  }
}
