package com.ecommerce.mapper;

import com.ecommerce.config.MapstructConfig.HibernateAwareMapperConfig;
import com.ecommerce.model.dto.OrderCreateDTO;
import com.ecommerce.model.dto.OrderDTO;
import com.ecommerce.model.entity.Order;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    componentModel = "spring",
    uses = {OrderItemMapper.class},
    config = HibernateAwareMapperConfig.class)
public interface OrderMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "items", ignore = true)
  Order toEntity(OrderCreateDTO orderDTO);

  OrderDTO toDto(Order order);

  List<OrderDTO> toDtoList(List<Order> orders);
}
