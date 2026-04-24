package com.gyanMonteiro.gesmed.entity;

import com.gyanMonteiro.gesmed.dto.request.ClientAddressRequestDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "client_addresses")
public class ClientAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String number;

    @Column(nullable = false)
    private String complement;

    @Column(nullable = false)
    private String neighborhood;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(name = "zip_code", nullable = false)
    private String zipCode;

    @Column(name = "is_main", nullable = false, updatable = false)
    private boolean isMain;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;


    public static ClientAddress from(ClientAddressRequestDTO dto, Client client){
        ClientAddress address = new ClientAddress();
        address.setLabel(dto.label());
        address.setStreet(dto.street());
        address.setNumber(dto.number());
        address.setComplement(dto.complement());
        address.setNeighborhood(dto.neighborhood());
        address.setCity(dto.city());
        address.setState(dto.state());
        address.setZipCode(dto.zipCode());
        address.setMain(dto.isMain());
        address.setClient(client);
        return address;
    }
}
