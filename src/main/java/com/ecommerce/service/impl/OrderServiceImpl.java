package com.ecommerce.service.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.exception.OrderException;
import com.ecommerce.model.entity.Order;
import com.ecommerce.model.entity.Order.Status;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.service.interfaces.OrderService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public Order createOrder(Order order) {
        log.debug("Création d'une nouvelle commande");

        try {
            return orderRepository.save(order);
        }  catch (Exception e) {
            log.error("Erreur inattendue lors de la création de la commande", e);
            throw new OrderException("Erreur lors de la création de la commande: " + e.getMessage());
        }
    }

    @Override
    public Order findOrderById(Long id) {
        if(id <= 0) {
            throw new IllegalArgumentException("L'ID doit être positif");
        }
        return orderRepository.findById(id).orElseThrow(() -> new OrderException("id"));
    }

    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    public Page<Order> findAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    

    @Override
    public Page<Order> findOrdersByStatus(Status status, Pageable pageable) {
        log.debug("Recherche des commandes avec le statut: {}", status);
        try {
            return orderRepository.findByStatus(status, pageable);
        } catch (Exception e) {
            log.error("Erreur lors de la recherche des commandes par statut", e);
            throw new OrderException("Erreur lors de la recherche des commandes par statut: " + e.getMessage());
        }
    }

    @Override
    public Page<Order> findOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.debug("Recherche des commandes entre {} et {}", startDate, endDate);
        
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Les dates de début et de fin ne peuvent pas être nulles");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La date de début doit être antérieure à la date de fin");
        }
        
        try {
            return orderRepository.findBetweenDates(startDate, endDate, pageable);
        } catch (Exception e) {
            log.error("Erreur lors de la recherche des commandes entre deux dates", e);
            throw new OrderException("Erreur lors de la recherche des commandes entre deux dates: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long id, Status newStatus) {
        log.debug("Mise à jour du statut de la commande {} vers {}", id, newStatus);
        
        if (id <= 0) {
            throw new IllegalArgumentException("L'ID doit être positif");
        }
        
        if (newStatus == null) {
            throw new IllegalArgumentException("Le nouveau statut ne peut pas être null");
        }
        
        try {
            Order order = findOrderById(id);
            order.setStatus(newStatus);
            return orderRepository.save(order);
        } catch (OrderException e) {
            log.error("Commande non trouvée pour la mise à jour du statut - id: {}", id);
            throw new OrderException("Commande non trouvée pour la mise à jour du statut: " + e.getMessage());
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du statut de la commande", e);
            throw new OrderException("Erreur lors de la mise à jour du statut de la commande: " + e.getMessage());
        }
    }
    
}
