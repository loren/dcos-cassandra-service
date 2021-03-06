package com.mesosphere.dcos.cassandra.scheduler.config;

public class ConfigValidationError {
    private final String configField;
    private final String message;

    public ConfigValidationError(String configField, String message) {
        this.configField = configField;
        this.message = message;
    }

    public String getConfigField() {
        return configField;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return String.format("Validation error. Field: '%s'; Message: '%s'", configField, message);
    }
}
