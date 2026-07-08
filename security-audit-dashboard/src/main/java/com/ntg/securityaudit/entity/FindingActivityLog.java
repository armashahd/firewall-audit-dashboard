package com.ntg.securityaudit.entity;

import com.ntg.securityaudit.enums.FindingActionType;
import com.ntg.securityaudit.enums.FindingStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "finding_activity_logs")
public class FindingActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "finding_id", nullable = false)
    private Finding finding;

    private String username;

    @Enumerated(EnumType.STRING)
    private FindingActionType actionType;

    @Enumerated(EnumType.STRING)
    private FindingStatus oldStatus;

    @Enumerated(EnumType.STRING)
    private FindingStatus newStatus;

    @Column(length = 3000)
    private String comment;

    private LocalDateTime createdAt;

    public FindingActivityLog() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Finding getFinding() {
        return finding;
    }

    public void setFinding(Finding finding) {
        this.finding = finding;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public FindingActionType getActionType() {
        return actionType;
    }

    public void setActionType(FindingActionType actionType) {
        this.actionType = actionType;
    }

    public FindingStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(FindingStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public FindingStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(FindingStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
