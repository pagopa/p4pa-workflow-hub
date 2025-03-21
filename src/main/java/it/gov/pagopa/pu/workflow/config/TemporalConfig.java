package it.gov.pagopa.pu.workflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.temporal.common.converter.DataConverter;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.common.converter.JacksonJsonPayloadConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalConfig {
  @Bean
  public DataConverter temporalDataConverter(ObjectMapper objectMapper){
    return DefaultDataConverter.newDefaultInstance()
      .withPayloadConverterOverrides(new JacksonJsonPayloadConverter(objectMapper));
  }
}
