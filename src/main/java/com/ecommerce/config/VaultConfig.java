package com.ecommerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;

@Configuration
public class VaultConfig {
  @Value("${vault.token}")
  private String vaultToken;

  @Value("${vault.uri}")
  private String vaultUri;

  @Bean
  public VaultTemplate vaultTemplate() {
    VaultEndpoint endpoint = VaultEndpoint.from(vaultUri);
    ClientAuthentication clientAuthentication = new TokenAuthentication(vaultToken);
    return new VaultTemplate(endpoint, clientAuthentication);
  }
}
