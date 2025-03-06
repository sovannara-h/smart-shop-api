package com.ecommerce.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.exception.OrderException;
import com.ecommerce.exception.UserNotFoundException;
import com.ecommerce.model.dto.OrderCreateDTO;
import com.ecommerce.model.entity.Order;
import com.ecommerce.model.entity.Order.Status;
import com.ecommerce.model.entity.User;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.interfaces.OrderService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderServiceImpl(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Order createOrder(OrderCreateDTO orderDTO) {
        Order order = new Order();
        order.setCustomerEmail(orderDTO.getEmail());
        

        if (orderDTO.getUserId() != null) {
            User user = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));
            order.setUser(user);
        }
        
        order.setShippingName(orderDTO.getShippingName());
        order.setShippingAddress(orderDTO.getShippingAddress());
        order.setShippingPhone(orderDTO.getShippingPhone());
        
        
        return orderRepository.save(order);
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
    public List<Order> findOrdersByMonth(Integer month) {
        if (month < 0) {
            throw new IllegalArgumentException("Le mois ne peut pas être plus petit que 0");
        }
        
        if (month > 12) {
            throw new IllegalArgumentException("Le mois ne peut pas être supérieur à 12");
        }
        
        try {
            LocalDateTime startOfMonth = LocalDateTime.now()
                .withMonth(month)
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0);
                
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
            
            return orderRepository.findBetweenDates(startOfMonth, endOfMonth, Pageable.unpaged())
                .getContent();
                
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des commandes par mois", e);
            throw new OrderException("Erreur lors de la récupération des commandes par mois: " + e.getMessage());
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
