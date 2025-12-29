package com.billing.ipd.entity;

import java.time.LocalDateTime;

import com.billing.model.IPDBillingDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "ipd_room_allocation")
@Data
public class IPDRoomAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ipd_billing_details_id")
    @JsonIgnore
    private IPDBillingDetails ipdBillingDetails;

    private String roomNumber;
    private Integer bedNumber;
    private Double bedPricePerDay;
//    private Integer days;
    private Long daysAdmitted;
    private Double totalRoomCharges;

    private LocalDateTime allocationDate;
    private LocalDateTime releaseDate; // null = current

    // getters/setters
}