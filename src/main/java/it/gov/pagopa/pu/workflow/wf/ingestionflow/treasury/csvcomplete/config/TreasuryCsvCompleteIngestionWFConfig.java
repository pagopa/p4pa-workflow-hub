package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.csvcomplete.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.treasury.csvcomplete.TreasuryCsvCompleteIngestionActivity;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.config.TreasuryIngestionWfConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.treasury-csv-complete-ingestion")
public class TreasuryCsvCompleteIngestionWFConfig extends TreasuryIngestionWfConfig {

    public TreasuryCsvCompleteIngestionActivity buildTreasuryCsvCompleteIngestionActivityStub() {
        return Workflow.newActivityStub(TreasuryCsvCompleteIngestionActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
    }

}
