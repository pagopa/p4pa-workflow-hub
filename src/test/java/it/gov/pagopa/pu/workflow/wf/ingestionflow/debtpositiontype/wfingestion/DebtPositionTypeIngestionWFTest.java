package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtpositiontype.wfingestion;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtpositiontype.DebtPositionTypeIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.debtpositiontype.DebtPositionTypeIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFTest;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtpositiontype.config.DebtPositionTypeIngestionWFConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeIngestionWFTest extends BaseIngestionFlowFileWFTest<DebtPositionTypeIngestionFlowFileResult> {

    @Mock
    private DebtPositionTypeIngestionActivity debtPositionTypeIngestionActivityMock;

    @Override
    protected Pair<Object, Function<Long, DebtPositionTypeIngestionFlowFileResult>> configureIngestionFlowFileProcessorActivityMock(ApplicationContext applicationContextMock) {
      DebtPositionTypeIngestionWFConfig organizationIngestionWFConfigMock = Mockito.mock(DebtPositionTypeIngestionWFConfig.class);

        Mockito.doReturn(organizationIngestionWFConfigMock)
                .when(applicationContextMock)
                .getBean(DebtPositionTypeIngestionWFConfig.class);

        Mockito.when(organizationIngestionWFConfigMock.buildDebtPositionTypeIngestionActivityStub())
                .thenReturn(debtPositionTypeIngestionActivityMock);

        return Pair.of(debtPositionTypeIngestionActivityMock, debtPositionTypeIngestionActivityMock::processFile);
    }

    @Override
    protected BaseIngestionFlowFileWFImpl<DebtPositionTypeIngestionFlowFileResult> buildWf() {
        return new DebtPositionTypeIngestionWFImpl();
    }

    @Override
    protected DebtPositionTypeIngestionFlowFileResult buildExpectedIngestionFlowFileResult() {
        return new DebtPositionTypeIngestionFlowFileResult();
    }

}
