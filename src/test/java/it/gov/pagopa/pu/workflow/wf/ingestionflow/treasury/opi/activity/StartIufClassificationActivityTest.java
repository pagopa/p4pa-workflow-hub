package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.activity;

import it.gov.pagopa.pu.workflow.wf.classification.iuf.IufClassificationWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyTreasurySignalDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StartIufClassificationActivityTest {

    @Mock
    private IufClassificationWFClient iufClassificationWFClientMock;

    private StartIufClassificationActivityImpl startIufClassificationActivity;

    @BeforeEach
    void setUp() {
        startIufClassificationActivity = new StartIufClassificationActivityImpl(iufClassificationWFClientMock);
    }

    @Test
    void testSignalIufClassificationWithStart() {
        // Given
        Long organizationId = 1L;
        String iuf = "iuf-123";
        String treasuryId = "treasury-456";

        // When
        startIufClassificationActivity.signalIufClassificationWithStart(organizationId, iuf, treasuryId);

        // Then
// the following line should go on Given block
     IufClassificationNotifyTreasurySignalDTO expectedSignalDTO = IufClassificationNotifyTreasurySignalDTO.builder()
       .organizationId(organizationId)
       .iuf(iuf)
       .treasuryId(treasuryId)
       .build();
       
     verify(iufClassificationWFClientMock).notifyTreasury(expectedSignalDTO);
    
    }
}
