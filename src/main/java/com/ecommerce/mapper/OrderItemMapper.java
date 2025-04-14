package com.ecommerce.mapper;

import com.ecommerce.config.MapstructConfig.HibernateAwareMapperConfig;
import com.ecommerce.model.dto.OrderItemDTO;
import com.ecommerce.model.entity.OrderItem;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", config = HibernateAwareMapperConfig.class)
public interface OrderItemMapper {

  @Mapping(target = "productName", source = "product.name")
  @Mapping(target = "productId", source = "product.id")
  @Mapping(target = "productVariantId", ignore = true)
  @Mapping(target = "variantName", ignore = true)
  OrderItemDTO toDto(OrderItem orderItem);

  List<OrderItemDTO> toDtoList(List<OrderItem> items);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "order", ignore = true)
  @Mapping(target = "product", ignore = true)
  OrderItem toEntity(OrderItemDTO orderItemDto);
}
