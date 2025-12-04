package ${{values.java_package_name}}.chat.resource;

import ${{values.java_package_name}}.chat.config.ChatConfig;
import ${{values.java_package_name}}.chat.model.ChatRequest;
import ${{values.java_package_name}}.chat.model.ChatResponse;
import ${{values.java_package_name}}.chat.model.ConfigResponse;
import ${{values.java_package_name}}.chat.service.ChatService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;

@Path("/api/chat")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChatResource {
    
    private static final Logger LOG = Logger.getLogger(ChatResource.class);
    
    @Inject
    ChatService chatService;
    
    @Inject
    ChatConfig chatConfig;
    
    @ConfigProperty(name = "quarkus.langchain4j.openai.chat-model.model-name", defaultValue = "gpt-3.5-turbo")
    String modelName;
    
    @POST
    public ChatResponse chat(ChatRequest request) {
        LOG.infof("Received chat request: messages=%d, newMessage=%s, systemPrompt=%s", 
                request.getMessages() != null ? request.getMessages().size() : 0,
                request.getNewMessage() != null ? "present" : "null",
                request.getSystemPrompt() != null ? "present" : "null");
        try {
            ChatResponse response = chatService.chat(request);
            LOG.infof("Chat response generated successfully, length=%d", 
                    response.getAssistantMessage() != null ? response.getAssistantMessage().length() : 0);
            return response;
        } catch (Exception e) {
            LOG.errorf(e, "Error processing chat request: %s", e.getMessage());
            throw e;
        }
    }
    
    @POST
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Response streamChat(ChatRequest request) {
        LOG.infof("Received streaming chat request: messages=%d, newMessage=%s", 
                request.getMessages() != null ? request.getMessages().size() : 0,
                request.getNewMessage() != null ? "present" : "null");
        try {
            ChatResponse response = chatService.chat(request);
            LOG.infof("Streaming response generated, length=%d", 
                    response.getAssistantMessage() != null ? response.getAssistantMessage().length() : 0);
            return Response.ok()
                    .type(MediaType.SERVER_SENT_EVENTS)
                    .entity(new StreamingResponse(response.getAssistantMessage()))
                    .build();
        } catch (Exception e) {
            LOG.errorf(e, "Error processing streaming chat request: %s", e.getMessage());
            return Response.serverError()
                    .entity("error: " + e.getMessage())
                    .build();
        }
    }
    
    @GET
    @Path("/config")
    public ConfigResponse getConfig() {
        LOG.debugf("Getting config: modelName=%s", modelName);
        return new ConfigResponse(modelName, chatConfig.systemPrompt());
    }
    
    private static class StreamingResponse implements jakarta.ws.rs.core.StreamingOutput {
        private final String content;
        
        public StreamingResponse(String content) {
            this.content = content;
        }
        
        @Override
        public void write(java.io.OutputStream output) throws IOException, jakarta.ws.rs.WebApplicationException {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(
                    new java.io.BufferedWriter(new java.io.OutputStreamWriter(output)))) {
                
                // Simple streaming: send content character by character
                String[] words = content.split(" ");
                for (int i = 0; i < words.length; i++) {
                    writer.write("event: token\n");
                    writer.write("data: " + (i > 0 ? " " : "") + words[i] + "\n\n");
                    writer.flush();
                    try {
                        Thread.sleep(50); // Small delay for streaming effect
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                
                writer.write("event: done\n");
                writer.write("data: \n\n");
                writer.flush();
            }
        }
    }
}

