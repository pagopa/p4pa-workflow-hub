package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.wfingestion;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.treasury.TreasuryOpiIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.treasury.TreasuryIufIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFTest;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.activity.NotifyTreasuryToIufClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.config.TreasuryOpiIngestionWfConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.function.Function;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TreasuryOpiIngestionWFImplTest extends BaseIngestionFlowFileWFTest<TreasuryIufIngestionFlowFileResult> {
  @Mock
  private TreasuryOpiIngestionActivity treasuryOpiIngestionActivityMock;
  @Mock
  private NotifyTreasuryToIufClassificationActivity notifyTreasuryToIufClassificationActivityMock;

  @Override
  protected Pair<Object, Function<Long, TreasuryIufIngestionFlowFileResult>> configureIngestionFlowFileProcessorActivityMock(ApplicationContext applicationContextMock) {
    TreasuryOpiIngestionWfConfig treasuryOpiIngestionWfConfigMock = mock(TreasuryOpiIngestionWfConfig.class);

    Mockito.doReturn(treasuryOpiIngestionWfConfigMock)
      .when(applicationContextMock)
      .getBean(TreasuryOpiIngestionWfConfig.class);

    when(treasuryOpiIngestionWfConfigMock.buildTreasuryOpiIngestionActivityStub())
      .thenReturn(treasuryOpiIngestionActivityMock);
    when(treasuryOpiIngestionWfConfigMock.buildNotifyTreasuryToIufClassificationActivityStub())
      .thenReturn(notifyTreasuryToIufClassificationActivityMock);

    return Pair.of(treasuryOpiIngestionActivityMock, treasuryOpiIngestionActivityMock::processFile);
  }

  @Override
  protected TreasuryOpiIngestionWFImpl buildWf() {
    return new TreasuryOpiIngestionWFImpl();
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
