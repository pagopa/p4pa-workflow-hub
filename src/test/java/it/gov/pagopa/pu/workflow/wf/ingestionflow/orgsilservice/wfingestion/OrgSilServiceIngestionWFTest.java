package it.gov.pagopa.pu.workflow.wf.ingestionflow.orgsilservice.wfingestion;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.orgsilservice.OrgSilServiceIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.orgsilservice.OrgSilServiceIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFTest;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.orgsilservice.config.OrgSilServiceIngestionWFConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
class OrgSilServiceIngestionWFTest extends BaseIngestionFlowFileWFTest<OrgSilServiceIngestionFlowFileResult> {

    @Mock
    private OrgSilServiceIngestionActivity orgSilServiceIngestionActivityMock;

    @Override
    protected Pair<Object, Function<Long, OrgSilServiceIngestionFlowFileResult>> configureIngestionFlowFileProcessorActivityMock(ApplicationContext applicationContextMock) {
        OrgSilServiceIngestionWFConfig orgSilServiceIngestionWfConfigMock = Mockito.mock(OrgSilServiceIngestionWFConfig.class);

        Mockito.doReturn(orgSilServiceIngestionWfConfigMock)
                .when(applicationContextMock)
                .getBean(OrgSilServiceIngestionWFConfig.class);

        Mockito.when(orgSilServiceIngestionWfConfigMock.buildOrgSilServiceIngestionActivityStub())
                .thenReturn(orgSilServiceIngestionActivityMock);

        return Pair.of(orgSilServiceIngestionActivityMock, orgSilServiceIngestionActivityMock::processFile);
    }

    @Override
    protected BaseIngestionFlowFileWFImpl<OrgSilServiceIngestionFlowFileResult> buildWf() {
        return new OrgSilServiceIngestionWFImpl();
    }

    @Override
    protected OrgSilServiceIngestionFlowFileResult buildExpectedIngestionFlowFileResult() {
        return new OrgSilServiceIngestionFlowFileResult();
    }

}
