package com.pharmacyhub.entity.connections;

import com.pharmacyhub.constants.StateEnum;
import com.pharmacyhub.entity.Proprietor;
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
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "proprietors_connections")
public class ProprietorsConnections
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
  @JoinColumn(name = "proprietor_id")
  private Proprietor proprietor;
  
  // Explicit getters and setters for relationship fields
  public User getUser() {
    return user;
  }
  
  public void setUser(User user) {
    this.user = user;
  }
  
  public Proprietor getProprietor() {
    return proprietor;
  }
  
  public void setProprietor(Proprietor proprietor) {
    this.proprietor = proprietor;
  }
  
  public Long getId() {
    return id;
  }
  
  public StateEnum getState() {
    return state;
  }
}
