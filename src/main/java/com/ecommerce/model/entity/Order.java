package com.ecommerce.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "entities")
public class Order {

    public enum Status {
        PENDING,
        CONFIRMED,
        SHIPPED,
        DELIVERED,
        CANCELLED
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_date")
    @NotNull(message = "La date de commande est obligatoire")
    @PastOrPresent(message = "La date de commande ne peut pas être dans le futur")
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Le statut est obligatoire")
    private Status status;

    @Column(name = "total_amount")
    @NotNull(message = "Le montant total est obligatoire")
    @Min(value = 0, message = "Le montant total doit être supérieur ou égal à 0")
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Size(min = 1, message = "La commande doit contenir au moins un article")
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Version
    private Long version;
}
