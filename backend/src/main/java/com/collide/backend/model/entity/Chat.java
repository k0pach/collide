package com.collide.backend.model.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "chats")
public class Chat extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "first_user_id", nullable = false)
    private AppUser firstUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "second_user_id", nullable = false)
    private AppUser secondUser;

    @Column(name = "first_user_last_read_at")
    private OffsetDateTime firstUserLastReadAt;

    @Column(name = "second_user_last_read_at")
    private OffsetDateTime secondUserLastReadAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AppUser getFirstUser() { return firstUser; }
    public void setFirstUser(AppUser firstUser) { this.firstUser = firstUser; }
    public AppUser getSecondUser() { return secondUser; }
    public void setSecondUser(AppUser secondUser) { this.secondUser = secondUser; }
    public OffsetDateTime getFirstUserLastReadAt() { return firstUserLastReadAt; }
    public void setFirstUserLastReadAt(OffsetDateTime firstUserLastReadAt) { this.firstUserLastReadAt = firstUserLastReadAt; }
    public OffsetDateTime getSecondUserLastReadAt() { return secondUserLastReadAt; }
    public void setSecondUserLastReadAt(OffsetDateTime secondUserLastReadAt) { this.secondUserLastReadAt = secondUserLastReadAt; }
}
