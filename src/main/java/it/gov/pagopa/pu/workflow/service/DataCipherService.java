package it.gov.pagopa.pu.workflow.service;

import it.gov.pagopa.payhub.activities.util.AESUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Service
public class DataCipherService {

    private final String encryptPsw;
    private final JsonMapper jsonMapper;

    public DataCipherService(
            @Value("${data-cipher.encrypt-psw}") String encryptPsw,
            JsonMapper jsonMapper
    ) {
        this.encryptPsw = encryptPsw;
        this.jsonMapper = jsonMapper;
    }

    public byte[] encrypt(String plainText) {
        return AESUtils.encrypt(encryptPsw, plainText);
    }

    public String decrypt(byte[] cipherData) {
        return AESUtils.decrypt(encryptPsw, cipherData);
    }

    public <T> byte[] encryptObj(T obj) {
        try {
            return encrypt(jsonMapper.writeValueAsString(obj));
        } catch (JacksonException e) {
            throw new IllegalStateException("Cannot serialize object as JSON", e);
        }
    }

    public <T> T decryptObj(byte[] cipherData, Class<T> clazz) {
        try {
            return jsonMapper.readValue(decrypt(cipherData), clazz);
        } catch (JacksonException e) {
            throw new IllegalStateException("Cannot deserialize object as JSON", e);
        }
    }
}
