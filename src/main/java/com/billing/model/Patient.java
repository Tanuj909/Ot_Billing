


package com.billing.model;

import java.time.LocalDate;
import com.billing.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "patient")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
    // external id coming from User Management
    @Column(name = "external_id", unique = true)
    private Long externalId;
	
    private String patientName;
    private String gender;
    private int age;
    private String contactNumber;
    private String address;

    private LocalDate admissionDate;
//    private LocalDate dischargeDate;
    
    @ManyToOne
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;
    
    @Enumerated(EnumType.STRING)
    private Role role;
    
    @PrePersist
    public void prePersist() {
        if (admissionDate == null) {
            admissionDate = LocalDate.now();
        }
    }
}
