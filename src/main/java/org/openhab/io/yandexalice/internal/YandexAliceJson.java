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
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YandexAliceJson} is responsible for JSON string generation
 *
 * @author Petr Shatsillo - Initial contribution
 */
@NonNullByDefault
public class YandexAliceJson {

    String requestID = "";
    JSONObject returnRequest = new JSONObject();
    Logger log = LoggerFactory.getLogger(YandexAliceJson.class);

    public YandexAliceJson(String requestID) {
        returnRequest.put("request_id", requestID);
        returnRequest.put("payload", new JSONObject());
        returnRequest.getJSONObject("payload").put("devices", new JSONArray());
        this.requestID = requestID;
    }

    // public YandexAliceJson() {
    // returnRequest.put("payload", new JSONObject());
    // returnRequest.getJSONObject("payload").put("devices", new JSONArray());
    // }

    public YandexAliceJson(double ts, String uuid) {
        returnRequest.put("payload", new JSONObject());
        returnRequest.put("ts", ts).put("payload",
                new JSONObject().put("user_id", uuid).put("devices", new JSONArray()));
    }

    public void setUserDevices(String s) {
        JSONObject payload = new JSONObject();
        payload.put("user_id", s);
        payload.put("devices", new JSONArray());
        returnRequest.put("payload", payload);
    }

    public void setDeviceID(@Nullable YandexDevice yaDevice) {
        if (yaDevice != null) {
            returnRequest.getJSONObject("payload").getJSONArray("devices")
                    .put(new JSONObject().put("id", yaDevice.getId()));
        }
    }

    public void addCapabilities(YandexDevice yaDev) {
        JSONArray device = new JSONObject(returnRequest.get("payload").toString()).getJSONArray("devices");
        JSONArray caps = new JSONArray();
        for (YandexAliceCapabilities cp : yaDev.getCapabilities()) {
            if (cp.capabilityName.equals(YandexDevice.CAP_RANGE)) {
                caps.put(new JSONObject().put("type", cp.getCapabilityName()).put("parameters",
                        new JSONObject().put("instance", cp.getInstance())
                                .put("range",
                                        new JSONObject().put("min", cp.getMinRange()).put("max", cp.getMaxRange())
                                                .put("precision", cp.getPrecision()))
                                .put("unit", cp.getUnit()))
                        .put("retrievable", true).put("reportable", true));
            } else if (cp.capabilityName.equals(YandexDevice.CAP_COLOR_SETTINGS)) {
                if (cp.getScenesList().isEmpty()) {
                    caps.put(new JSONObject().put("type", cp.getCapabilityName())
                            .put("parameters", new JSONObject().put("color_model", "hsv")).put("retrievable", true)
                            .put("reportable", true));
                } else {
                    caps.put(new JSONObject().put("type", cp.getCapabilityName())
                            .put("parameters",
                                    new JSONObject().put("color_model", "hsv").put("color_scene",
                                            new JSONObject().put("scenes", cp.getScenesList())))
                            .put("retrievable", true).put("reportable", true));
                }
            } else if (cp.capabilityName.equals(YandexDevice.CAP_MODE)) {
                caps.put(new JSONObject().put("type", cp.getCapabilityName())
                        .put("parameters",
                                new JSONObject().put("instance", cp.getInstance()).put("modes", cp.getModes()))
                        .put("retrievable", true).put("reportable", true));
            } else if (cp.capabilityName.equals(YandexDevice.CAP_TOGGLE)) {
                caps.put(new JSONObject().put("type", cp.getCapabilityName())
                        .put("parameters", new JSONObject().put("instance", cp.getInstance())).put("retrievable", true)
                        .put("reportable", true));
            } else {
                caps.put(new JSONObject().put("type", cp.getCapabilityName()).put("parameters", new JSONObject())
                        .put("retrievable", true).put("reportable", true));
            }
        }
        for (int i = 0; i < device.length(); i++) {
            String capID = device.getJSONObject(i).get("id").toString();
            if (capID.equals(yaDev.getId())) {
                returnRequest.getJSONObject("payload").getJSONArray("devices").getJSONObject(i).put("capabilities",
                        caps);
            }
        }
    }

    public void addProperties(YandexDevice yaDev) {
        JSONArray device = new JSONObject(returnRequest.get("payload").toString()).getJSONArray("devices");
        JSONArray properties = new JSONArray();
        for (YandexAliceProperties prp : yaDev.getProperties()) {
            if (prp.getPropName().equals(YandexDevice.PROP_EVENT)) {
                properties.put(new JSONObject().put("type", prp.getPropName())
                        .put("parameters",
                                new JSONObject().put("instance", prp.getInstance()).put("events", prp.getEvents()))
                        .put("retrievable", true).put("reportable", true));
            } else if (prp.getPropName().equals(YandexDevice.PROP_FLOAT)) {
                properties.put(new JSONObject().put("type", prp.getPropName())
                        .put("parameters",
                                new JSONObject().put("instance", prp.getInstance()).put("unit", prp.getUnit()))
                        .put("retrievable", true).put("reportable", true));
            }
        }
        for (int i = 0; i < device.length(); i++) {
            String capID = device.getJSONObject(i).get("id").toString();
            if (capID.equals(yaDev.getId())) {
                returnRequest.getJSONObject("payload").getJSONArray("devices").getJSONObject(i).put("properties",
                        properties);
                // log.debug("jsonObject device {}", returnRequest);
            }
        }
    }

    public void createDevice(YandexDevice yDev) {
        JSONObject deviceObj = new JSONObject();
        deviceObj.put("id", yDev.getId()).put("name", yDev.getName()).put("type", yDev.getType())
                .put("capabilities", new JSONArray()).put("properties", new JSONArray());
        returnRequest.getJSONObject("payload").getJSONArray("devices").put(deviceObj);
    }

    public void addCapabilityState(YandexAliceCapabilities capability, State state) {
        JSONArray caps;
        if (returnRequest.getJSONObject("payload").getJSONArray("devices").getJSONObject(0).isNull("capabilities")) {
            caps = new JSONArray();
            // log.debug("cap is empty");
        } else {
            caps = returnRequest.getJSONObject("payload").getJSONArray("devices").getJSONObject(0)
                    .getJSONArray("capabilities");
            // log.debug("cap is {}", caps);
        }
        if (state instanceof OnOffType) {
            // if (capability.getCapabilityName().equals(YandexDevice.CAP_RANGE)) {
            // if (state.equals(OnOffType.OFF)) {
            // caps.put(new JSONObject().put("type", capability.getCapabilityName()).put("state",
            // new JSONObject().put("instance", capability.getInstance()).put("value", 0)));
            // }
            // } else if (capability.getCapabilityName().equals(YandexDevice.CAP_COLOR_SETTINGS)) {
            // caps.put(new JSONObject().put("type", capability.getCapabilityName()).put("state", new JSONObject()
            // .put("instance", "hsv").put("value", new JSONObject().put("h", 0).put("s", 0).put("v", 0))));
            // } else {
            boolean status = state.equals(OnOffType.ON);
            caps.put(new JSONObject().put("type", capability.getCapabilityName()).put("state",
                    new JSONObject().put("instance", capability.getInstance()).put("value", status)));
            // }
        } else if (state instanceof HSBType) {
            // log.debug("HSB");
            caps.put(new JSONObject().put("type", capability.getCapabilityName()).put("state",
                    new JSONObject().put("instance", "hsv").put("value",
                            new JSONObject().put("h", ((HSBType) state).getHue())
                                    .put("s", ((HSBType) state).getSaturation())
                                    .put("v", ((HSBType) state).getBrightness()))));
        } else if (state instanceof PercentType) {
            if (capability.getCapabilityName().equals(YandexDevice.CAP_ON_OFF)) {
                boolean status = ((PercentType) state).intValue() > 0;
                caps.put(new JSONObject().put("type", capability.getCapabilityName()).put("state",
                        new JSONObject().put("instance", capability.getInstance()).put("value", status)));
            } else {
                caps.put(new JSONObject().put("type", capability.getCapabilityName()).put("state", new JSONObject()
                        .put("instance", capability.getInstance()).put("value", ((PercentType) state).intValue())));
            }
        } else if (state instanceof DecimalType) {
            caps.put(new JSONObject().put("type", capability.getCapabilityName()).put("state", new JSONObject()
                    .put("instance", capability.getInstance()).put("value", ((Number) state).doubleValue())));
        } else if (state instanceof StringType) {
            caps.put(new JSONObject().put("type", capability.getCapabilityName()).put("state",
                    new JSONObject().put("instance", capability.getInstance()).put("value", state.toString())));
        }
        returnRequest.getJSONObject("payload").getJSONArray("devices").getJSONObject(0).put("capabilities", caps);
    }

    public void addCapabilityState(YandexDevice yaDev, YandexAliceCapabilities cap, State state) {
        if (state instanceof OnOffType) {
            boolean status = state.equals(OnOffType.ON);
            JSONArray device = returnRequest.getJSONObject("payload").getJSONArray("devices");
            for (int i = 0; i < device.length(); i++) {
                if (device.getJSONObject(i).getString("id").equals(yaDev.getId())) {
                    device.getJSONObject(i).put("capabilities",
                            new JSONArray().put(new JSONObject().put("type", cap.getCapabilityName()).put("state",
                                    new JSONObject().put("instance", cap.getInstance()).put("value", status))));
                    returnRequest.getJSONObject("payload").put("devices", device);
                }
            }
        }
    }

    public void addPropertyState(YandexAliceProperties prop, State state) {
        JSONArray props;
        if (returnRequest.getJSONObject("payload").getJSONArray("devices").getJSONObject(0).isNull("properties")) {
            props = new JSONArray();
            // log.debug("prop is empty");
        } else {
            props = returnRequest.getJSONObject("payload").getJSONArray("devices").getJSONObject(0)
                    .getJSONArray("properties");
            // log.debug("prop is {}", props);
        }

        if ((state instanceof DecimalType) || (state instanceof QuantityType)) {
            if (prop.getInstance().equals(YandexDevice.EVENT_OPEN)) {
                String st;
                if (((Number) state).intValue() == 0) {
                    st = "closed";
                } else {
                    st = "opened";
                }
                props.put(new JSONObject().put("type", prop.getPropName()).put("state",
                        new JSONObject().put("instance", prop.getInstance()).put("value", st)));
            } else {
                props.put(new JSONObject().put("type", prop.getPropName()).put("state", new JSONObject()
                        .put("instance", prop.getInstance()).put("value", ((Number) state).doubleValue())));
            }
        } else if (state instanceof OpenClosedType) {
            String st;
            if (state.toString().equals("CLOSED")) {
                st = "closed";
            } else {
                st = "opened";
            }
            props.put(new JSONObject().put("type", prop.getPropName()).put("state",
                    new JSONObject().put("instance", prop.getInstance()).put("value", st)));
        } else if (state instanceof OnOffType) {
            if (prop.getInstance().equals(YandexDevice.EVENT_MOTION)) {
                String st;
                if (state.toString().equals("ON")) {
                    st = "detected";
                } else {
                    st = "not_detected";
                }
                props.put(new JSONObject().put("type", prop.getPropName()).put("state",
                        new JSONObject().put("instance", prop.getInstance()).put("value", st)));
            } else if (prop.getInstance().equals(YandexDevice.EVENT_WATER_LEAK)) {
                String st;
                if (state.toString().equals("ON")) {
                    st = "leak";
                } else {
                    st = "dry";
                }
                props.put(new JSONObject().put("type", prop.getPropName()).put("state",
                        new JSONObject().put("instance", prop.getInstance()).put("value", st)));
            } else if ((prop.getInstance().equals(YandexDevice.EVENT_SMOKE))
                    || (prop.getInstance().equals(YandexDevice.EVENT_GAS))) {
                String st;
                if (state.toString().equals("ON")) {
                    st = "detected";
                } else {
                    st = "not_detected";
                }
                props.put(new JSONObject().put("type", prop.getPropName()).put("state",
                        new JSONObject().put("instance", prop.getInstance()).put("value", st)));
            } else if (prop.getInstance().equals(YandexDevice.EVENT_BATTERY_LEVEL)) {
                String st;
                if (state.toString().equals("ON")) {
                    st = "low";
                } else {
                    st = "normal";
                }
                props.put(new JSONObject().put("type", prop.getPropName()).put("state",
                        new JSONObject().put("instance", prop.getInstance()).put("value", st)));
            }
        }
        returnRequest.getJSONObject("payload").getJSONArray("devices").getJSONObject(0).put("properties", props);
    }

    public void addPropertyState(YandexAliceProperties prop, double intState) {
        returnRequest.getJSONObject("payload").getJSONArray("devices").getJSONObject(0).put("properties",
                new JSONArray().put(new JSONObject().put("type", prop.getPropName()).put("state",
                        new JSONObject().put("instance", prop.getInstance()).put("value", intState))));
    }

    public void addPropertyState(YandexDevice yaDev, YandexAliceProperties prop, State state) {
        if ((state instanceof DecimalType) || (state instanceof QuantityType)) {
            JSONArray device = returnRequest.getJSONObject("payload").getJSONArray("devices");
            for (int i = 0; i < device.length(); i++) {
                if (device.getJSONObject(i).getString("id").equals(yaDev.getId())) {
                    device.getJSONObject(i).put("properties",
                            new JSONArray().put(new JSONObject().put("type", prop.getPropName()).put("state",
                                    new JSONObject().put("instance", prop.getInstance()).put("value",
                                            ((Number) state).doubleValue()))));
                    returnRequest.getJSONObject("payload").put("devices", device);
                }
            }
        } else if ((state instanceof OnOffType) || (state instanceof StringType)) {
            JSONArray device = returnRequest.getJSONObject("payload").getJSONArray("devices");
            for (int i = 0; i < device.length(); i++) {
                if (device.getJSONObject(i).getString("id").equals(yaDev.getId())) {
                    device.getJSONObject(i).put("properties",
                            new JSONArray()
                                    .put(new JSONObject().put("type", prop.getPropName()).put("state", new JSONObject()
                                            .put("instance", prop.getInstance()).put("value", state.toString()))));
                    returnRequest.getJSONObject("payload").put("devices", device);
                }
            }
        } else if ((state instanceof OpenClosedType)) {
            String st;
            if (state.toString().equals("CLOSED")) {
                st = "closed";
            } else {
                st = "opened";
            }
            JSONArray device = returnRequest.getJSONObject("payload").getJSONArray("devices");
            for (int i = 0; i < device.length(); i++) {
                if (device.getJSONObject(i).getString("id").equals(yaDev.getId())) {
                    device.getJSONObject(i).put("properties",
                            new JSONArray().put(new JSONObject().put("type", prop.getPropName()).put("state",
                                    new JSONObject().put("instance", prop.getInstance()).put("value", st))));
                    returnRequest.getJSONObject("payload").put("devices", device);
                }
            }
        }
    }

    public void addError(String errorCode, String id) {
        JSONArray device = returnRequest.getJSONObject("payload").getJSONArray("devices");
        for (int i = 0; i < device.length(); i++) {
            if (device.getJSONObject(i).getString("id").equals(id)) {
                device.getJSONObject(i).put("error_code", errorCode);
                returnRequest.getJSONObject("payload").put("devices", device);
            }
        }
    }
}
