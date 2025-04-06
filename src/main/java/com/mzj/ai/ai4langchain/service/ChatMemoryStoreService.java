package com.mzj.ai.ai4langchain.service;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ChatMemoryStoreService implements ChatMemoryStore {
    public static final ConcurrentHashMap<Object, List<ChatMessage>> chatMenoryMap = new ConcurrentHashMap<>();
    private Logger logger = LoggerFactory.getLogger(ChatMemoryStoreService.class);
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        logger.info("Getting messages from memory id: " + memoryId);
        return chatMenoryMap.getOrDefault(memoryId, List.of());
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        this.chatMenoryMap.put(memoryId, list);
        logger.info("Updating messages from memory id: " + memoryId);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        this.chatMenoryMap.remove(memoryId);
        logger.info("Deleting messages from memory id: " + memoryId);
    }
}
