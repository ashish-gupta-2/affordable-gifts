package com.affordablegifts.backend.common;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

/** Framework error pages (404, 405...) say only the status: no timestamp, path or exception. */
@Component
class MinimalErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest request, ErrorAttributeOptions options) {
        Map<String, Object> full = super.getErrorAttributes(request, options);
        Map<String, Object> minimal = new LinkedHashMap<>();
        minimal.put("status", full.get("status"));
        minimal.put("error", full.get("error"));
        return minimal;
    }
}
