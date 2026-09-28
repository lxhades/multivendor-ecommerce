package com.auth_service.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_addresses")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receiver_name", nullable = false)
    private String receiverName;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String detail;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

}