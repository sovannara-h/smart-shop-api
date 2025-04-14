package com.ecommerce.config;

import org.hibernate.Hibernate;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration pour MapStruct afin de gérer correctement les proxies Hibernate */
@Configuration
public class MapstructConfig {

  @Bean
  public HibernateProxyHelper hibernateProxyHelper() {
    return new HibernateProxyHelper();
  }

  /** Helper pour déproxifier les objets Hibernate */
  public static class HibernateProxyHelper {

    /**
     * Déproxifie un objet Hibernate si nécessaire
     *
     * @param <T> Type de l'entité
     * @param entity L'entité qui peut être un proxy
     * @return L'entité déproxifiée ou l'entité originale si elle n'est pas un proxy
     */
    public <T> T deproxy(T entity) {
      if (entity == null) {
        return null;
      }
      return (T) Hibernate.unproxy(entity);
    }
  }

  @MapperConfig(
      componentModel = "spring",
      unmappedTargetPolicy = ReportingPolicy.IGNORE,
      uses = {HibernateProxyHelper.class})
  public interface HibernateAwareMapperConfig {}
}
