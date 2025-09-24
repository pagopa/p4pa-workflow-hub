package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.xls.wfIngestion;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.treasury.xls.TreasuryXlsIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.treasury.TreasuryIufIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFTest;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.activity.NotifyTreasuryToIufClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.xls.config.TreasuryXlsIngestionWFConfig;
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
class TreasuryXlsIngestionWFImplTest extends BaseIngestionFlowFileWFTest<TreasuryIufIngestionFlowFileResult> {
  @Mock
  private TreasuryXlsIngestionActivity treasuryXlsIngestionActivityMock;

  @Mock
  private NotifyTreasuryToIufClassificationActivity notifyTreasuryToIufClassificationActivityMock;

  @Override
  protected Pair<Object, Function<Long, TreasuryIufIngestionFlowFileResult>> configureIngestionFlowFileProcessorActivityMock(ApplicationContext applicationContextMock) {
    TreasuryXlsIngestionWFConfig treasuryXlsIngestionWFConfigMock = Mockito.mock(TreasuryXlsIngestionWFConfig.class);

    Mockito.doReturn(treasuryXlsIngestionWFConfigMock)
      .when(applicationContextMock)
      .getBean(TreasuryXlsIngestionWFConfig.class);

    when(treasuryXlsIngestionWFConfigMock.buildNotifyTreasuryToIufClassificationActivityStub())
      .thenReturn(notifyTreasuryToIufClassificationActivityMock);

    Mockito.when(treasuryXlsIngestionWFConfigMock.buildTreasuryXlsIngestionActivityStub())
      .thenReturn(treasuryXlsIngestionActivityMock);

    return Pair.of(treasuryXlsIngestionActivityMock, treasuryXlsIngestionActivityMock::processFile);
  }

  @Override
  protected BaseIngestionFlowFileWFImpl<TreasuryIufIngestionFlowFileResult> buildWf() {
    return new TreasuryXlsIngestionWFImpl();
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
        .signalTreasuryIufClassificationWithStart(expectedResult.getOrganizationId(), iuf, treasuryId)
    );
  }
}
