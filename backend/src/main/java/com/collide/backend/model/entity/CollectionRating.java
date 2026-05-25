package com.collide.backend.model.entity;

import com.collide.backend.model.id.CollectionRatingId;
import jakarta.persistence.*;

@Entity
@Table(name = "collection_ratings")
public class CollectionRating extends AuditedEntity {
    @EmbeddedId
    private CollectionRatingId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("collectionId")
    @JoinColumn(name = "collection_id")
    private CollectionEntity collection;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(nullable = false)
    private short rating;

    public CollectionRatingId getId() { return id; }
    public void setId(CollectionRatingId id) { this.id = id; }
    public CollectionEntity getCollection() { return collection; }
    public void setCollection(CollectionEntity collection) { this.collection = collection; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public short getRating() { return rating; }
    public void setRating(short rating) { this.rating = rating; }
}
