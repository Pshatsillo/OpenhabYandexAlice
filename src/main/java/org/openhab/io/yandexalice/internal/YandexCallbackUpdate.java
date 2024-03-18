/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class update items on remote server.
 *
 * @author Petr Shatsillo - Initial contribution
 *
 */
@NonNullByDefault
public class YandexCallbackUpdate implements Runnable {
    private final YandexAliceCredits credit = new YandexAliceCredits();
    private final Logger logger = LoggerFactory.getLogger(YandexCallbackUpdate.class);
    String json;

    public YandexCallbackUpdate(String json) {
        this.json = json;
    }

    @Override
    public void run() {
        logger.debug("UpdateCallback running, json is {}", json);
        HttpURLConnection con;
        URL yandexURL;
        try {
            yandexURL = new URL("https://dialogs.yandex.net/api/v1/skills/" + credit.getSkillID() + "/callback/state");
            con = (HttpURLConnection) yandexURL.openConnection();
            con.setConnectTimeout(1000);
            con.setReadTimeout(2000);
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "OAuth " + credit.getoAuth());
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int code = con.getResponseCode();
            // Map<String, List<String>> headers = con.getHeaderFields();
            logger.debug("Response: {}, code {}", con.getResponseMessage(), code);
            // InputStream resp = con.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            String result = response.toString().trim();
            logger.debug("input string from REST: {}", result);
        } catch (IOException e) {
            logger.debug("ERROR {}", e.getMessage());
        }
    }
}
