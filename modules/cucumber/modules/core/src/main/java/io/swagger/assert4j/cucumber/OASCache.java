package io.swagger.assert4j.cucumber;

import com.google.common.collect.Maps;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import javax.inject.Singleton;
import java.util.Map;

/**
 * Utility class for loading and caching Swagger definitions
 */

@Singleton
public class OASCache {

    private final OpenAPIParser parser;
    private Map<String, OpenAPI> cache = Maps.newHashMap();

    public OASCache() {
        parser = new OpenAPIParser();
    }

    public OpenAPI getOAS(String oasUrl) {
        if (!cache.containsKey(oasUrl)) {
            SwaggerParseResult result = parser.readLocation(oasUrl, null, new ParseOptions());
            cache.put(oasUrl, result.getOpenAPI() );
        }

        return cache.get(oasUrl);
    }
}
