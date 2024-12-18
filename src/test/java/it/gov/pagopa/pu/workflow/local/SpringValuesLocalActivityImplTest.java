package it.gov.pagopa.pu.workflow.local;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class SpringValuesLocalActivityImplTest {

    private SpringValuesLocalActivityImpl springValuesLocalActivity;
    private Environment mockEnvironment;

    @BeforeEach
    public void setUp() {
        mockEnvironment = mock(Environment.class);
        springValuesLocalActivity = new SpringValuesLocalActivityImpl();
    }

    @Test
    public void testGetProperties() {
        // Set the values for the properties
        springValuesLocalActivity.setWorkflowQueue("PaymentsReportingIngestionWF");
        springValuesLocalActivity.setStartToCloseTimeoutInSeconds(60);
        springValuesLocalActivity.setRetryInitialIntervalInMillis(1000);
        springValuesLocalActivity.setRetryBackoffCoefficient(1.1);
        springValuesLocalActivity.setRetryMaximumAttempts(10);

        // Call the method
        HashMap<String, String> properties = springValuesLocalActivity.getProperties();

        // Verify the properties
        assertEquals("PaymentsReportingIngestionWF", properties.get("workflowQueue"));
        assertEquals("60", properties.get("startToCloseTimeoutInSeconds"));
        assertEquals("1000", properties.get("retryInitialIntervalInMillis"));
        assertEquals("1.1", properties.get("retryBackoffCoefficient"));
        assertEquals("10", properties.get("retryMaximumAttempts"));
    }
}
