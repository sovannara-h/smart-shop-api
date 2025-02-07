package com.ecommerce.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.exception.OrderException;
import com.ecommerce.model.dto.ApiResponse;
import com.ecommerce.model.entity.Order;
import com.ecommerce.model.entity.Order.Status;
import com.ecommerce.service.impl.OrderServiceImpl;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description =  "API de gestion des commandes")
@Validated
@Slf4j
public class OrderController {

    private final OrderServiceImpl orderServiceImpl;

    public OrderController(OrderServiceImpl orderServiceImpl) {
        this.orderServiceImpl = orderServiceImpl;
    }

    @Operation(summary = "Créer une nouvelle commande")
    @Timed(value = "order.creation.time", description = "Temps de création d'une commande")
    @PostMapping
    @RateLimiter(name = "createOrder")
    public ResponseEntity<ApiResponse<Order>> createOrder(@Valid @RequestBody Order order) {
        try {
            Order created = orderServiceImpl.createOrder(order);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, created, "Commande créé avec succès", null, LocalDateTime.now()));
        } catch (Exception e) {
            log.error("Erreur lors de la création du produit", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, null, null, e.getMessage(), LocalDateTime.now()));
        }
    }

    @Operation(summary = "Récupérer tous les produits", description = "Retourne une liste paginée de tous les produits")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Order>>> getAllOrders(
        @Parameter(description = "Numéro de page (commence à 0)")
        @RequestParam(defaultValue =  "0") int page,
        @Parameter(description = "Nombre d'éléments par page")
        @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Page<Order> orders = orderServiceImpl.findAllOrders(PageRequest.of(page, size));
            return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                .body(new ApiResponse<>(true, orders, "Commandes récupérés avec succès", null, LocalDateTime.now()));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des commandes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, null, "Erreur serveur: " + e.getMessage(), LocalDateTime.now()));
        }
    }

    @Operation(summary = "Récupérer les commandes entre deux dates")
    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<Page<Order>>> getOrderBetweenDate(
        @Parameter(description = "Date de début")
        @RequestParam LocalDateTime startDate,
        @Parameter(description = "Date de fin")
        @RequestParam LocalDateTime endDate,
        @Parameter(description = "Numéro de page")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Taille de la page")
        @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Page<Order> orders = orderServiceImpl.findOrdersBetweenDates(startDate, endDate, PageRequest.of(page, size));
            return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                .body(new ApiResponse<>(true, orders, "Commandes récupérées avec succès", null, LocalDateTime.now()));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des commandes par date", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, null, e.getMessage(), LocalDateTime.now()));
        }
    }

    @Operation(summary = "Récupérer les commandes par statut")
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<Order>>> getOrdersByStatus(
        @Parameter(description = "Statut de la commande")
        @PathVariable Status status,
        @Parameter(description = "Numéro de page")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Taille de la page")
        @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Page<Order> orders = orderServiceImpl.findOrdersByStatus(status, PageRequest.of(page, size));
            return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                .body(new ApiResponse<>(true, orders, "Commandes récupérées avec succès", null, LocalDateTime.now()));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des commandes par statut", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, null, e.getMessage(), LocalDateTime.now()));
        }
    }

    @Operation(summary = "Récupérer une commande", description = "Retourne un produit par son ID")
    @GetMapping("/{id}")
    @Cacheable(value = "orders", key = "#id")
    @RateLimiter(name = "getOrder")
    public ResponseEntity<ApiResponse<Order>> getOrderByID(
        @Parameter(description = "ID de la commande")
        @PathVariable @Positive Long id
    ) {
        log.debug("Recherche du produit avec l'ID: {}", id);
        
        try {
            Order order = orderServiceImpl.findOrderById(id);
            return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                .eTag(String.valueOf(order.getVersion()))
                .body(new ApiResponse<>(true, order, "Commande récupérés avec succès", null, LocalDateTime.now()));
                
        } catch (OrderException e) {
            log.warn("Commande non trouvé: {}", id);
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la commande: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Mettre à jour le statut d'une commande")
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Order>> updateOrderStatus(
        @Parameter(description = "ID de la commande")
        @PathVariable @Positive Long id,
        @Parameter(description = "Nouveau statut")
        @RequestParam Status newStatus
    ) {
        try {
            Order updatedOrder = orderServiceImpl.updateOrderStatus(id, newStatus);
            return ResponseEntity.ok()
                .body(new ApiResponse<>(true, updatedOrder, "Statut de la commande mis à jour avec succès", null, LocalDateTime.now()));
        } catch (OrderException e) {
            log.warn("Erreur lors de la mise à jour du statut de la commande: {}", id, e);
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, null, null, e.getMessage(), LocalDateTime.now()));
        } catch (Exception e) {
            log.error("Erreur serveur lors de la mise à jour du statut de la commande: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, null, "Erreur serveur", LocalDateTime.now()));
        }
    }

    @Operation(summary = "Supprimer une commande")
    @DeleteMapping("/{id}")
    @CacheEvict(value = "orders", key = "#id")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(
        @Parameter(description = "ID de la commande")
        @PathVariable @Positive Long id
    ) {
        try {
            orderServiceImpl.deleteOrder(id);
            return ResponseEntity.ok()
                .body(new ApiResponse<>(true, null, "Commande supprimée avec succès", null, LocalDateTime.now()));
        } catch (OrderException e) {
            log.warn("Erreur lors de la suppression de la commande: {}", id, e);
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, null, null, e.getMessage(), LocalDateTime.now()));
        } catch (Exception e) {
            log.error("Erreur serveur lors de la suppression de la commande: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, null, null, "Erreur serveur", LocalDateTime.now()));
        }
    }

    @Operation(summary = "Récupérer les commandes par mois")
    @GetMapping("/month")
    public ResponseEntity<ApiResponse<List<Order>>> getOrdersPerMonth(
        @Parameter(description = "Mois")
        @RequestParam @Positive Integer month
        ) {
            try {
                List<Order> orders =  orderServiceImpl.findOrdersByMonth(month);
                return ResponseEntity.ok()
                    .body(new ApiResponse<>(true,  orders, "Commandes récupérer avec succês", null, LocalDateTime.now()));
            } catch (Exception e) {
                // TODO: handle exception
                log.error("Erreur serveur lors de la suppression de la commande: {}", month, e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, null, "Erreur serveur", LocalDateTime.now()));
            }
    }

}
