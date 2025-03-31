package it.gov.pagopa.pu.workflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DataCipherServiceTest {

  private final DataCipherService service = new DataCipherService("PSW", new ObjectMapper());

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
