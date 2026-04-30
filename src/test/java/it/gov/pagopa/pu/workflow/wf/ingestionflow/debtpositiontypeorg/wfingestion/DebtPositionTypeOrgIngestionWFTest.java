package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtpositiontypeorg.wfingestion;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtpositiontypeorg.DebtPositionTypeOrgIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.debtpositiontypeorg.DebtPositionTypeOrgIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFTest;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtpositiontypeorg.config.DebtPositionTypeOrgIngestionWFConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeOrgIngestionWFTest extends BaseIngestionFlowFileWFTest<DebtPositionTypeOrgIngestionFlowFileResult> {

    @Mock
    private DebtPositionTypeOrgIngestionActivity debtPositionTypeOrgIngestionActivityMock;

    @Override
    protected Pair<Object, Function<Long, DebtPositionTypeOrgIngestionFlowFileResult>> configureIngestionFlowFileProcessorActivityMock(ApplicationContext applicationContextMock) {
      DebtPositionTypeOrgIngestionWFConfig organizationIngestionWFConfigMock = Mockito.mock(DebtPositionTypeOrgIngestionWFConfig.class);

        Mockito.doReturn(organizationIngestionWFConfigMock)
                .when(applicationContextMock)
                .getBean(DebtPositionTypeOrgIngestionWFConfig.class);

        Mockito.when(organizationIngestionWFConfigMock.buildDebtPositionTypeOrgIngestionActivityStub())
                .thenReturn(debtPositionTypeOrgIngestionActivityMock);

        return Pair.of(debtPositionTypeOrgIngestionActivityMock, debtPositionTypeOrgIngestionActivityMock::processFile);
    }

    @Override
    protected BaseIngestionFlowFileWFImpl<DebtPositionTypeOrgIngestionFlowFileResult> buildWf() {
        return new DebtPositionTypeOrgIngestionWFImpl();
    }

    @Override
    protected DebtPositionTypeOrgIngestionFlowFileResult buildExpectedIngestionFlowFileResult() {
        return DebtPositionTypeOrgIngestionFlowFileResult.builder().organizationId(1L).build();
    }

}
