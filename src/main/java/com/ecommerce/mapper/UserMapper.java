package com.ecommerce.mapper;

import com.ecommerce.config.MapstructConfig.HibernateAwareMapperConfig;
import com.ecommerce.model.dto.SignUpRequest;
import com.ecommerce.model.dto.UserDto;
import com.ecommerce.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", config = HibernateAwareMapperConfig.class)
public interface UserMapper {

  UserDto toDto(User user);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  User toEntity(SignUpRequest signUpRequest);
}
