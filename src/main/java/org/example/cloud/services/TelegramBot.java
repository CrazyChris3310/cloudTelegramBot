package org.example.cloud.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.SneakyThrows;
import org.example.cloud.dto.BasicMessage;
import org.example.cloud.dto.ChatCreationRequest;
import org.example.cloud.entity.ChatMapping;
import org.example.cloud.repository.ChatsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;

import static org.example.cloud.dto.MediaContentType.*;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final ChatsRepository chatsRepository;

    private final MessageService messageService;

    private final Map<Long, ChatCreationRequest> requestMap = new HashMap<>();
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final long APPROVE_EXPECTATION_LIMIT = 5*60*1000;

    public TelegramBot(@Value("${telegram.bot.token}") String botToken, ChatsRepository chatsRepository,
                       MessageService messageService) throws TelegramApiException {
        super(botToken);
        this.chatsRepository = chatsRepository;
        this.messageService = messageService;
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(this);
        System.out.println("Telegram bot started");
    }

    public void handleCommand(Message message) throws MalformedURLException {
        String[] args = message.getText().split(" ");
        if (args[0].startsWith("/help")) {
            String helpMessage = "/help - помощь\n" +
            "/setchat [chatId] - запросить досутп к vk чату с идентификатором chatId\n" +
            "/approve_chat uuid - подвердить кодом из vk, что у вас есть доступ к запрошенному чату\n" +
            "/say_id - узнать id текущего чата";
            BasicMessage basicMessage = new BasicMessage(TEXT, message.getChatId());
            basicMessage.setText(helpMessage);
            sendMessage(basicMessage);
        } else if (args[0].startsWith("/say_id")) {
            BasicMessage basicMessage = new BasicMessage(TEXT, message.getChatId());
            basicMessage.setText(message.getChatId().toString());
            sendMessage(basicMessage);
        }
        else if (args[0].startsWith("/setchat")) {
            String text = args[1];
            long chatId;
            try {
                chatId = Long.parseLong(text);
            } catch (Exception e) {
                BasicMessage errorMessage = new BasicMessage(TEXT, message.getChatId());
                errorMessage.setText("ChaId should be a number");
                sendMessage(errorMessage);
                return;
            }
            String code = UUID.randomUUID().toString();
            requestMap.put(message.getChatId(), new ChatCreationRequest(code, System.currentTimeMillis(), chatId));
            BasicMessage codeMessage = new BasicMessage(TEXT, Long.parseLong(text));
            codeMessage.setText("A request to sync this chat to telegram was registered\nApproval code: " + code);
            messageService.send(codeMessage);
        } else if (args[0].startsWith("/approve_chat")) {
            String text = args[1];
            ChatCreationRequest chatCreationRequest = requestMap.get(message.getChatId());
            BasicMessage basicMessage = new BasicMessage(TEXT, message.getChatId());
            if (chatCreationRequest != null) {
                if (System.currentTimeMillis() < chatCreationRequest.timestamp() + APPROVE_EXPECTATION_LIMIT) {
                    if (chatCreationRequest.hash().equals(text)) {
                        requestMap.remove(message.getChatId());
                        chatsRepository.save(new ChatMapping(message.getChatId(), chatCreationRequest.vkChat()));
                        basicMessage.setText("Chats are successfully synced");
                    } else {
                        basicMessage.setText("Invalid approval code");
                    }
                } else {
                    requestMap.remove(message.getChatId());
                    basicMessage.setText("Time limit exceeded");
                }
            } else {
                basicMessage.setText("No chats are waiting to be synced");
            }
            sendMessage(basicMessage);
        }
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.getMessage() == null) {
            return;
        }

        var msg = update.getMessage();
        var user = msg.getFrom();

        if (msg.isCommand()) {
            handleCommand(msg);
            return;
        }

        BasicMessage message = new BasicMessage();
        message.setFullname(user.getFirstName() + " " + user.getLastName());

        if (msg.getPhoto() != null) {
            message.setType(PHOTO);
            String fileId = msg.getPhoto().stream().max(Comparator.comparingInt(a -> a.getHeight() * a.getWidth())).get().getFileId();
            GetFile getFile = GetFile.builder().fileId(fileId).build();
            File photoFile = execute(getFile);
            String fileUrl = photoFile.getFileUrl(getBotToken());
            message.setUrl(fileUrl);
            message.setText(msg.getText());
        } else if (msg.getVideoNote() != null) {
            message.setType(VIDEO);
            String fileId = msg.getVideoNote().getFileId();
            GetFile getFile = GetFile.builder().fileId(fileId).build();
            File videoFile = execute(getFile);
            String fileUrl = videoFile.getFileUrl(getBotToken());
            message.setUrl(fileUrl);
            message.setText(msg.getText());
        } else if (msg.getVideo() != null) {
            message.setType(VIDEO);
            String fileId = msg.getVideo().getFileId();
            GetFile getFile = GetFile.builder().fileId(fileId).build();
            File videaoFile = execute(getFile);
            String fileUrl = videaoFile.getFileUrl(getBotToken());
            message.setUrl(fileUrl);
        } else if (msg.getAudio() != null) {
            message.setType(AUDIO);
            String fileId = msg.getAudio().getFileId();
            GetFile getFile = GetFile.builder().fileId(fileId).build();
            File audiFile = execute(getFile);
            String fileUrl = audiFile.getFileUrl(getBotToken());
            message.setUrl(fileUrl);
        } else if (msg.getText() != null && !msg.getText().isEmpty()) {
            message.setType(TEXT);
            message.setText(msg.getText());
        } else if (msg.getSticker() != null) {
            message.setType(PHOTO);
            String fileId = msg.getSticker().getFileId();
            GetFile getFile = GetFile.builder().fileId(fileId).build();
            File videaoFile = execute(getFile);
            String fileUrl = videaoFile.getFileUrl(getBotToken());
            message.setUrl(fileUrl);
        } else if (msg.getVoice() != null) {
            message.setType(AUDIO);
            String fileId = msg.getVoice().getFileId();
            GetFile getFile = GetFile.builder().fileId(fileId).build();
            File audiFile = execute(getFile);
            String fileUrl = audiFile.getFileUrl(getBotToken());
            message.setUrl(fileUrl);
        } else if (msg.getDocument() != null) {
            message.setType(DOC);
            String fileId = msg.getDocument().getFileId();
            GetFile getFile = GetFile.builder().fileId(fileId).build();
            File file = execute(getFile);
            String fileUrl = file.getFileUrl(getBotToken());
            message.setUrl(fileUrl);
        }
        else {
            return;
        }

        List<Long> vkChats = chatsRepository.findVkChatByTelegramChat(msg.getChatId());
        for (var chatId : vkChats) {
            message.setDestination(chatId);
            messageService.send(message);
        }
    }

    @Override
    public String getBotUsername() {
        return "vkToTelgramBot";
    }

    @SqsListener("messages_to_telegram.fifo")
    public void sendMessage(BasicMessage basicMessage) {
        try {
            if (basicMessage.getType() == TEXT) {
                SendMessage sm = SendMessage.builder()
                        .chatId(basicMessage.getDestination()) //Who are we sending a message to
                        .text(basicMessage.toString()).build();
                execute(sm);
            } else if (basicMessage.getType() == PHOTO) {
                URL url = URI.create(basicMessage.getUrl()).toURL();
                SendPhoto sm = SendPhoto.builder()
                        .chatId(basicMessage.getDestination())
                        .photo(new InputFile(url.openStream(), url.getFile()))
                        .caption(basicMessage.getFullname())
                        .build();
                execute(sm);
            } else if (basicMessage.getType() == VIDEO) {
                URL url = URI.create(basicMessage.getUrl()).toURL();
                SendVideo sm = SendVideo.builder()
                        .chatId(basicMessage.getDestination())
                        .video(new InputFile(url.openStream(), url.getFile()))
                        .caption(basicMessage.getFullname())
                        .build();
                execute(sm);
            } else if (basicMessage.getType() == AUDIO) {
                URL url = URI.create(basicMessage.getUrl()).toURL();
//                SendAudio sm = SendAudio.builder()
//                        .chatId(basicMessage.getDestination())
//                        .audio(new InputFile(url.openStream(), url.getFile()))
//                        .caption(basicMessage.getFullname())
//                        .build();
//                execute(sm);
                SendVoice vm = SendVoice.builder()
                        .chatId(basicMessage.getDestination())
                        .voice(new InputFile(url.openStream(), url.getFile()))
                        .caption(basicMessage.getFullname())
                        .build();
                execute(vm);
            } else if (basicMessage.getType() == DOC) {
                URL url = URI.create(basicMessage.getUrl()).toURL();
                SendDocument sm = SendDocument.builder()
                        .chatId(basicMessage.getDestination())
                        .document(new InputFile(url.openStream(), url.getFile()))
                        .caption(basicMessage.getFullname())
                        .build();
                execute(sm);
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
