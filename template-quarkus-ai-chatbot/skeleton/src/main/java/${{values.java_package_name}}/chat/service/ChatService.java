package ${{values.java_package_name}}.chat.service;

import ${{values.java_package_name}}.chat.ai.ChatAiService;
import ${{values.java_package_name}}.chat.config.ChatConfig;
import ${{values.java_package_name}}.chat.model.ChatRequest;
import ${{values.java_package_name}}.chat.model.ChatResponse;
import ${{values.java_package_name}}.chat.model.Message;
import dev.langchain4j.data.message.ChatMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ChatService {
    
    private static final Logger LOG = Logger.getLogger(ChatService.class);
    
    @Inject
    ChatAiService chatAiService;
    
    @Inject
    ChatConfig chatConfig;
    
    public ChatResponse chat(ChatRequest request) {
        LOG.debugf("Processing chat request in ChatService");
        
        // Build message history
        List<Message> allMessages = new ArrayList<>();
        if (request.getMessages() != null) {
            allMessages.addAll(request.getMessages());
            LOG.debugf("Added %d existing messages", request.getMessages().size());
        }
        
        // Add new user message
        if (request.getNewMessage() != null) {
            allMessages.add(new Message("user", request.getNewMessage()));
            LOG.debugf("Added new user message: %s", request.getNewMessage());
        }
        
        // Use system prompt from request or config
        String systemPrompt = request.getSystemPrompt();
        if (systemPrompt == null || systemPrompt.trim().isEmpty()) {
            systemPrompt = chatConfig.systemPrompt();
            LOG.debugf("Using system prompt from config: %s", systemPrompt);
        } else {
            LOG.debugf("Using system prompt from request: %s", systemPrompt);
        }
        
        // Build user message from conversation history
        StringBuilder userMessageBuilder = new StringBuilder();
        for (Message msg : allMessages) {
            if ("user".equalsIgnoreCase(msg.getRole())) {
                if (userMessageBuilder.length() > 0) {
                    userMessageBuilder.append("\n");
                }
                userMessageBuilder.append(msg.getContent());
            } else if ("assistant".equalsIgnoreCase(msg.getRole())) {
                // Include assistant responses in context
                if (userMessageBuilder.length() > 0) {
                    userMessageBuilder.append("\n\nPrevious assistant response: ").append(msg.getContent());
                }
            }
        }
        
        // Get the latest user message
        String userMessage = request.getNewMessage();
        if (userMessage == null && !allMessages.isEmpty()) {
            // Use the last user message if newMessage is not provided
            for (int i = allMessages.size() - 1; i >= 0; i--) {
                if ("user".equalsIgnoreCase(allMessages.get(i).getRole())) {
                    userMessage = allMessages.get(i).getContent();
                    break;
                }
            }
        }
        
        if (userMessage == null || userMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("User message cannot be empty");
        }
        
        LOG.debugf("Calling ChatAiService with systemPrompt='%s', userMessage='%s'", systemPrompt, userMessage);
        
        try {
            // Get response using the AI service with proper annotations
            String response = chatAiService.chat(systemPrompt, userMessage);
            LOG.infof("Received response from ChatAiService (length=%d)", response != null ? response.length() : 0);
            return new ChatResponse(response);
        } catch (Exception e) {
            LOG.errorf(e, "Error calling ChatAiService: %s", e.getMessage());
            throw new RuntimeException("Failed to get response from AI service: " + e.getMessage(), e);
        }
    }
    
    public List<ChatMessage> buildMessageHistory(ChatRequest request) {
        List<Message> allMessages = new ArrayList<>();
        if (request.getMessages() != null) {
            allMessages.addAll(request.getMessages());
        }
        
        if (request.getNewMessage() != null) {
            allMessages.add(new Message("user", request.getNewMessage()));
        }
        
        String systemPrompt = request.getSystemPrompt();
        if (systemPrompt == null || systemPrompt.trim().isEmpty()) {
            systemPrompt = chatConfig.systemPrompt();
        }
        
        return chatAiService.buildMessageHistory(allMessages, systemPrompt);
    }
}

