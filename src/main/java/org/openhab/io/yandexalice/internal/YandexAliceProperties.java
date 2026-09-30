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
import org.openhab.core.types.State;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link YandexAliceProperties} model for Yandex properties
 *
 * @author Petr Shatsillo - Initial contribution
 */
@NonNullByDefault
public class YandexAliceProperties {

    private String propName;
    private String instance;
    private String unit = "";
    private String ohItemID = "";
    private JsonArray events = new JsonArray();

    @Nullable
    private State state = null;

    private JsonObject createValueObj(String val) {
        JsonObject o = new JsonObject();
        o.addProperty("value", val);
        return o;
    }

    public YandexAliceProperties(String ohItemID, String propName, String instance, String unit) {
        this.propName = propName;
        this.instance = instance;
        this.unit = unit;
        this.ohItemID = ohItemID;

        if (propName.equals(YandexDevice.PROP_EVENT)) {
            initEvents(instance);
        }
    }

    public YandexAliceProperties(String propName, String instance) {
        this.propName = propName;
        this.instance = instance;

        if (propName.equals(YandexDevice.PROP_EVENT)) {
            initEvents(instance);
        }
    }

    private void initEvents(String inst) {
        switch (inst) {
            case YandexDevice.EVENT_OPEN:
                events = new JsonArray();
                events.add(createValueObj("opened"));
                events.add(createValueObj("closed"));
                break;
            case YandexDevice.EVENT_MOTION:
                events = new JsonArray();
                events.add(createValueObj("detected"));
                events.add(createValueObj("not_detected"));
                break;
            case YandexDevice.EVENT_WATER_LEAK:
                events = new JsonArray();
                events.add(createValueObj("dry"));
                events.add(createValueObj("leak"));
                break;
            case YandexDevice.EVENT_SMOKE:
            case YandexDevice.EVENT_GAS:
                events = new JsonArray();
                events.add(createValueObj("detected"));
                events.add(createValueObj("not_detected"));
                events.add(createValueObj("high"));
                break;
            case YandexDevice.EVENT_BATTERY_LEVEL:
                events = new JsonArray();
                events.add(createValueObj("low"));
                events.add(createValueObj("normal"));
                break;
            case YandexDevice.EVENT_BUTTON:
                events = new JsonArray();
                events.add(createValueObj("click"));
                events.add(createValueObj("double_click"));
                events.add(createValueObj("long_press"));
                break;
        }
    }

    public String getOhID() {
        return ohItemID;
    }

    public String getPropName() {
        return propName;
    }

    public String getInstance() {
        return instance;
    }

    public String getUnit() {
        return unit;
    }

    public JsonArray getEvents() {
        return events;
    }

    public @Nullable State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
