package com.ecommerce.service.interfaces;

import com.ecommerce.model.dto.ProductCreateDTO;
import com.ecommerce.model.entity.Category;
import com.ecommerce.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
  Product createProduct(ProductCreateDTO dto);

  Product updateProduct(Long id, Product product);

  void deleteProduct(Long id);

  Page<Product> findAllProducts(Pageable pageable);

  Product findProductById(Long id);

  Page<Product> findProductsByCategory(Category category, Pageable pageable);
  // List<Product> searchProducts(ProductSearchCriteria criteria);
}
