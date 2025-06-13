package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.csvcomplete.wfingestion;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.treasury.csvcomplete.TreasuryCsvCompleteIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.treasury.TreasuryIufIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFTest;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.csvcomplete.config.TreasuryCsvCompleteIngestionWFConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
class TreasureCsvCompleteIngestionWFTest extends BaseIngestionFlowFileWFTest<TreasuryIufIngestionFlowFileResult> {

    @Mock
    private TreasuryCsvCompleteIngestionActivity debtPositionTypeIngestionActivityMock;

    @Override
    protected Pair<Object, Function<Long, TreasuryIufIngestionFlowFileResult>> configureIngestionFlowFileProcessorActivityMock(ApplicationContext applicationContextMock) {
      TreasuryCsvCompleteIngestionWFConfig organizationIngestionWFConfigMock = Mockito.mock(TreasuryCsvCompleteIngestionWFConfig.class);

        Mockito.doReturn(organizationIngestionWFConfigMock)
                .when(applicationContextMock)
                .getBean(TreasuryCsvCompleteIngestionWFConfig.class);

        Mockito.when(organizationIngestionWFConfigMock.buildTreasuryCsvCompleteIngestionActivityStub())
                .thenReturn(debtPositionTypeIngestionActivityMock);

        return Pair.of(debtPositionTypeIngestionActivityMock, debtPositionTypeIngestionActivityMock::processFile);
    }

    @Override
    protected BaseIngestionFlowFileWFImpl<TreasuryIufIngestionFlowFileResult> buildWf() {
        return new TreasuryCsvCompleteIngestionWFImpl();
    }

    @Override
    protected TreasuryIufIngestionFlowFileResult buildExpectedIngestionFlowFileResult() {
        return new TreasuryIufIngestionFlowFileResult();
    }

}
