package com.github.kgoedert.crm;

import javax.inject.Singleton;
import javax.json.bind.JsonbConfig;

import io.quarkus.jsonb.JsonbConfigCustomizer;

@Singleton
public class JsonConfigurator implements JsonbConfigCustomizer {

    public void customize(JsonbConfig config) {
        config.withPropertyVisibilityStrategy(new PrivateVisibilityStrategy());
    }
}