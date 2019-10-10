package com.smartbear.readyapi4j.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContentUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ContentUtils.class);

    /**
     * Serializes the specified content object to a string with the specified mediaType,
     * supports mediaTypes ending with "json", "yaml" and "xml" - other types are simply
     * returned as toString()
     *
     * @param content   the object to serialize
     * @param mediaType the mediaType, for example "application/json"
     * @return the serialized object, null if a null-object was specified
     */

    public static String serializeContent(Object content, String mediaType) {

        if (content == null) {
            return null;
        }

        if (content instanceof String) {
            return (String) content;
        }

        if (mediaType == null) {
            return content.toString();
        }

        String result = null;
        String mediaTypeLowerCase = mediaType.toLowerCase();
        try {
            if (mediaTypeLowerCase.endsWith("json")) {
                result = Json.mapper().writeValueAsString(content);
            } else if (mediaTypeLowerCase.endsWith("yaml")) {
                result = Yaml.mapper().writeValueAsString(content);
            } else if (mediaTypeLowerCase.endsWith("xml")) {
                result = new XmlMapper().writeValueAsString(content);
            }
        } catch (JsonProcessingException e) {
            LOG.error("Failed to serialize body with mediaType " + mediaType, e);
        }

        if (result == null) {
            result = content.toString();
        }

        return result;
    }

    /**
     * turns a simple format such as 'json' or 'xml' into its corresponding content-type
     * by prefixing it with 'application/' if needed.
     *
     * @param format the format to expand
     * @return the expanded format
     */

    public static String expandContentType(String format) {
        if (format.indexOf('/') == -1) {
            return "application/" + format;
        }

        return format;
    }

    private ContentUtils() {
    }
}
