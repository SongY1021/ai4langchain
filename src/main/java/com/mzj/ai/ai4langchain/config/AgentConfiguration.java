package com.mzj.ai.ai4langchain.config;

import com.mzj.ai.ai4langchain.service.ChatMemoryStoreService;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.service.*;
import dev.langchain4j.service.tool.ToolProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AgentConfiguration {
    public interface CloudAgent {
        String chat(@MemoryId String memoryId, @UserMessage String userMessage);
        @SystemMessage("""
                你现在的角色是一个云安全管理系统的智能体助手，你可以进行安全网元镜像的查询及安全网元虚机的创建。需要注意当创建网元是需要询问用户虚机使用时长的。
                当用户提供的信息不存在网元名称和类型时提供给用户当前存在的网元信息供用户选择。
                """)
        TokenStream stream(@MemoryId String memoryId, @UserMessage String userMessage);
    }

    @Bean
    public CloudAgent cloudAgent(ChatLanguageModel chatLanguageModel
            , StreamingChatLanguageModel streamingChatLanguageModel) {

        ChatMemoryStoreService chatMemoryStoreService = new ChatMemoryStoreService();

        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .maxMessages(10)
                .id(memoryId)
                .chatMemoryStore(chatMemoryStoreService)
                .build();

        McpTransport transport = new HttpMcpTransport.Builder()
                .sseUrl("http://localhost:9080/sse")
                .logRequests(true) // if you want to see the traffic in the log
                .logResponses(true)
                .build();

        McpClient mcpClient = new DefaultMcpClient.Builder()
                .transport(transport)
                .build();

        ToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(List.of(mcpClient))
                .build();

        CloudAgent cloudAgent = AiServices.builder(CloudAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .chatMemoryProvider(chatMemoryProvider)
                .toolProvider(toolProvider)
                .build();
        return cloudAgent;
    }
}