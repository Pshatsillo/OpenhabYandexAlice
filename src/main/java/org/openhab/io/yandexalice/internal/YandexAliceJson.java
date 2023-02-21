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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YandexAliceJson} is responsible for JSON string generation
 *
 * @author Petr Shatsillo - Initial contribution
 */
@NonNullByDefault
public class YandexAliceJson {
    public final String DEV_SOCKET = "devices.types.socket";
    public final String CAP_ON_OFF = "devices.capabilities.on_off";
    public final String DEV_SWITCH = "devices.types.switch";
    public final String DEV_LIGHT = "devices.types.light";
    String requestID;
    JSONObject returnRequest = new JSONObject();
    Logger log = LoggerFactory.getLogger(YandexAliceJson.class);

    public YandexAliceJson(String requestID) {
        returnRequest.put("request_id", requestID);
        this.requestID = requestID;
    }

    public void setUserDevices(String s) {
        JSONObject payload = new JSONObject();
        payload.put("user_id", s);
        payload.put("devices", new JSONArray());
        returnRequest.put("payload", payload);
    }

    public void setDeviceType(String s) {
    }

    public void setDeviceName(String deviceLabel) {
    }

    public void setDeviceID(String tstDevice) {
        JSONArray device = new JSONObject(returnRequest.get("payload").toString()).getJSONArray("devices");
        // log.info("device {}", device);
    }

    public void createDevice(String id, String name, String type) {
        JSONObject deviceObj = new JSONObject();
        deviceObj.put("id", id).put("name", name).put("type", type).put("capabilities", new JSONArray())
                .put("properties", new JSONArray());
        returnRequest.getJSONObject("payload").getJSONArray("devices").put(deviceObj);
    }

    public void addCapabilities(String capabilityName, String id) {
        JSONArray device = new JSONObject(returnRequest.get("payload").toString()).getJSONArray("devices");
        for (int i = 0; i < device.length(); i++) {
            String capID = device.getJSONObject(i).get("id").toString();
            if (capID.equals(id)) {
                returnRequest.getJSONObject("payload").getJSONArray("devices").getJSONObject(i)
                        .getJSONArray("capabilities").put(new JSONObject().put("type", capabilityName));
                log.debug("jsonObject device {}", returnRequest);
            }
        }
    }

    public void addProperties(String propertyName, String id, String instance, String unit) {
        JSONArray device = new JSONObject(returnRequest.get("payload").toString()).getJSONArray("devices");
        for (int i = 0; i < device.length(); i++) {
            String capID = device.getJSONObject(i).get("id").toString();
            if (capID.equals(id)) {
                returnRequest.getJSONObject("payload").getJSONArray("devices").getJSONObject(i)
                        .getJSONArray("properties").put(new JSONObject().put("type", propertyName).put("parameters",
                                new JSONObject().put("instance", instance).put("unit", unit)));
                log.debug("jsonObject device {}", returnRequest);
            }
        }
    }
}
