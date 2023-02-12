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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventFilter;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.ItemStateEvent;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class starts communication between openhab and yandex.
 *
 * @author Petr Shatsillo - Initial contribution
 *
 */
@Component(service = { YandexService.class,
        EventSubscriber.class }, configurationPid = "org.openhab.yandexalice", property = Constants.SERVICE_PID
                + "=org.openhab.yandexalice")
@ConfigurableService(category = "io", label = "Yandex Alice", description_uri = YandexService.CONFIG_URI)
public class YandexService implements EventSubscriber {
    protected static final String CONFIG_URI = "io:yandexalice";
    private static final String CFG_TOKEN = "token";
    private static Logger logger = LoggerFactory.getLogger(YandexService.class);
    private final HttpClient httpClient;
    protected static ItemRegistry itemRegistry;
    protected final EventPublisher eventPublisher;
    private @NonNullByDefault({}) YandexAliceCallbackServlet yandexHTTPCallback;
    private final HttpService httpService;

    @Activate
    public YandexService(final @Reference HttpClientFactory httpClientFactory,
            final @Reference ItemRegistry itemRegistry, final @Reference EventPublisher eventPublisher,
            final @Reference HttpService httpService) {
        this.httpClient = httpClientFactory.createHttpClient("yandexalice");
        this.httpService = httpService;
        this.httpClient.setStopTimeout(0);
        this.httpClient.setMaxConnectionsPerDestination(200);
        this.httpClient.setConnectTimeout(30000);
        this.httpClient.setFollowRedirects(false);

        this.itemRegistry = itemRegistry;
        this.eventPublisher = eventPublisher;
    }

    @Activate
    protected void activate(BundleContext context, Map<String, ?> config) {
        try {
            yandexHTTPCallback = new YandexAliceCallbackServlet();
            this.httpService.registerServlet("/yandex", yandexHTTPCallback, null,
                    this.httpService.createDefaultHttpContext());
        } catch (ServletException | NamespaceException ignored) {
        }
    }

    @Deactivate
    protected void deactivate() {
        // logger.debug("openHAB Cloud connector deactivated");
        try {
            httpClient.stop();
        } catch (Exception e) {
            // logger.debug("Could not stop Jetty http client", e);
        }
    }

    @Modified
    protected void modified(Map<String, ?> config) {
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return Set.of(ItemStateEvent.TYPE);
    }

    @Override
    public EventFilter getEventFilter() {
        return null;
    }

    @Override
    public void receive(Event event) {
    }

    public static String getItemState(String json, String header) {
        JSONObject answer = new JSONObject();
        try {
            JSONObject parseItem = new JSONObject(json);
            JSONArray dev = (JSONArray) parseItem.get("devices");
            JSONObject itemID = dev.getJSONObject(0);
            Item item = itemRegistry.getItem(itemID.getString("id"));
            logger.debug("Getting item {} state", item.getName());
            JSONObject payload = new JSONObject();
            JSONArray devices = new JSONArray();
            answer.put("payload", payload);
            answer.put("request_id", header);
            JSONObject itemJson = new JSONObject();
            itemJson.put("id", item.getName());
            itemJson.put("capabilities", getCapabilitiesState(item));
            devices.put(itemJson);
            payload.put("devices", devices);
        } catch (ItemNotFoundException e) {
            logger.debug("Error get item {} state", e.getLocalizedMessage());
        }
        return answer.toString();
    }

    public static @NonNull String getItemsList(String header) {
        JSONObject response = new JSONObject();
        JSONObject payload = new JSONObject();
        JSONArray devices = new JSONArray();
        payload.put("user_id", "1");
        response.put("payload", payload);
        response.put("request_id", header);
        Collection<Item> itemsList = itemRegistry.getItems();
        for (Item item : itemsList) {
            if (item.hasTag("yndx")) {
                JSONObject itemJson = new JSONObject();
                itemJson.put("id", item.getName());
                itemJson.put("name", item.getLabel());
                if (item.getType().equals("Switch") && item.hasTag("Lightbulb")) {
                    JSONArray capabilitiesArray = new JSONArray();
                    JSONObject capabilitiesObj = new JSONObject();
                    capabilitiesObj.put("type", "devices.capabilities.on_off");
                    capabilitiesObj.put("retrievable", true);
                    capabilitiesObj.put("reportable", true);
                    capabilitiesArray.put(capabilitiesObj);
                    itemJson.put("type", "devices.types.light");
                    itemJson.put("capabilities", capabilitiesArray);
                } else if (item.getType().equals("Switch") && item.hasTag("RadiatorControl")) {
                    JSONArray capabilitiesArray = new JSONArray();
                    JSONObject capabilitiesObj = new JSONObject();
                    capabilitiesObj.put("type", "devices.capabilities.on_off");
                    capabilitiesObj.put("retrievable", true);
                    capabilitiesObj.put("reportable", true);
                    capabilitiesArray.put(capabilitiesObj);
                    itemJson.put("type", "devices.types.thermostat");
                    itemJson.put("capabilities", capabilitiesArray);
                }
                devices.put(itemJson);
                payload.put("devices", devices);
            }
            logger.debug("item name {} and tags: {}", item.getName(), item.getTags().toString());
        }
        String answer = "";
        answer = response.toString();
        logger.debug("Items list response: {}", answer);
        return answer;
    }

    private static JSONArray getCapabilitiesState(Item item) {
        JSONObject itemJson = new JSONObject();
        JSONObject state = new JSONObject();
        JSONArray capabilitiesArray = new JSONArray();
        if (item.getType().equals("Switch") && item.hasTag("Lightbulb")) {

            JSONObject capabilitiesObj = new JSONObject();
            capabilitiesObj.put("type", "devices.capabilities.on_off");
            state.put("instance", "on");
            if (item.getState().toString().equals("ON")) {
                state.put("value", true);
            } else {
                state.put("value", false);
            }
            capabilitiesObj.put("state", state);
            capabilitiesArray.put(capabilitiesObj);
            itemJson.put("capabilities", capabilitiesArray);
        } else if (item.getType().equals("Switch") && item.hasTag("RadiatorControl")) {
            JSONObject capabilitiesObj = new JSONObject();
            capabilitiesObj.put("type", "devices.capabilities.on_off");
            state.put("instance", "on");
            if (item.getState().toString().equals("ON")) {
                state.put("value", true);
            } else {
                state.put("value", false);
            }
            capabilitiesObj.put("state", state);
            capabilitiesArray.put(capabilitiesObj);
            itemJson.put("capabilities", capabilitiesArray);
        }
        return capabilitiesArray;
    }

    public static String setItemState(String json, String header) {
        JSONObject answer = new JSONObject();
        try {
            JSONArray parseItem = new JSONObject(json).getJSONObject("payload").getJSONArray("devices");
            JSONObject dev = parseItem.getJSONObject(0);
            String id = dev.getString("id");
            JSONArray capabilities = dev.getJSONArray("capabilities");
            JSONObject state = capabilities.getJSONObject(0).getJSONObject("state");
            boolean value = state.getBoolean("value");
            logger.debug("Parsing finish");// .getJSONArray(0).getJSONObject(0)
            // .getJSONArray("state").getJSONObject(1).getJSONObject("value");
        } catch (Exception e) {
            logger.debug("Error get item {}", e.getLocalizedMessage());
        }
        String result = "";
        return result;
    }
}
