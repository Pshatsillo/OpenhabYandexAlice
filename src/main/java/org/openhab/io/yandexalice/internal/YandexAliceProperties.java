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
import org.openhab.core.types.State;

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
    private JSONArray events = new JSONArray();
    @Nullable
    private State state = null;

    public YandexAliceProperties(String ohItemID, String propName, String instance, String unit) {
        this.propName = propName;
        this.instance = instance;
        this.unit = unit;
        this.ohItemID = ohItemID;
        if (propName.equals(YandexDevice.PROP_EVENT)) {
            if (instance.equals(YandexDevice.EVENT_OPEN)) {
                events = new JSONArray().put(new JSONObject().put("value", "opened"))
                        .put(new JSONObject().put("value", "closed"));
            }
        }
    }

    public String getOhID() {
        return ohItemID;
    }

    public YandexAliceProperties(String propName, String instance) {
        this.propName = propName;
        this.instance = instance;
        if (propName.equals(YandexDevice.PROP_EVENT)) {
            if (instance.equals(YandexDevice.EVENT_OPEN)) {
                events = new JSONArray().put(new JSONObject().put("value", "opened"))
                        .put(new JSONObject().put("value", "closed"));
            }
        }
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

    public JSONArray getEvents() {
        return events;
    }

    public @Nullable State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
