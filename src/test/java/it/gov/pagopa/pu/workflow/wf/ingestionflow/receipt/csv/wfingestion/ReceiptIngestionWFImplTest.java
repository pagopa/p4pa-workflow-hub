package it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.csv.wfingestion;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.receipt.ReceiptIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.receipt.ReceiptIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFTest;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.csv.config.ReceiptIngestionWfConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
class ReceiptIngestionWFImplTest extends BaseIngestionFlowFileWFTest<ReceiptIngestionFlowFileResult> {
  @Mock
  private ReceiptIngestionActivity receiptIngestionActivityMock;

  private ReceiptIngestionWFImpl wf;

  @Override
  protected Pair<Object, Function<Long, ReceiptIngestionFlowFileResult>> configureIngestionFlowFileProcessorActivityMock(ApplicationContext applicationContextMock) {
    ReceiptIngestionWfConfig receiptIngestionWfConfigMock = Mockito.mock(ReceiptIngestionWfConfig.class);

    Mockito.doReturn(receiptIngestionWfConfigMock).when(applicationContextMock)
      .getBean(ReceiptIngestionWfConfig.class);

    Mockito.when(receiptIngestionWfConfigMock.buildReceiptIngestionActivityStub())
      .thenReturn(receiptIngestionActivityMock);

    return Pair.of(receiptIngestionActivityMock, receiptIngestionActivityMock::processFile);
  }

  @Override
  protected BaseIngestionFlowFileWFImpl<ReceiptIngestionFlowFileResult> buildWf() {
    return new ReceiptIngestionWFImpl();
  }

  @Override
  protected ReceiptIngestionFlowFileResult buildExpectedIngestionFlowFileResult() {
    return new ReceiptIngestionFlowFileResult();
  }
}
