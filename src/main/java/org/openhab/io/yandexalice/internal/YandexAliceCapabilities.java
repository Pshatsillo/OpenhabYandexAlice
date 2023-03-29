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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.core.types.State;

/**
 * The {@link YandexAliceCapabilities} model for Yandex capabilities
 *
 * @author Petr Shatsillo - Initial contribution
 */
@NonNullByDefault
public class YandexAliceCapabilities {
    String capabilityName = "";
    String instance;
    private String unit;
    private int precisionRange;
    private int maxRange;
    private int minRange;
    private String ohID;
    private String scenesOhID;
    @Nullable
    private State state = null;
    JSONArray modes = new JSONArray();
    JSONArray scenesList = new JSONArray();

    public JSONArray getModes() {
        return modes;
    }

    public void setModes(Collection<String> modesList) {
        JSONArray modes = new JSONArray();
        modesList.forEach((md) -> modes.put(new JSONObject().put("value", md)));
        this.modes = modes;
    }

    public YandexAliceCapabilities() {
        instance = "";
        unit = "";
        ohID = "";
        scenesOhID = "";
    }

    public String getOhID() {
        return ohID;
    }

    public void setOhID(String ohID) {
        this.ohID = ohID;
    }

    public void addCapability(String capability) {
        this.capabilityName = capability;
    }

    public String getCapabilityName() {
        return capabilityName;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setRange(int minRange, int maxRange, int precisionRange) {
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.precisionRange = precisionRange;
    }

    public int getMinRange() {
        return minRange;
    }

    public int getMaxRange() {
        return maxRange;
    }

    public int getPrecision() {
        return precisionRange;
    }

    public String getUnit() {
        return unit;
    }

    public @Nullable State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setScenesList(Collection<String> scenesList) {
        scenesList.forEach((scn) -> {
            this.scenesList.put(new JSONObject().put("id", scn));
        });
    }

    public JSONArray getScenesList() {
        return scenesList;
    }

    public String getScenesOhID() {
        return scenesOhID;
    }

    public void setScenesOhID(String scenesOhID) {
        this.scenesOhID = scenesOhID;
    }
}
