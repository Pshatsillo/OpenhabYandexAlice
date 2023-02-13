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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.List;
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
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.items.events.ItemStateEvent;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;
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
    protected static EventPublisher eventPublisher;
    private @NonNullByDefault({}) YandexAliceCallbackServlet yandexHTTPCallback;
    private final HttpService httpService;

    private String yandexToken;

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
        logger.warn("Activate Config is {}", config);
        if (config != null && config.get(CFG_TOKEN) != null) {
            yandexToken = config.get(CFG_TOKEN).toString();
        }
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
        logger.warn("modified Config is {}", config);
        if (config != null && config.get(CFG_TOKEN) != null) {
            yandexToken = config.get(CFG_TOKEN).toString();
        }
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
        ItemStateEvent ise = (ItemStateEvent) event;
        String name = ise.getItemName();
        State state = ise.getItemState();
        boolean stateBool;
        if (state.equals("ON")) {
            stateBool = true;
        } else {
            stateBool = false;
        }

        // https://api.iot.yandex.net/v1.0/devices/actions
        HttpURLConnection con;
        URL yandexURL = null;
        try {
            yandexURL = new URL("https://api.iot.yandex.net/v1.0/devices/actions");

            con = (HttpURLConnection) yandexURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + yandexToken);
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            JSONObject requestObj = new JSONObject();
            JSONArray devices = new JSONArray();
            devices.put(new JSONObject().put("id", name).put("actions",
                    new JSONArray().put(new JSONObject().put("type", "devices.capabilities.on_off").put("state",
                            new JSONObject().put("instance", "on").put("value", stateBool)))));
            requestObj.put("devices", devices);
            String answer = "{\"devices\":[{\"id\": \"LightItemTestName\",\"actions\":[{\"type\": \"devices.capabilities.on_off\",\"state\":{\"instance\": \"on\",\"value\": true}}]}]}";
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = answer.getBytes("UTF-8");
                os.write(input, 0, input.length);
            }

            int code = con.getResponseCode();
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
            // }
            con.disconnect();

            logger.debug("state changed");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
            Item item = itemRegistry.getItem(id);
            if (value) {
                eventPublisher.post(ItemEventFactory.createCommandEvent(id, OnOffType.ON));
            } else {
                eventPublisher.post(ItemEventFactory.createCommandEvent(id, OnOffType.OFF));
            }
            logger.debug("Parsing finish");// .getJSONArray(0).getJSONObject(0)

            JSONObject payload = new JSONObject();
            JSONArray devices = new JSONArray();
            answer.put("payload", payload);
            answer.put("request_id", header);
            JSONObject itemJson = new JSONObject();
            itemJson.put("id", item.getName());
            JSONArray caps = getCapabilitiesState(item, "DONE");
            itemJson.put("capabilities", caps);
            devices.put(itemJson);
            payload.put("devices", devices);
        } catch (Exception e) {
            logger.debug("Error get item {} state", e.getLocalizedMessage());
        }
        return answer.toString();
    }

    private static JSONArray getCapabilitiesState(Item item, String status) {
        JSONObject itemJson = new JSONObject();
        JSONObject state = new JSONObject();
        JSONArray capabilitiesArray = new JSONArray();
        if (item.getType().equals("Switch")) {
            JSONObject capabilitiesObj = new JSONObject();
            capabilitiesObj.put("type", "devices.capabilities.on_off");
            state.put("instance", "on");
            state.put("action_result", new JSONObject().put("status", status));
            capabilitiesObj.put("state", state);
            capabilitiesArray.put(capabilitiesObj);
            itemJson.put("capabilities", capabilitiesArray);
        }
        return capabilitiesArray;
    }
}
