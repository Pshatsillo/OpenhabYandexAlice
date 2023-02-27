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
    public static final String UNIT_TEMP_KELVIN = "unit.temperature.kelvin";
    public static final String UNIT_TEMP_CELSIUS = "unit.temperature.celsius";
    public static final String PROP_FLOAT = "devices.properties.float";
    public static final String DEV_SENSOR = "devices.types.sensor";
    public static final String DEV_SOCKET = "devices.types.socket";
    public static final String DEV_SWITCH = "devices.types.switch";
    public static final String DEV_LIGHT = "devices.types.light";
    public static final String INS_TEMP = "temperature";
    public static final String CAP_ON_OFF = "devices.capabilities.on_off";

    String requestID;
    JSONObject returnRequest = new JSONObject();
    Logger log = LoggerFactory.getLogger(YandexAliceJson.class);

    public YandexAliceJson(String requestID) {
        returnRequest.put("request_id", requestID);
        returnRequest.put("payload", new JSONObject());
        returnRequest.getJSONObject("payload").put("devices", new JSONArray());
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

    public void setDeviceID(YandexDevice yaDevice) {
        returnRequest.getJSONObject("payload").getJSONArray("devices")
                .put(new JSONObject().put("id", yaDevice.getId()));
        // log.info("device {}", device);
    }

    public void addCapabilities(YandexDevice yaDev) {
        JSONArray device = new JSONObject(returnRequest.get("payload").toString()).getJSONArray("devices");
        JSONArray caps = new JSONArray();
        for (YandexAliceCapabilities cp : yaDev.getCapabilities()) {
            caps.put(new JSONObject().put("type", cp.getCapability()).put("parameters", new JSONObject())
                    .put("retrievable", true).put("reportable", true));
        }
        for (int i = 0; i < device.length(); i++) {
            String capID = device.getJSONObject(i).get("id").toString();
            if (capID.equals(yaDev.getId())) {
                returnRequest.getJSONObject("payload").getJSONArray("devices").getJSONObject(i).put("capabilities",
                        caps);
                // log.debug("jsonObject device {}", returnRequest);
            }
        }
    }

    public void addProperties(YandexDevice yaDev) {
        JSONArray device = new JSONObject(returnRequest.get("payload").toString()).getJSONArray("devices");
        JSONArray properties = new JSONArray();
        for (YandexAliceProperties prp : yaDev.getProperties()) {
            properties.put(new JSONObject().put("type", prp.getPropName())
                    .put("parameters", new JSONObject().put("instance", prp.getInstance()).put("unit", prp.getUnit()))
                    .put("retrievable", true).put("reportable", true));
        }
        for (int i = 0; i < device.length(); i++) {
            String capID = device.getJSONObject(i).get("id").toString();
            if (capID.equals(yaDev.getId())) {
                returnRequest.getJSONObject("payload").getJSONArray("devices").getJSONObject(i).put("properties",
                        properties);
                log.debug("jsonObject device {}", returnRequest);
            }
        }
    }

    public void createDevice(YandexDevice yDev) {
        JSONObject deviceObj = new JSONObject();
        deviceObj.put("id", yDev.getId()).put("name", yDev.getName()).put("type", yDev.getType())
                .put("capabilities", new JSONArray()).put("properties", new JSONArray());
        returnRequest.getJSONObject("payload").getJSONArray("devices").put(deviceObj);
    }

    public void addCapabilityState(String capability, String status) {
        if (status.equals("on")) {
            status = "true";
        } else if (status.equals("off")) {
            status = "false";
        }
        log.debug("Cap set");
    }

    public void addPropertyState(YandexAliceProperties prop, String state) {
        returnRequest.getJSONObject("payload").getJSONArray("devices").getJSONObject(0).put("properties",
                new JSONArray().put(new JSONObject().put("type", prop.getPropName()).put("state", new JSONObject()
                        .put("instance", prop.getInstance()).put("value", Integer.parseInt(state.split(" ")[0])))));
        log.debug("Property set");
    }
}
