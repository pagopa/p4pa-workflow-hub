package it.gov.pagopa.pu.workflow.wf.ingestionflow.organization.wfingestion;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.organization.OrganizationIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.organization.OrganizationIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFTest;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.organization.config.OrganizationIngestionWFConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
class OrganizationIngestionWFTest extends BaseIngestionFlowFileWFTest<OrganizationIngestionFlowFileResult> {

    @Mock
    private OrganizationIngestionActivity organizationIngestionActivityMock;

    @Override
    protected Pair<Object, Function<Long, OrganizationIngestionFlowFileResult>> configureIngestionFlowFileProcessorActivityMock(ApplicationContext applicationContextMock) {
        OrganizationIngestionWFConfig organizationIngestionWfConfigMock = Mockito.mock(OrganizationIngestionWFConfig.class);

        Mockito.doReturn(organizationIngestionWfConfigMock)
                .when(applicationContextMock)
                .getBean(OrganizationIngestionWFConfig.class);

        Mockito.when(organizationIngestionWfConfigMock.buildOrganizationIngestionActivityStub())
                .thenReturn(organizationIngestionActivityMock);

        return Pair.of(organizationIngestionActivityMock, organizationIngestionActivityMock::processFile);
    }

    @Override
    protected BaseIngestionFlowFileWFImpl<OrganizationIngestionFlowFileResult> buildWf() {
        return new OrganizationIngestionWFImpl();
    }

    @Override
    protected OrganizationIngestionFlowFileResult buildExpectedIngestionFlowFileResult() {
        return new OrganizationIngestionFlowFileResult();
    }

}
