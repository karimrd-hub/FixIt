package com.example.fixit.module.store.entity;

import com.example.fixit.common.entity.AuditableEntity;
import com.example.fixit.module.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "store_staff",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_store_staff_user_store",
        columnNames = {"user_id", "store_id"}
    )
)
public class StoreStaff extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_store_staff_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false, foreignKey = @ForeignKey(name = "fk_store_staff_store"))
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(name = "store_role", nullable = false, length = 20)
    private StoreRole storeRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StoreStaffStatus status = StoreStaffStatus.ACTIVE;
}
