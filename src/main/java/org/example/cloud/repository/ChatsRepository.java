package org.example.cloud.repository;

import org.example.cloud.entity.ChatMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatsRepository extends JpaRepository<ChatMapping, Long> {

    @Query("select c.vkChat from ChatMapping c where c.telegramChat = ?1")
    List<Long> findVkChatByTelegramChat(Long chatId);

    @Query("select c.telegramChat from ChatMapping c where c.vkChat = ?1")
    List<Long> findTelegramChatByVkChat(Long chatId);

}
