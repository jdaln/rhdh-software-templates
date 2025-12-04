package ${{values.java_package_name}}.chat.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

@RegisterAiService
@Singleton
public interface ChatAiService {
    
    @SystemMessage("{{systemPrompt}}")
    @UserMessage("{{userMessage}}")
    String chat(String systemPrompt, String userMessage);
    
    default List<ChatMessage> buildMessageHistory(List<${{values.java_package_name}}.chat.model.Message> requestMessages, 
                                                  String systemPrompt) {
        List<ChatMessage> messages = new ArrayList<>();
        
        // Add system prompt if provided
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            messages.add(new dev.langchain4j.data.message.SystemMessage(systemPrompt));
        }
        
        // Convert request messages to LangChain4j messages
        for (${{values.java_package_name}}.chat.model.Message msg : requestMessages) {
            switch (msg.getRole().toLowerCase()) {
                case "system":
                    messages.add(new dev.langchain4j.data.message.SystemMessage(msg.getContent()));
                    break;
                case "user":
                    messages.add(new dev.langchain4j.data.message.UserMessage(msg.getContent()));
                    break;
                case "assistant":
                    messages.add(new AiMessage(msg.getContent()));
                    break;
            }
        }
        
        return messages;
    }
}

