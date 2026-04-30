package it.gov.pagopa.pu.workflow.config.temporal;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentracing.Tracer;
import io.temporal.common.converter.DataConverter;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.common.converter.JacksonJsonPayloadConverter;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "temporal")
@Data
public class TemporalConfig {

  @NestedConfigurationProperty
  private TemporalTimeoutsConfig timeouts;

  @Bean
  public Tracer globalTracer(OpenTelemetry openTelemetry){
    return OpenTracingShim.createTracerShim(openTelemetry);
  }

  @Bean
  public DataConverter temporalDataConverter(ObjectMapper objectMapper){
    return DefaultDataConverter.newDefaultInstance()
      .withPayloadConverterOverrides(new JacksonJsonPayloadConverter(objectMapper));
  }

  @Getter
  @Setter
  public static class TemporalTimeoutsConfig {
    private long systemInfo;
    private long rpcLongPoll;
    private long rpcQuery;
    private long rpcGeneric;
  }
}
