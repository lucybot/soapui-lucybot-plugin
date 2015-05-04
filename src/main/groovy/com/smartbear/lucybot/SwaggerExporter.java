package com.smartbear.lucybot;

import com.eviware.soapui.impl.rest.RestService;

/**
 * Created by ole on 16/09/14.
 */
public interface SwaggerExporter {

    public String exportToLucyBot(String apiVersion, String format, RestService[] services, String basePath);
}
