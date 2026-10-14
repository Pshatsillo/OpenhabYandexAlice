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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link YandexAliceJson} is responsible for JSON string generation
 *
 * @author Petr Shatsillo - Initial contribution
 */
@NonNullByDefault
public class YandexAliceJson {
    JsonObject returnRequest = new JsonObject();

    public YandexAliceJson(String requestID) {
        returnRequest.addProperty("request_id", requestID);
        JsonObject payload = new JsonObject();
        payload.add("devices", new JsonArray());
        returnRequest.add("payload", payload);
    }

    public YandexAliceJson(double ts, String uuid) {
        JsonObject payload = new JsonObject();
        payload.addProperty("user_id", uuid);
        payload.add("devices", new JsonArray());
        returnRequest.addProperty("ts", ts);
        returnRequest.add("payload", payload);
    }

    // Позволяет использовать объект напрямую в resp.getWriter().print(jsonObj)
    @Override
    public String toString() {
        return returnRequest.toString();
    }

    public void setUserDevices(String s) {
        JsonObject payload = new JsonObject();
        payload.addProperty("user_id", s);
        payload.add("devices", new JsonArray());
        returnRequest.add("payload", payload);
    }

    public void setDeviceID(@Nullable YandexDevice yaDevice) {
        if (yaDevice != null) {
            JsonObject devObj = new JsonObject();
            devObj.addProperty("id", yaDevice.getId());
            returnRequest.getAsJsonObject("payload").getAsJsonArray("devices").add(devObj);
        }
    }

    public void addCapabilities(YandexDevice yaDev) {
        JsonArray device = returnRequest.getAsJsonObject("payload").getAsJsonArray("devices");
        JsonArray caps = new JsonArray();

        for (YandexAliceCapabilities cp : yaDev.getCapabilities()) {
            JsonObject capObj = new JsonObject();
            capObj.addProperty("type", cp.getCapabilityName());
            capObj.addProperty("retrievable", true);
            capObj.addProperty("reportable", true);

            if (cp.capabilityName.equals(YandexDevice.CAP_RANGE)) {
                JsonObject params = new JsonObject();
                params.addProperty("instance", cp.getInstance());
                params.addProperty("unit", cp.getUnit());
                JsonObject range = new JsonObject();
                range.addProperty("min", cp.getMinRange());
                range.addProperty("max", cp.getMaxRange());
                range.addProperty("precision", cp.getPrecision());
                params.add("range", range);
                capObj.add("parameters", params);

            } else if (cp.capabilityName.equals(YandexDevice.CAP_COLOR_SETTINGS)) {
                JsonObject params = new JsonObject();
                if (cp.getColorModel().isModel()) {
                    params.addProperty("color_model", "hsv");
                }
                if (cp.getTemperatureK().isTemp()) {
                    JsonObject tempK = new JsonObject();
                    tempK.addProperty("min", 1500);
                    tempK.addProperty("max", 9000);
                    params.add("temperature_k", tempK);
                }
                if (cp.getScenesList().size() > 0) {
                    JsonObject sceneObj = new JsonObject();
                    sceneObj.add("scenes", cp.getScenesList());
                    params.add("color_scene", sceneObj);
                }
                capObj.add("parameters", params);

            } else if (cp.capabilityName.equals(YandexDevice.CAP_MODE)) {
                JsonObject params = new JsonObject();
                params.addProperty("instance", cp.getInstance());
                params.add("modes", cp.getModes());
                capObj.add("parameters", params);

            } else if (cp.capabilityName.equals(YandexDevice.CAP_TOGGLE)) {
                JsonObject params = new JsonObject();
                params.addProperty("instance", cp.getInstance());
                capObj.add("parameters", params);

            } else {
                capObj.add("parameters", new JsonObject());
            }
            caps.add(capObj);
        }

        for (int i = 0; i < device.size(); i++) {
            JsonObject dev = device.get(i).getAsJsonObject();
            if (dev.get("id").getAsString().equals(yaDev.getId())) {
                dev.add("capabilities", caps);
            }
        }
    }

    public void addProperties(YandexDevice yaDev) {
        JsonArray device = returnRequest.getAsJsonObject("payload").getAsJsonArray("devices");
        JsonArray properties = new JsonArray();

        for (YandexAliceProperties prp : yaDev.getProperties()) {
            JsonObject propObj = new JsonObject();
            propObj.addProperty("type", prp.getPropName());
            propObj.addProperty("retrievable", true);
            propObj.addProperty("reportable", true);

            JsonObject params = new JsonObject();
            params.addProperty("instance", prp.getInstance());

            if (prp.getPropName().equals(YandexDevice.PROP_EVENT)) {
                params.add("events", prp.getEvents());
            } else if (prp.getPropName().equals(YandexDevice.PROP_FLOAT)) {
                params.addProperty("unit", prp.getUnit());
            }
            propObj.add("parameters", params);
            properties.add(propObj);
        }

        for (int i = 0; i < device.size(); i++) {
            JsonObject dev = device.get(i).getAsJsonObject();
            if (dev.get("id").getAsString().equals(yaDev.getId())) {
                dev.add("properties", properties);
            }
        }
    }

    public void createDevice(YandexDevice yDev) {
        JsonObject deviceObj = new JsonObject();
        deviceObj.addProperty("id", yDev.getId());
        deviceObj.addProperty("name", yDev.getName());
        deviceObj.addProperty("type", yDev.getType());
        deviceObj.add("capabilities", new JsonArray());
        deviceObj.add("properties", new JsonArray());
        deviceObj.addProperty("room", "Test");
        returnRequest.getAsJsonObject("payload").getAsJsonArray("devices").add(deviceObj);
    }

    public void addCapabilityState(YandexAliceCapabilities capability, State state) {
        JsonObject device0 = returnRequest.getAsJsonObject("payload").getAsJsonArray("devices").get(0)
                .getAsJsonObject();
        JsonArray caps;

        if (!device0.has("capabilities") || device0.get("capabilities").isJsonNull()) {
            caps = new JsonArray();
        } else {
            caps = device0.getAsJsonArray("capabilities");
        }

        JsonObject capObj = new JsonObject();
        capObj.addProperty("type", capability.getCapabilityName());

        JsonObject stateObj = new JsonObject();
        stateObj.addProperty("instance", capability.getInstance());

        if (state instanceof OnOffType) {
            boolean status = state.equals(OnOffType.ON);
            stateObj.addProperty("value", status);
        } else if (state instanceof HSBType) {
            if (capability.instance.equals("brightness")) {
                stateObj.addProperty("value", ((HSBType) state).getBrightness().intValue());
            }
            if (capability.getCapabilityName().equals(YandexDevice.CAP_COLOR_SETTINGS)) {
                stateObj.addProperty("instance", "hsv");
                JsonObject hsvVal = new JsonObject();
                hsvVal.addProperty("h", ((HSBType) state).getHue().intValue());
                hsvVal.addProperty("s", ((HSBType) state).getSaturation().intValue());
                hsvVal.addProperty("v", ((HSBType) state).getBrightness().intValue());
                stateObj.add("value", hsvVal);
            }
            if (capability.getCapabilityName().equals(YandexDevice.CAP_ON_OFF)) {
                boolean status = ((HSBType) state).getBrightness().intValue() != 0;
                stateObj.addProperty("value", status);
            }
        } else if (state instanceof PercentType) {
            if (capability.getCapabilityName().equals(YandexDevice.CAP_ON_OFF)) {
                boolean status = ((PercentType) state).intValue() > 0;
                stateObj.addProperty("value", status);
            } else {
                stateObj.addProperty("value", ((PercentType) state).intValue());
            }
        } else if ((state instanceof DecimalType) || (state instanceof QuantityType)) {
            if (capability.getTemperatureK().isTemp()) {
                stateObj.addProperty("instance", "temperature_k");
            }
            stateObj.addProperty("value", ((Number) state).doubleValue());
        } else if (state instanceof StringType) {
            if (capability.getCapabilityName().equals(YandexDevice.CAP_COLOR_SETTINGS)) {
                stateObj.addProperty("instance", "scene");
            }
            stateObj.addProperty("value", state.toString());
        }

        capObj.add("state", stateObj);
        caps.add(capObj);
        device0.add("capabilities", caps);
    }

    public void addCapabilityState(YandexDevice yaDev, YandexAliceCapabilities cap, State state) {
        if (state instanceof OnOffType) {
            boolean status = state.equals(OnOffType.ON);
            JsonArray device = returnRequest.getAsJsonObject("payload").getAsJsonArray("devices");

            for (int i = 0; i < device.size(); i++) {
                JsonObject devObj = device.get(i).getAsJsonObject();
                if (devObj.get("id").getAsString().equals(yaDev.getId())) {
                    JsonArray caps = new JsonArray();
                    JsonObject capObj = new JsonObject();
                    capObj.addProperty("type", cap.getCapabilityName());

                    JsonObject stateObj = new JsonObject();
                    stateObj.addProperty("instance", cap.getInstance());
                    stateObj.addProperty("value", status);

                    capObj.add("state", stateObj);
                    caps.add(capObj);
                    devObj.add("capabilities", caps);
                    break;
                }
            }
        }
    }

    public void addPropertyState(YandexAliceProperties prop, State state) {
        JsonObject device0 = returnRequest.getAsJsonObject("payload").getAsJsonArray("devices").get(0)
                .getAsJsonObject();
        JsonArray props;

        if (!device0.has("properties") || device0.get("properties").isJsonNull()) {
            props = new JsonArray();
        } else {
            props = device0.getAsJsonArray("properties");
        }

        JsonObject propObj = new JsonObject();
        propObj.addProperty("type", prop.getPropName());
        JsonObject stateObj = new JsonObject();
        stateObj.addProperty("instance", prop.getInstance());

        if ((state instanceof DecimalType) || (state instanceof QuantityType)) {
            if (prop.getInstance().equals(YandexDevice.EVENT_OPEN)) {
                String st = (((Number) state).intValue() == 0) ? "closed" : "opened";
                stateObj.addProperty("value", st);
            } else {
                stateObj.addProperty("value", ((Number) state).doubleValue());
            }
        } else if (state instanceof OpenClosedType) {
            String st = state.toString().equals("CLOSED") ? "closed" : "opened";
            stateObj.addProperty("value", st);
        } else if (state instanceof OnOffType) {
            String st = "";
            if (prop.getInstance().equals(YandexDevice.EVENT_MOTION)) {
                st = state.toString().equals("ON") ? "detected" : "not_detected";
            } else if (prop.getInstance().equals(YandexDevice.EVENT_WATER_LEAK)) {
                st = state.toString().equals("ON") ? "leak" : "dry";
            } else if ((prop.getInstance().equals(YandexDevice.EVENT_SMOKE))
                    || (prop.getInstance().equals(YandexDevice.EVENT_GAS))) {
                st = state.toString().equals("ON") ? "detected" : "not_detected";
            } else if (prop.getInstance().equals(YandexDevice.EVENT_BATTERY_LEVEL)) {
                st = state.toString().equals("ON") ? "low" : "normal";
            }
            if (!st.isEmpty())
                stateObj.addProperty("value", st);
        } else if (state instanceof StringType) {
            if (prop.getInstance().equals(YandexDevice.EVENT_BUTTON)) {
                stateObj.addProperty("value", state.toString());
            }
        }

        propObj.add("state", stateObj);
        props.add(propObj);
        device0.add("properties", props);
    }

    public void addPropertyState(YandexAliceProperties prop, double intState) {
        JsonObject device0 = returnRequest.getAsJsonObject("payload").getAsJsonArray("devices").get(0)
                .getAsJsonObject();
        JsonArray props = new JsonArray();

        JsonObject propObj = new JsonObject();
        propObj.addProperty("type", prop.getPropName());
        JsonObject stateObj = new JsonObject();
        stateObj.addProperty("instance", prop.getInstance());
        stateObj.addProperty("value", intState);

        propObj.add("state", stateObj);
        props.add(propObj);
        device0.add("properties", props);
    }

    public void addPropertyState(YandexDevice yaDev, YandexAliceProperties prop, State state) {
        JsonArray device = returnRequest.getAsJsonObject("payload").getAsJsonArray("devices");
        for (int i = 0; i < device.size(); i++) {
            JsonObject devObj = device.get(i).getAsJsonObject();
            if (devObj.get("id").getAsString().equals(yaDev.getId())) {
                JsonArray props = new JsonArray();
                JsonObject propObj = new JsonObject();
                propObj.addProperty("type", prop.getPropName());
                JsonObject stateObj = new JsonObject();
                stateObj.addProperty("instance", prop.getInstance());

                if ((state instanceof DecimalType) || (state instanceof QuantityType)) {
                    stateObj.addProperty("value", ((Number) state).doubleValue());
                } else if ((state instanceof OnOffType) || (state instanceof StringType)) {
                    stateObj.addProperty("value", state.toString());
                } else if ((state instanceof OpenClosedType)) {
                    String st = state.toString().equals("CLOSED") ? "closed" : "opened";
                    stateObj.addProperty("value", st);
                }

                propObj.add("state", stateObj);
                props.add(propObj);
                devObj.add("properties", props);
                break;
            }
        }
    }

    public void addError(String errorCode, String id) {
        JsonArray device = returnRequest.getAsJsonObject("payload").getAsJsonArray("devices");
        for (int i = 0; i < device.size(); i++) {
            JsonObject devObj = device.get(i).getAsJsonObject();
            if (devObj.get("id").getAsString().equals(id)) {
                devObj.addProperty("error_code", errorCode);
                break;
            }
        }
    }
}
