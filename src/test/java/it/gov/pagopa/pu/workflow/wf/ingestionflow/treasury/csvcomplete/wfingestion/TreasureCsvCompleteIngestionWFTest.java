package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.csvcomplete.wfingestion;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.treasury.csvcomplete.TreasuryCsvCompleteIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.treasury.TreasuryIufIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFTest;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.activity.NotifyTreasuryToIufClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.csvcomplete.config.TreasuryCsvCompleteIngestionWFConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.function.Function;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TreasureCsvCompleteIngestionWFTest extends BaseIngestionFlowFileWFTest<TreasuryIufIngestionFlowFileResult> {

  @Mock
  private TreasuryCsvCompleteIngestionActivity treasuryCsvCompleteIngestionActivity;

  @Mock
  private NotifyTreasuryToIufClassificationActivity notifyTreasuryToIufClassificationActivityMock;

  @Override
  protected Pair<Object, Function<Long, TreasuryIufIngestionFlowFileResult>> configureIngestionFlowFileProcessorActivityMock(ApplicationContext applicationContextMock) {
    TreasuryCsvCompleteIngestionWFConfig treasuryCsvCompleteIngestionWFConfigMock = Mockito.mock(TreasuryCsvCompleteIngestionWFConfig.class);

    Mockito.doReturn(treasuryCsvCompleteIngestionWFConfigMock)
      .when(applicationContextMock)
      .getBean(TreasuryCsvCompleteIngestionWFConfig.class);

    when(treasuryCsvCompleteIngestionWFConfigMock.buildNotifyTreasuryToIufClassificationActivityStub())
      .thenReturn(notifyTreasuryToIufClassificationActivityMock);

    Mockito.when(treasuryCsvCompleteIngestionWFConfigMock.buildTreasuryCsvCompleteIngestionActivityStub())
      .thenReturn(treasuryCsvCompleteIngestionActivity);

    return Pair.of(treasuryCsvCompleteIngestionActivity, treasuryCsvCompleteIngestionActivity::processFile);
  }

  @Override
  protected BaseIngestionFlowFileWFImpl<TreasuryIufIngestionFlowFileResult> buildWf() {
    return new TreasuryCsvCompleteIngestionWFImpl();
  }

  @Override
  protected TreasuryIufIngestionFlowFileResult buildExpectedIngestionFlowFileResult() {
    return TreasuryIufIngestionFlowFileResult.builder()
      .iuf2TreasuryIdMap(Map.of("iuf-1", "treasuryid-1"))
      .organizationId(1L)
      .processedRows(10L)
      .totalRows(100L)
      .build();
  }

  @Override
  protected void verifyExtraMocks(long ingestionFlowFileId, TreasuryIufIngestionFlowFileResult expectedResult) {
    expectedResult.getIuf2TreasuryIdMap().forEach((iuf, treasuryId) ->
      verify(notifyTreasuryToIufClassificationActivityMock)
        .signalIufClassificationWithStart(expectedResult.getOrganizationId(), iuf, treasuryId)
    );
  }
}
