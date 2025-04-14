package it.gov.pagopa.pu.workflow.config.rest;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(it.gov.pagopa.payhub.activities.config.rest.RestTemplateConfig.class)
public class RestTemplateConfigImport {
}
