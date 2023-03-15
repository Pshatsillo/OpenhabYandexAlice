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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.State;

/**
 * The {@link YandexDevice} model for Yandex item
 *
 * @author Petr Shatsillo - Initial contribution
 */
@NonNullByDefault
public class YandexDevice {
    public static final String UNIT_TEMP_KELVIN = "unit.temperature.kelvin";
    public static final String UNIT_TEMP_CELSIUS = "unit.temperature.celsius";
    public static final String UNIT_PERCENT = "unit.percent";
    public static final String UNIT_PPM = "unit.ppm";
    public static final String PROP_FLOAT = "devices.properties.float";
    public static final String PROP_EVENT = "devices.properties.event";
    public static final String DEV_SENSOR = "devices.types.sensor";
    public static final String DEV_SOCKET = "devices.types.socket";
    public static final String DEV_SWITCH = "devices.types.switch";
    public static final String DEV_LIGHT = "devices.types.light";
    public static final String DEV_OPENABLE = "devices.types.openable";
    public static final String DEV_SENSOR_OPEN = "devices.types.sensor.open";
    public static final String DEV_CURTAIN = "devices.types.openable.curtain";
    public static final String DEV_THERMOSTAT = "devices.types.thermostat";
    public static final String INS_TEMP = "temperature";
    public static final String INS_HUMIDITY = "humidity";
    public static final String INS_CO2 = "co2_level";
    public static final String INS_OPEN = "open";
    public static final String CAP_ON_OFF = "devices.capabilities.on_off";
    public static final String CAP_COLOR_SETTINGS = "devices.capabilities.color_setting";
    public static final String CAP_RANGE = "devices.capabilities.range";
    public static final String CAP_MODE = "devices.capabilities.mode";
    public static final String RANGE_BRIGHTNESS = "brightness";
    public static final String RANGE_CHANNEL = "channel";
    public static final String RANGE_HUMIDITY = "humidity";
    public static final String RANGE_OPEN = "open";
    public static final String RANGE_TEMPERATURE = "temperature";
    public static final String RANGE_VOLUME = "volume";
    public static final String MODE_HEAT = "heat";
    public static final String OPER_AUTO = "auto";
    public static final String OPER_MAX = "max";
    public static final String OPER_MIN = "min";
    public static final String OPER_NORMAL = "normal";
    public static final String OPER_TURBO = "turbo";
    public static final String OPER_ECO = "eco";
    public static final String OPER_SMART = "smart";
    public static final String OPER_COOL = "cool";
    public static final String OPER_DRY = "dry";
    public static final String OPER_FAN_ONLY = "fan_only";
    public static final String OPER_HEAT = "heat";
    public static final String OPER_PREHEAT = "preheat";
    public static final String OPER_HIGH = "high";
    public static final String OPER_LOW = "low";
    public static final String OPER_MEDIUM = "medium";
    public static final String OPER_FAST = "fast";
    public static final String OPER_SLOW = "slow";
    public static final String OPER_EXPRESS = "express";
    public static final String OPER_QUIET = "quiet";
    public static final String OPER_HORIZONTAL = "horizontal";
    public static final String OPER_STATIONARY = "stationary";
    public static final String OPER_VERTICAL = "vertical";
    public static final String OPER_ONE = "one";
    public static final String OPER_TWO = "two";
    public static final String OPER_THREE = "three";
    public static final String OPER_FOUR = "four";
    public static final String OPER_FIVE = "five";
    public static final String OPER_SIX = "six";
    public static final String OPER_SEVEN = "seven";
    public static final String OPER_EIGHT = "eight";
    public static final String OPER_NINE = "nine";
    public static final String OPER_TEN = "ten";
    public static final Collection<String> DEV_LIST = List.of(DEV_SENSOR, DEV_SOCKET, DEV_SWITCH, DEV_LIGHT,
            DEV_OPENABLE, DEV_SENSOR_OPEN, DEV_CURTAIN, DEV_THERMOSTAT);
    public static final Collection<String> RANGE_LIST = List.of(RANGE_BRIGHTNESS, RANGE_CHANNEL, RANGE_HUMIDITY,
            RANGE_OPEN, RANGE_TEMPERATURE, RANGE_VOLUME);
    public static final Collection<String> OPER_LIST = List.of(OPER_AUTO, OPER_MAX, OPER_MIN, OPER_NORMAL, OPER_TURBO,
            OPER_ECO, OPER_SMART, OPER_PREHEAT, OPER_HEAT, OPER_FAN_ONLY, OPER_DRY, OPER_COOL, OPER_MEDIUM, OPER_LOW,
            OPER_HIGH, OPER_QUIET, OPER_EXPRESS, OPER_SLOW, OPER_FAST, OPER_VERTICAL, OPER_STATIONARY, OPER_HORIZONTAL,
            OPER_ONE,OPER_TWO,OPER_THREE,OPER_FOUR,OPER_FIVE,OPER_SIX,OPER_SEVEN,OPER_EIGHT,OPER_NINE,OPER_TEN);

    public static Collection<String> DEFAULT_HEAT = List.of(OPER_AUTO, OPER_MAX, OPER_MIN, OPER_NORMAL, OPER_TURBO);

    private final String id;
    private final String name;
    private final String type;
    private final List<YandexAliceProperties> properties = new ArrayList<>();
    private final List<YandexAliceCapabilities> capabilities;
    private State state;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public YandexDevice(String id, String name, String type, State state) {
        this.id = id;
        this.name = name;
        this.type = type;
        capabilities = new ArrayList<>();
        this.state = state;
    }

    public void addCapabilities(String ohID, String capability) {
        YandexAliceCapabilities cp = new YandexAliceCapabilities();
        cp.setOhID(ohID);
        switch (capability) {
            case CAP_ON_OFF:
                cp.addCapability(capability);
                cp.setInstance("on");
                break;
            case CAP_COLOR_SETTINGS:
                cp.addCapability(capability);
                break;
            case CAP_RANGE:
                cp.addCapability(capability);
                cp.setInstance("brightness");
                cp.setUnit(UNIT_PERCENT);
                cp.setRange(0, 100, 1);
                break;
        }
        capabilities.add(cp);
    }

    public void addCapabilities(String ohID, String capability, String instance, String unit, int minRange,
            int maxRange, int precision) {
        YandexAliceCapabilities cp = new YandexAliceCapabilities();
        cp.addCapability(capability);
        cp.setInstance(instance);
        cp.setUnit(unit);
        cp.setRange(minRange, maxRange, precision);
        cp.setOhID(ohID);
        capabilities.add(cp);
    }

    public void addCapabilities(String ohID, String capability, String instance, Collection<String> modesCol) {
        YandexAliceCapabilities cp = new YandexAliceCapabilities();
        cp.addCapability(capability);
        cp.setInstance(instance);
        cp.setModes(modesCol);
        cp.setOhID(ohID);
        capabilities.add(cp);
    }

    public List<YandexAliceCapabilities> getCapabilities() {
        return capabilities;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void addProperties(String ohItemID, String propName, String instance, String unit) {
        YandexAliceProperties prop = new YandexAliceProperties(ohItemID, propName, instance, unit);
        this.properties.add(prop);
    }

    public void addProperties(String propName, String instance) {
        YandexAliceProperties prop = new YandexAliceProperties(propName, instance);
        this.properties.add(prop);
    }

    public List<YandexAliceProperties> getProperties() {
        return properties;
    }
}
