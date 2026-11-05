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

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class update items on remote server.
 *
 * @author Petr Shatsillo - Initial contribution
 *
 */
@NonNullByDefault
public class YandexCallbackUpdate {

    private static final int CONNECT_TIMEOUT_SECONDS = 5;

    private final YandexAliceCredits credit;
    private final Logger logger = LoggerFactory.getLogger(YandexCallbackUpdate.class);
    private final HttpClient httpClient;

    public YandexCallbackUpdate(HttpClient httpClient, YandexAliceCredits credit) {
        this.httpClient = httpClient;
        this.credit = credit;
    }

    public void send(String json) {
        String url = "https://dialogs.yandex.net/api/v1/skills/" + credit.getSkillID() + "/callback/state";
        logger.debug("UpdateCallback sending, json is {}", json);
        try {
            httpClient.newRequest(url).method(HttpMethod.POST)
                    .header(HttpHeader.AUTHORIZATION, "OAuth " + credit.getoAuth())
                    .header(HttpHeader.CONTENT_TYPE, "application/json")
                    .content(new StringContentProvider("application/json", json, StandardCharsets.UTF_8))
                    .timeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS).send(new BufferingResponseListener() {
                        @Override
                        public void onComplete(Result result) {
                            if (result.isFailed()) {
                                Throwable failure = result.getFailure();
                                logger.error("Callback request failed: {}",
                                        failure != null ? failure.getMessage() : "unknown error");
                                return;
                            }
                            int status = result.getResponse().getStatus();
                            String body = this.getContentAsString(StandardCharsets.UTF_8);
                            if (status != 200 && status != 202) {
                                logger.error("Response error: {} {}", status, body);
                            } else {
                                logger.debug("input string from REST: {}", body);
                            }
                        }
                    });
        } catch (Exception e) {
            logger.error("ERROR sending callback: {}", e.getMessage());
        }
    }
}
