/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("POST servlet: {}", req.getRequestURI());
        String body = req.getReader().lines().reduce("", String::concat);
        logger.debug("POST request from Yandex: {}", body);
        String answer = "";
        if (req.getRequestURI() != null) {
            String reqUri = req.getRequestURI();
            if (("/yandex/v1.0/user/devices/query").equals(reqUri)) {
                logger.debug("Requesting item state from Yandex: {}", body);
                answer = YandexService.getItemState(body, req.getHeader("X-Request-Id"));
            } else if (("/yandex/v1.0/user/devices/action").equals(reqUri)) {
                logger.debug("Action item from Yandex: {}", body);
                answer = YandexService.setItemState(body, req.getHeader("X-Request-Id"));
            }
        }
        resp.setContentType(MediaType.APPLICATION_JSON);
        resp.setCharacterEncoding("utf-8");
        resp.getWriter().print(answer);
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
