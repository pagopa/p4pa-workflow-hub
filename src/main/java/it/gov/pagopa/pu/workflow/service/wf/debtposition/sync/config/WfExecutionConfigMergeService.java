package it.gov.pagopa.pu.workflow.service.wf.debtposition.sync.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.WfExecutionConfig;
import it.gov.pagopa.pu.workflow.exception.custom.IllegalStateBusinessException;
import it.gov.pagopa.pu.workflow.utilities.ErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class WfExecutionConfigMergeService {

  private final ObjectMapper objectMapper;

  public WfExecutionConfigMergeService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public WfExecutionConfig merge(WfExecutionConfig defaultConfig, WfExecutionConfig wfExecutionConfig) {
    if (defaultConfig == null && wfExecutionConfig == null) {
      return null;
    } else if (defaultConfig == null) {
      if(wfExecutionConfig instanceof GenericWfExecutionConfig){
        return wfExecutionConfig;
      } else {
        log.info("Ignoring provided wfExecutionConfig! Not generic type: {}", wfExecutionConfig.getClass().getSimpleName());
        return null;
      }
    } else if (wfExecutionConfig == null) {
      return clone(defaultConfig);
    } else {
      return mergeInner(defaultConfig, wfExecutionConfig);
    }
  }

  private WfExecutionConfig clone(WfExecutionConfig wfConfig) {
    try {
      return objectMapper.readValue(objectMapper.writeValueAsString(wfConfig), wfConfig.getClass());
    } catch (JsonProcessingException e) {
      throw new IllegalStateBusinessException(ErrorCodeConstants.ERROR_CODE_INVALID_EXECUTION_CONFIG, "Cannot clone WfExecutionConfig class " + wfConfig.getClass().getSimpleName(), e);
    }
  }

  private WfExecutionConfig mergeInner(WfExecutionConfig defaultConfig, WfExecutionConfig wfExecutionConfig) {
    try {
      if(!wfExecutionConfig.getClass().equals(defaultConfig.getClass())){
        log.info("Ignoring provided wfExecutionConfig! Not expected type: expected: {}, provided {}", defaultConfig.getClass().getSimpleName(), wfExecutionConfig.getClass().getSimpleName());
        return clone(defaultConfig);
      }

      JsonNode defaultValues = objectMapper.convertValue(defaultConfig, JsonNode.class);
      return objectMapper.convertValue(
        objectMapper.readerForUpdating(defaultValues)
          .readValue(objectMapper.writeValueAsString(wfExecutionConfig)),
        defaultConfig.getClass());
    } catch (IOException e) {
      throw new IllegalStateBusinessException(ErrorCodeConstants.ERROR_CODE_INVALID_EXECUTION_CONFIG, "Cannot merge WfExecutionConfig from class " + wfExecutionConfig.getClass().getSimpleName() + " to class " + defaultConfig.getClass().getSimpleName(), e);
    }
  }
}
