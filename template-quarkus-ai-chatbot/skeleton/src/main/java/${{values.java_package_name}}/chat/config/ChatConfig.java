package ${{values.java_package_name}}.chat.config;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "ai.chat")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface ChatConfig {
    
    @WithDefault("You are a helpful assistant.")
    String systemPrompt();
    
    @WithDefault("60")
    int timeout();
}

