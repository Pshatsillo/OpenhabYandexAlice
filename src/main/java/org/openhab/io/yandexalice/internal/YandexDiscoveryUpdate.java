/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * This class update items on remote server.
 *
 * @author Petr Shatsillo - Initial contribution
 *
 */
@NonNullByDefault
public class YandexDiscoveryUpdate implements Runnable {
    private final YandexAliceCredits credit = new YandexAliceCredits();
    private final Logger logger = LoggerFactory.getLogger(YandexDiscoveryUpdate.class);
    private final String userId;

    public YandexDiscoveryUpdate(String userId) {
        this.userId = userId;
    }

    @Override
    public void run() {
        logger.debug("DiscoveryUpdate running for user {}", userId);
        HttpURLConnection con;
        URL yandexURL;
        try {
            // Специальный endpoint для обновления списка устройств
            yandexURL = URI
                    .create("https://dialogs.yandex.net/api/v1/skills/" + credit.getSkillID() + "/callback/discovery")
                    .toURL();
            con = (HttpURLConnection) yandexURL.openConnection();
            con.setConnectTimeout(1000);
            con.setReadTimeout(2000);
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "OAuth " + credit.getoAuth());
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            // Формируем требуемый JSON: {"ts": 1234567890.123, "payload": {"user_id": "..."}}
            JsonObject json = new JsonObject();
            json.addProperty("ts", (double) System.currentTimeMillis() / 1000L);
            JsonObject payload = new JsonObject();
            payload.addProperty("user_id", userId);
            json.add("payload", payload);

            String jsonStr = json.toString();
            logger.debug("Discovery payload: {}", jsonStr);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonStr.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int code = con.getResponseCode();
            if ((code != 200) && (code != 202)) {
                InputStream errStream = con.getErrorStream();
                if (errStream != null) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    logger.error("Discovery Response error: {}", response.toString().trim());
                } else {
                    logger.error("Discovery Response code: {}", code);
                }
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                logger.info("Discovery successful! Yandex response: {}", response.toString().trim());
            }
        } catch (IOException e) {
            logger.error("Discovery ERROR {}", e.getMessage());
        }
    }
}
