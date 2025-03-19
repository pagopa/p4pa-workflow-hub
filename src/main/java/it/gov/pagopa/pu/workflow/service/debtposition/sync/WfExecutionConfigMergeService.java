package it.gov.pagopa.pu.workflow.service.debtposition.sync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.payhub.activities.dto.debtposition.WfExecutionConfig;
import org.springframework.stereotype.Service;

@Service
public class WfExecutionConfigMergeService {

  private final ObjectMapper objectMapper;

  public WfExecutionConfigMergeService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public WfExecutionConfig mergeWfExecutionConfigs(WfExecutionConfig defaultConfig, WfExecutionConfig wfExecutionConfig) {
    if(defaultConfig == null && wfExecutionConfig == null){
      return null;
    } else if(defaultConfig == null){
      return wfExecutionConfig;
    } else if(wfExecutionConfig == null){
      return clone(defaultConfig);
    } else {

    }
  }

  private WfExecutionConfig clone(WfExecutionConfig wfConfig) {
    try {
      return objectMapper.readValue(objectMapper.writeValueAsString(wfConfig), wfConfig.getClass());
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Cannot clone WfExecutionConfig class " + wfConfig.getClass(), e);
    }
  }
}
