package com.pharmacyhub.entity.connections;

import com.pharmacyhub.constants.StateEnum;
import com.pharmacyhub.entity.PharmacyManager;
import com.pharmacyhub.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pharmacy_manager_connections")
public class PharmacyManagerConnections
{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Builder.Default
  private StateEnum state = StateEnum.READY_TO_CONNECT;

  private String notes;

  @CreationTimestamp
  @Column(updatable = false, name = "created_at")
  private Date createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Date updatedAt;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne
  @JoinColumn(name = "pharmacy_manager_id")
  private PharmacyManager pharmacyManager;
  
  // Explicit getters and setters for relationship fields
  public User getUser() {
    return user;
  }
  
  public void setUser(User user) {
    this.user = user;
  }
  
  public PharmacyManager getPharmacyManager() {
    return pharmacyManager;
  }
  
  public void setPharmacyManager(PharmacyManager pharmacyManager) {
    this.pharmacyManager = pharmacyManager;
  }
  
  public Long getId() {
    return id;
  }
  
  public StateEnum getState() {
    return state;
  }
}
