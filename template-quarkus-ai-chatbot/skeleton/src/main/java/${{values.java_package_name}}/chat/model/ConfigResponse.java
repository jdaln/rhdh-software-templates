package ${{values.java_package_name}}.chat.model;

public class ConfigResponse {
    private String modelName;
    private String systemPrompt;

    public ConfigResponse() {
    }

    public ConfigResponse(String modelName, String systemPrompt) {
        this.modelName = modelName;
        this.systemPrompt = systemPrompt;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }
}

