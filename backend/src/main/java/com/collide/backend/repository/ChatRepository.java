package com.collide.backend.repository;

import com.collide.backend.model.entity.Chat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRepository extends JpaRepository<Chat, UUID> {
    @Query("""
            select c from Chat c
            where (c.firstUser.id = :first and c.secondUser.id = :second)
               or (c.firstUser.id = :second and c.secondUser.id = :first)
            """)
    Optional<Chat> findPrivateChat(@Param("first") UUID first, @Param("second") UUID second);

    @Query("""
            select c from Chat c
            where c.firstUser.id = :userId or c.secondUser.id = :userId
            order by c.updatedAt desc
            """)
    List<Chat> findForUser(@Param("userId") UUID userId);
}
