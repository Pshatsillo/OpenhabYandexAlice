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

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YandexAliceCallbackServlet} is responsible for creating things and thing
 * handlers.
 *
 * @author Petr Shatsillo - Initial contribution
 */
public class YandexAliceCallbackServlet extends HttpServlet {
    private static final long serialVersionUID = -2725161358635927815L;
    private final Logger logger = LoggerFactory.getLogger(YandexAliceCallbackServlet.class);

    public YandexAliceCallbackServlet() {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.debug("Get servlet: {}", req.getPathInfo());
        resp.setContentType(MediaType.APPLICATION_JSON);
        resp.setCharacterEncoding("utf-8");
        resp.getWriter().print(YandexService.getItemsList(req.getHeader("X-Request-Id")));
        resp.setStatus(HttpServletResponse.SC_OK);
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
        // URL megaURL = new URL(
        // "http://localhost:" + System.getProperty("org.osgi.service.http.port") + "/rest/items?recursive=false");
        // con = (HttpURLConnection) megaURL.openConnection();
        // con.setRequestMethod("GET");
        //
        // con.setRequestProperty("Authorization", "Bearer "
        // + "oh.admin.plTrWNSGx4VzY1McJSd8DItg3B8mvuP8sAb7SLYLD1ha25XRhDjgCMKFkLDhWcLHwM8JMlJbyMpaRBmKyGw");
        // con.setRequestProperty("accept", "application/json");
        //
        // Map<String, List<String>> headers = con.getHeaderFields();
        // // if (con.getResponseCode() == 200) {
        // // logger.debug("OK");
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
        // }
        // con.disconnect();
        logger.debug("POST request from Yandex: {}", body);
        String answer = "";
        if (req.getRequestURI().equals("/yandex/v1.0/user/devices/query")) {
            logger.debug("Requesting item state from Yandex: {}", body);
            answer = YandexService.getItemState(body, req.getHeader("X-Request-Id"));
        } else if (req.getRequestURI().equals("/yandex/v1.0/user/devices/action")) {
            logger.debug("Action item from Yandex: {}", body);
            answer = YandexService.setItemState(body, req.getHeader("X-Request-Id"));
        }
        resp.setContentType(MediaType.APPLICATION_JSON);
        resp.setCharacterEncoding("utf-8");
        resp.getWriter().print(answer);
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
