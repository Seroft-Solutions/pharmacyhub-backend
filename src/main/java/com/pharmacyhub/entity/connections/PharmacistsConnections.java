package com.pharmacyhub.entity.connections;

import com.pharmacyhub.constants.StateEnum;
import com.pharmacyhub.entity.Pharmacist;
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
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Entity
@Table(name = "pharmacists_connections")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PharmacistsConnections
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
  @JoinColumn(name = "pharmacist_id")
  private Pharmacist pharmacist;

  public StateEnum getState() {
    return state;
  }

  public void setState(StateEnum state) {
    this.state = state;
  }

    public Long getId() {
        return id;
    }

    public String getNotes() {
        return notes;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public User getUser() {
        return user;
    }

    public Pharmacist getPharmacist() {
        return pharmacist;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setPharmacist(Pharmacist pharmacist) {
        this.pharmacist = pharmacist;
    }
}
