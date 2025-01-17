package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.activity;

import it.gov.pagopa.pu.workflow.wf.classification.iuf.IufClassificationWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyTreasurySignalDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotifyTreasuryToIufClassificationActivityTest {

    @Mock
    private IufClassificationWFClient iufClassificationWFClientMock;

    private NotifyTreasuryToIufClassificationActivityImpl notifyTreasuryToIufClassificationActivity;

    @BeforeEach
    void setUp() {
        notifyTreasuryToIufClassificationActivity = new NotifyTreasuryToIufClassificationActivityImpl(iufClassificationWFClientMock);
    }

    @Test
    void testSignalIufClassificationWithStart() {
        // Given
        Long organizationId = 1L;
        String iuf = "iuf-123";
        String treasuryId = "treasury-456";

        IufClassificationNotifyTreasurySignalDTO expectedSignalDTO = IufClassificationNotifyTreasurySignalDTO.builder()
                .organizationId(organizationId)
                .iuf(iuf)
                .treasuryId(treasuryId)
                .build();

        // When
        notifyTreasuryToIufClassificationActivity.signalIufClassificationWithStart(organizationId, iuf, treasuryId);

        // Then
        verify(iufClassificationWFClientMock).notifyTreasury(expectedSignalDTO);

    }
}
