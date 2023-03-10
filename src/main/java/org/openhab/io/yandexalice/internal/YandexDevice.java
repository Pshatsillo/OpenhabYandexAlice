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
    public static final String RANGE_BRIGHTNESS = "brightness";
    public static final String RANGE_CHANNEL = "channel";
    public static final String RANGE_HUMIDITY = "humidity";
    public static final String RANGE_OPEN = "open";
    public static final String RANGE_TEMPERATURE = "temperature";
    public static final String RANGE_VOLUME = "volume";
    public static final Collection<String> DEV_LIST = List.of(DEV_SENSOR, DEV_SOCKET, DEV_SWITCH, DEV_LIGHT,
            DEV_OPENABLE, DEV_SENSOR_OPEN, DEV_CURTAIN, DEV_THERMOSTAT);
    public static final Collection<String> RANGE_LIST = List.of(RANGE_BRIGHTNESS, RANGE_CHANNEL, RANGE_HUMIDITY,
            RANGE_OPEN, RANGE_TEMPERATURE, RANGE_VOLUME);
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
