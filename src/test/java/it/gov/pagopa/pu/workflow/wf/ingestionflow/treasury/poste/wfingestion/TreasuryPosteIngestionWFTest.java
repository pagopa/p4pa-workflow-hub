package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.poste.wfingestion;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.treasury.poste.TreasuryPosteIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.treasury.TreasuryIufIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFTest;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.activity.NotifyTreasuryToIufClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.poste.config.TreasuryPosteIngestionWFConfig;
import java.util.Map;
import java.util.function.Function;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class TreasuryPosteIngestionWFTest extends
  BaseIngestionFlowFileWFTest<TreasuryIufIngestionFlowFileResult> {

  @Mock
  private TreasuryPosteIngestionActivity treasuryPosteIngestionActivity;

  @Mock
  private NotifyTreasuryToIufClassificationActivity notifyTreasuryToIufClassificationActivityMock;

  @Override
  protected Pair<Object, Function<Long, TreasuryIufIngestionFlowFileResult>> configureIngestionFlowFileProcessorActivityMock(
    ApplicationContext applicationContextMock) {
    TreasuryPosteIngestionWFConfig treasuryPosteIngestionWFConfigMock = Mockito.mock(
      TreasuryPosteIngestionWFConfig.class);

    Mockito.doReturn(treasuryPosteIngestionWFConfigMock)
      .when(applicationContextMock)
      .getBean(TreasuryPosteIngestionWFConfig.class);

    when(
      treasuryPosteIngestionWFConfigMock.buildNotifyTreasuryToIufClassificationActivityStub())
      .thenReturn(notifyTreasuryToIufClassificationActivityMock);

    Mockito.when(
        treasuryPosteIngestionWFConfigMock.buildTreasuryPosteIngestionActivityStub())
      .thenReturn(treasuryPosteIngestionActivity);

    return Pair.of(treasuryPosteIngestionActivity,
      treasuryPosteIngestionActivity::processFile);
  }

  @Override
  protected BaseIngestionFlowFileWFImpl<TreasuryIufIngestionFlowFileResult> buildWf() {
    return new TreasuryPosteIngestionWFImpl();
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
  protected void verifyExtraMocks(long ingestionFlowFileId,
    TreasuryIufIngestionFlowFileResult expectedResult) {
    expectedResult.getIuf2TreasuryIdMap().forEach((iuf, treasuryId) ->
      verify(notifyTreasuryToIufClassificationActivityMock)
        .signalTreasuryIufClassificationWithStart(
          expectedResult.getOrganizationId(), iuf, treasuryId)
    );
  }

}
