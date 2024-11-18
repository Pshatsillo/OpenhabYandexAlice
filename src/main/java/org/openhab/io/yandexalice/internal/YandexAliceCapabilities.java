/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
    private double precisionRange;
    private int maxRange;
    private int minRange;
    private String ohID;
    private String scenesOhID;
    @Nullable
    private State state = null;
    JSONArray modes = new JSONArray();
    JSONArray scenesList = new JSONArray();
    // private JSONObject temperatureK = new JSONObject();

    private ColorSettingsTemperature temperatureK = new ColorSettingsTemperature();
    private ColorSettingsModel colorSettingsModel = new ColorSettingsModel();

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

    public void setRange(int minRange, int maxRange, double precisionRange) {
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

    public double getPrecision() {
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

    public void setTemperatureK(ColorSettingsTemperature temperatureK) {
        this.temperatureK = temperatureK;
        // temperatureK.setOhID(ohID);
        // temperatureK.setTemp(true);
    }

    public ColorSettingsTemperature getTemperatureK() {
        return temperatureK;
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

    public void setColorModel(ColorSettingsModel colorSettingsModel) {
        this.colorSettingsModel = colorSettingsModel;
    }

    public ColorSettingsModel getColorModel() {
        return colorSettingsModel;
    }

    public static class ColorSettingsTemperature {
        private String ohID = "";
        private boolean isTemp;
        @Nullable
        private State state;

        public String getOhID() {
            return ohID;
        }

        public void setOhID(String ohID) {
            this.ohID = ohID;
        }

        public boolean isTemp() {
            return isTemp;
        }

        public void setTemp(boolean temp) {
            isTemp = temp;
        }

        public @Nullable State getState() {
            return state;
        }

        public void setState(State state) {
            this.state = state;
        }
    }

    public static class ColorSettingsModel {
        private String ohID = "";
        private boolean isModel;
        @Nullable
        private State state;

        public String getOhID() {
            return ohID;
        }

        public void setOhID(String ohID) {
            this.ohID = ohID;
        }

        public boolean isModel() {
            return isModel;
        }

        public void setModel(boolean isModel) {
            this.isModel = isModel;
        }

        public @Nullable State getState() {
            return state;
        }

        public void setState(State state) {
            this.state = state;
        }
    }
}
