package it.gov.pagopa.pu.workflow.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class DataCipherServiceTest {

  private final DataCipherService service = new DataCipherService("PSW", new JsonMapper());

  @Test
  void testEncrypt() {
    // Given
    String plain = "PLAINTEXT";

    // When
    byte[] cipher = service.encrypt(plain);
    String result = service.decrypt(cipher);

    // Then
    Assertions.assertEquals(plain, result);
  }

  @Test
  void testEncryptObj() {
    // Given
    String plain = "PLAINTEXT";

    // When
    byte[] cipher = service.encryptObj(plain);
    String result = service.decryptObj(cipher, String.class);

    // Then
    Assertions.assertEquals(plain, result);
  }

}
