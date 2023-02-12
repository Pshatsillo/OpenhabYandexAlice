/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.io.yandexalice.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YandexAliceCallbackServlet} is responsible for creating things and thing
 * handlers.
 *
 * @author Petr Shatsillo - Initial contribution
 */
@NonNullByDefault
public class YandexAliceCallbackServlet extends HttpServlet {
    private static final long serialVersionUID = -2725161358635927815L;
    private final Logger logger = LoggerFactory.getLogger(YandexAliceCallbackServlet.class);

    public YandexAliceCallbackServlet() {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.debug("Get servlet: {}", req.getPathInfo());
        //
        // HttpURLConnection con;
        // URL megaURL = new URL(
        // "http://localhost:" + System.getProperty("org.osgi.service.http.port") + "/rest/items?recursive=false");
        // con = (HttpURLConnection) megaURL.openConnection();
        // con.setRequestMethod("GET");
        //
        // con.setRequestProperty("Authorization", "Bearer "
        // + "oh.admin.plTrWNSGx4VzY1McJSd8DItg3B8mvuP8sAb7SLYLD1ha25XRhDjgCMKFkLDhWcLHwM8JMlJbyMpaRBmKyGw");
        // con.setRequestProperty("accept", "application/json");
        // logger.debug("Response: {}", con.getResponseMessage());
        // BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        // String inputLine;
        // StringBuilder response = new StringBuilder();
        // while ((inputLine = in.readLine()) != null) {
        // response.append(inputLine);
        // }
        // in.close();
        // String result = response.toString().trim();
        // logger.debug("input string from REST: {}", result);
        // con.disconnect();
        // JsonParser parser = new JsonParser();
        // JsonElement jsonElement = parser.parse(result);
        // // JsonObject rootObject = jsonElement.getAsJsonObject();
        // JsonArray itemslist = jsonElement.getAsJsonArray();
        // JsonArray yandexList = new JsonArray();
        // for (JsonElement itemslistO : itemslist) {
        // JsonObject rootObject = itemslistO.getAsJsonObject();
        // JsonArray tags = rootObject.getAsJsonArray("tags");
        // for (JsonElement tag : tags) {
        // String tg = tag.getAsString();
        // if (tg.equals("yndx")) {
        // yandexList.add(rootObject);
        // }
        // }
        // logger.debug("json {}", rootObject.toString());
        // }
        // JsonObject devList = new JsonObject();
        // JsonObject payload = new JsonObject();
        // payload.addProperty("user_id", "1");
        // payload.add("devices", yandexList);
        // devList.addProperty("request_id", req.getHeader("X-Request-Id"));
        // devList.add("payload", payload);
        resp.setContentType(MediaType.APPLICATION_JSON);
        resp.setCharacterEncoding("utf-8");
        // logger.debug("X-Request-Id: {}", req.getHeader("X-Request-Id"));
        // String responseJSON = "{\"request_id\":\" " + req.getHeader("X-Request-Id")
        // // + ", \"payload\": {\"user_id\":\"63db985b4f99a7559623ff03\", \"devices\":[]}}";
        // + "\", \"payload\": {\"user_id\":\"63db985b4f99a7559623ff03\", \"devices\":[{\"id\":
        // \"abc-123\",\"name\":\"лампa\",\"type\": \"devices.types.light\", \"capabilities\": [{\"type\":
        // \"devices.capabilities.on_off\", \"retrievable\": true, \"reportable\": true}], \"properties\": []}, {\"id\":
        // \"abc-321\",\"name\":\"лампa\",\"type\": \"devices.types.light\", \"capabilities\": [{\"type\":
        // \"devices.capabilities.on_off\", \"retrievable\": true, \"reportable\": true}], \"properties\": []}]}}";
        // // resp.setContentLength(responseJSON.length() + 4);
        resp.getWriter().print(YandexService.getItemsList(req.getHeader("X-Request-Id")));
        resp.setStatus(HttpServletResponse.SC_OK);
        logger.debug("response: {}", YandexService.getItemsList(req.getHeader("X-Request-Id")));
        // super.doGet(req, resp);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.debug("HEAD servlet");
        super.doHead(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpURLConnection con;
        logger.debug("POST servlet: {}", req.getRequestURI());
        String body = req.getReader().lines().reduce("", String::concat);
        URL megaURL = new URL(
                "http://localhost:" + System.getProperty("org.osgi.service.http.port") + "/rest/items?recursive=false");
        con = (HttpURLConnection) megaURL.openConnection();
        con.setRequestMethod("GET");

        con.setRequestProperty("Authorization", "Bearer "
                + "oh.admin.plTrWNSGx4VzY1McJSd8DItg3B8mvuP8sAb7SLYLD1ha25XRhDjgCMKFkLDhWcLHwM8JMlJbyMpaRBmKyGw");
        con.setRequestProperty("accept", "application/json");

        Map<String, List<String>> headers = con.getHeaderFields();
        // if (con.getResponseCode() == 200) {
        // logger.debug("OK");
        logger.debug("Response: {}", con.getResponseMessage());
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        String result = response.toString().trim();
        logger.debug("input string from REST: {}", result);
        con.disconnect();
        // }
        // con.disconnect();
        logger.debug("POST request from Yandex: {}", body);

        if (req.getRequestURI().equals("/yandex/v1.0/user/devices/query")) {
            logger.debug("Requesting item state from Yandex: {}", body);
            YandexService.getItemState(body);
        }
    }
}
