package com.example.racketbackend.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Entity
@Table(
        name = "BranchInventory",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_branch_inventory",
                columnNames = {
                        "branch_id",
                        "racket_id"
                }
        )
)
public class BranchInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "racket_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Racket racket;

    private Integer stockQuantity = 0;
    private Integer reservedQuantity = 0;

    // Prevents simultaneous stock updates overwriting each other.
    @Version
    private Long version;

    public int getAvailableQuantity() {
        int stock =
                stockQuantity == null
                        ? 0
                        : stockQuantity;

        int reserved =
                reservedQuantity == null
                        ? 0
                        : reservedQuantity;

        return Math.max(
                stock - reserved,
                0
        );
    }
}