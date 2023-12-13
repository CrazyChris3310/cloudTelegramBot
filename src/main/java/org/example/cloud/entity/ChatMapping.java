package org.example.cloud.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_mapping")
@Getter
@Setter
@NoArgsConstructor
public class ChatMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long telegramChat;
    private Long vkChat;

    public ChatMapping(Long telegramChat, Long vkChat) {
        this.telegramChat = telegramChat;
        this.vkChat = vkChat;
    }
}
