/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
    public static final String UNIT_VOLT = "unit.volt";
    public static final String UNIT_AMPERE = "unit.ampere";
    public static final String UNIT_LUX = "unit.illumination.lux";
    public static final String UNIT_MCG_M3 = "unit.density.mcg_m3";
    public static final String UNIT_WATT = "unit.watt";
    public static final String UNIT_BAR = "unit.pressure.bar";
    public static final String UNIT_MMHD = "unit.pressure.mmhg";
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
    public static final String DEV_MEDIA_DEVICE = "devices.types.media_device";
    public static final String DEV_TV = "devices.types.media_device.tv";
    public static final String DEV_TV_BOX = "devices.types.media_device.tv_box";
    public static final String DEV_RECEIVER = "devices.types.media_device.receiver";
    public static final String DEV_HUMIDIFIER = "devices.types.humidifier";
    public static final String DEV_PURIFIER = "devices.types.purifier";
    public static final String DEV_VACUUM_CLEANER = "devices.types.vacuum_cleaner";
    public static final String DEV_WASHING_MACHINE = "devices.types.washing_machine";
    public static final String DEV_DISHWASHER = "devices.types.dishwasher";
    public static final String DEV_IRON = "devices.types.iron";
    public static final String DEV_SENSOR_VIBRATION = "devices.types.sensor.vibration";
    public static final String DEV_SENSOR_ILLUMINATION = "devices.types.sensor.illumination";
    public static final String DEV_SENSOR_CLIMATE = "devices.types.sensor.climate";
    public static final String DEV_SENSOR_WATER_LEAK = "devices.types.sensor.water_leak";
    public static final String DEV_SENSOR_BUTTON = "devices.types.sensor.button";
    public static final String DEV_SENSOR_GAS = "devices.types.sensor.gas";
    public static final String DEV_SENSOR_SMOKE = "devices.types.sensor.smoke";
    public static final String DEV_PET_DRINKING_FOUNTAIN = "devices.types.pet_drinking_fountain";
    public static final String DEV_PET_FEEDER = "devices.types.pet_feeder";
    public static final String DEV_KETTLE = "devices.types.cooking.kettle";
    public static final String DEV_COFFEE_MAKER = "devices.types.cooking.coffee_maker";
    public static final String DEV_OTHER = "devices.types.other";
    public static final String FLOAT_AMPERAGE = "amperage";
    public static final String FLOAT_BATTERY_LEVEL = "battery_level";
    public static final String FLOAT_CO2 = "co2_level";
    public static final String FLOAT_FOOD_LEVEL = "food_level";
    public static final String FLOAT_HUMIDITY = "humidity";
    public static final String FLOAT_ILLUMINATION = "illumination";
    public static final String FLOAT_PM1_DENSITY = "pm1_density";
    public static final String FLOAT_PM25_DENSITY = "pm2.5_density";
    public static final String FLOAT_PM10_DENSITY = "pm10_density";
    public static final String FLOAT_POWER = "power";
    public static final String FLOAT_PRESSURE = "pressure";
    public static final String FLOAT_TEMP = "temperature";
    public static final String FLOAT_TVOC = "tvoc";
    public static final String FLOAT_VOLTAGE = "voltage";
    public static final String FLOAT_WATER_LEVEL = "water_level";
    public static final String CAP_ON_OFF = "devices.capabilities.on_off";
    public static final String CAP_COLOR_SETTINGS = "devices.capabilities.color_setting";
    public static final String CAP_RANGE = "devices.capabilities.range";
    public static final String CAP_MODE = "devices.capabilities.mode";
    public static final String CAP_TOGGLE = "devices.capabilities.toggle";
    public static final String RANGE_BRIGHTNESS = "brightness";
    public static final String RANGE_CHANNEL = "channel";
    public static final String RANGE_HUMIDITY = "humidity";
    public static final String RANGE_OPEN = "open";
    public static final String RANGE_TEMPERATURE = "temperature";
    public static final String RANGE_VOLUME = "volume";
    public static final String MODE_CLEANUP = "cleanup_mode";
    public static final String MODE_COFFEE = "coffee_mode";
    public static final String MODE_DISHWASHING = "dishwashing";
    public static final String MODE_FAN_SPEED = "fan_speed";
    public static final String MODE_HEAT = "heat";
    public static final String MODE_INPUT_SOURCE = "input_source";
    public static final String MODE_PROGRAM = "program";
    public static final String MODE_SWING = "swing";
    public static final String MODE_TEA = "tea_mode";
    public static final String MODE_THERMOSTAT = "thermostat";
    public static final String MODE_WORK_SPEED = "work_speed";
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
    public static final String TOGGLE_BACKLIGHT = "backlight";
    public static final String TOGGLE_MUTE = "mute";
    public static final String TOGGLE_CONTROLS_LOCKED = "controls_locked";
    public static final String TOGGLE_IONIZATION = "ionization";
    public static final String TOGGLE_KEEP_WARM = "keep_warm";
    public static final String TOGGLE_OSCILLATION = "oscillation";
    public static final String TOGGLE_PAUSE = "pause";
    public static final String EVENT_VIBRATION = "vibration";
    public static final String EVENT_OPEN = "open";
    public static final String EVENT_BUTTON = "button";
    public static final String EVENT_MOTION = "motion";
    public static final String EVENT_SMOKE = "smoke";
    public static final String EVENT_GAS = "gas";
    public static final String EVENT_BATTERY_LEVEL = "battery_level";
    public static final String EVENT_WATER_LEVEL = "water_level";
    public static final String EVENT_WATER_LEAK = "water_leak";
    public static final String SCENE_ALARM = "alarm";
    public static final String SCENE_ALICE = "alice";
    public static final String SCENE_CANDLE = "candle";
    public static final String SCENE_DINNER = "dinner";
    public static final String SCENE_FANTASY = "fantasy";
    public static final String SCENE_GARLAND = "garland";
    public static final String SCENE_JUNGLE = "jungle";
    public static final String SCENE_MOVIE = "movie";
    public static final String SCENE_NEON = "neon";
    public static final String SCENE_NIGHT = "night";
    public static final String SCENE_OCEAN = "ocean";
    public static final String SCENE_PARTY = "party";
    public static final String SCENE_READING = "reading";
    public static final String SCENE_REST = "rest";
    public static final String SCENE_ROMANCE = "romance";
    public static final String SCENE_SIREN = "siren";
    public static final String SCENE_SUNRISE = "sunrise";
    public static final String SCENE_SUNSET = "sunset";
    public static final Collection<String> DEV_LIST = List.of(DEV_SENSOR, DEV_SOCKET, DEV_SWITCH, DEV_LIGHT,
            DEV_OPENABLE, DEV_SENSOR_OPEN, DEV_CURTAIN, DEV_THERMOSTAT, DEV_MEDIA_DEVICE, DEV_TV, DEV_TV_BOX,
            DEV_RECEIVER, DEV_HUMIDIFIER, DEV_PURIFIER, DEV_VACUUM_CLEANER, DEV_WASHING_MACHINE, DEV_DISHWASHER,
            DEV_IRON, DEV_SENSOR_VIBRATION, DEV_SENSOR_ILLUMINATION, DEV_SENSOR_CLIMATE, DEV_SENSOR_WATER_LEAK,
            DEV_SENSOR_BUTTON, DEV_SENSOR_GAS, DEV_SENSOR_SMOKE, DEV_PET_DRINKING_FOUNTAIN, DEV_PET_FEEDER, DEV_KETTLE,
            DEV_COFFEE_MAKER, DEV_OTHER);
    public static final Collection<String> RANGE_LIST = List.of(RANGE_BRIGHTNESS, RANGE_CHANNEL, RANGE_HUMIDITY,
            RANGE_OPEN, RANGE_TEMPERATURE, RANGE_VOLUME);
    public static final Collection<String> OPER_LIST = List.of(OPER_AUTO, OPER_MAX, OPER_MIN, OPER_NORMAL, OPER_TURBO,
            OPER_ECO, OPER_SMART, OPER_PREHEAT, OPER_HEAT, OPER_FAN_ONLY, OPER_DRY, OPER_COOL, OPER_MEDIUM, OPER_LOW,
            OPER_HIGH, OPER_QUIET, OPER_EXPRESS, OPER_SLOW, OPER_FAST, OPER_VERTICAL, OPER_STATIONARY, OPER_HORIZONTAL,
            OPER_ONE, OPER_TWO, OPER_THREE, OPER_FOUR, OPER_FIVE, OPER_SIX, OPER_SEVEN, OPER_EIGHT, OPER_NINE,
            OPER_TEN);
    public static final Collection<String> DEFAULT_CLEANUP = List.of(OPER_AUTO);
    public static final Collection<String> DEFAULT_COFFEE = List.of(OPER_AUTO);
    public static final Collection<String> DEFAULT_DISHWASHING = List.of(OPER_AUTO);
    public static final Collection<String> DEFAULT_FAN_SPEED = List.of(OPER_AUTO, OPER_HIGH, OPER_LOW, OPER_MEDIUM,
            OPER_TURBO);
    public static final Collection<String> DEFAULT_INPUT_SOURCE = List.of(OPER_ONE, OPER_TWO, OPER_THREE, OPER_FOUR);
    public static final Collection<String> DEFAULT_PROGRAM = List.of(OPER_ONE, OPER_TWO, OPER_THREE, OPER_FOUR);
    public static final Collection<String> DEFAULT_SWING = List.of(OPER_AUTO, OPER_HORIZONTAL, OPER_STATIONARY,
            OPER_VERTICAL);
    public static final Collection<String> DEFAULT_THERMOSTAT = List.of(OPER_AUTO, OPER_COOL, OPER_DRY, OPER_FAN_ONLY,
            OPER_HEAT, OPER_PREHEAT);
    public static final Collection<String> DEFAULT_TEA = List.of(OPER_AUTO);
    public static final Collection<String> DEFAULT_WORK_SPEED = List.of(OPER_AUTO, OPER_FAST, OPER_MAX, OPER_MEDIUM,
            OPER_MIN, OPER_SLOW, OPER_TURBO);

    public static Collection<String> DEFAULT_HEAT = List.of(OPER_AUTO, OPER_MAX, OPER_MIN, OPER_NORMAL, OPER_TURBO);
    public static final Collection<String> TOGGLE_LIST = List.of(TOGGLE_BACKLIGHT, TOGGLE_MUTE, TOGGLE_PAUSE,
            TOGGLE_IONIZATION, TOGGLE_OSCILLATION, TOGGLE_CONTROLS_LOCKED, TOGGLE_KEEP_WARM);
    public static final Collection<String> FLOAT_LIST = List.of(FLOAT_AMPERAGE, FLOAT_BATTERY_LEVEL, FLOAT_CO2,
            FLOAT_FOOD_LEVEL, FLOAT_HUMIDITY, FLOAT_ILLUMINATION, FLOAT_PM1_DENSITY, FLOAT_PM25_DENSITY,
            FLOAT_PM10_DENSITY, FLOAT_POWER, FLOAT_PRESSURE, FLOAT_TEMP, FLOAT_TVOC, FLOAT_VOLTAGE, FLOAT_WATER_LEVEL);
    public static final Collection<String> EVENT_LIST = List.of(EVENT_OPEN, EVENT_SMOKE, EVENT_BUTTON, EVENT_GAS,
            EVENT_MOTION, EVENT_BATTERY_LEVEL, EVENT_VIBRATION, EVENT_WATER_LEVEL, EVENT_WATER_LEAK);
    public static final Collection<String> SCENES_LIST = List.of(SCENE_ALARM, SCENE_ALICE, SCENE_CANDLE, SCENE_DINNER,
            SCENE_FANTASY, SCENE_GARLAND, SCENE_JUNGLE, SCENE_MOVIE, SCENE_NEON, SCENE_NIGHT, SCENE_OCEAN, SCENE_PARTY,
            SCENE_READING, SCENE_REST, SCENE_ROMANCE, SCENE_SIREN, SCENE_SUNRISE, SCENE_SUNSET);
    private final String id;
    private final String name;
    private final String type;
    private final List<YandexAliceProperties> properties = new ArrayList<>();
    private final List<YandexAliceCapabilities> capabilities;
    private State state;
    private Collection<String> scenesList = new ArrayList<>();;
    private String scenesOhID;

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
        scenesOhID = "";
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
                if (!scenesList.isEmpty()) {
                    cp.setScenesList(scenesList);
                    cp.setScenesOhID(scenesOhID);
                }
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
            int maxRange, Double precision) {
        YandexAliceCapabilities cp = new YandexAliceCapabilities();
        cp.addCapability(capability);
        cp.setInstance(instance);
        cp.setUnit(unit);
        cp.setRange(minRange, maxRange, precision);
        cp.setOhID(ohID);
        if (!scenesList.isEmpty()) {
            if (capability.equals(CAP_COLOR_SETTINGS)) {
                cp.setScenesList(scenesList);
                cp.setScenesOhID(scenesOhID);
            }
        }
        capabilities.add(cp);
    }

    public void addCapabilities(String ohID, String capability, String instance, Collection<String> modesCol) {
        YandexAliceCapabilities cp = new YandexAliceCapabilities();
        cp.addCapability(capability);
        cp.setInstance(instance);
        cp.setModes(modesCol);
        cp.setOhID(ohID);
        if (!scenesList.isEmpty()) {
            if (capability.equals(CAP_COLOR_SETTINGS)) {
                cp.setScenesList(scenesList);
                cp.setScenesOhID(scenesOhID);
            }
        }
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

    public void setSceneColorCapabilities(Collection<String> scenesList, String scenesOhID) {
        this.scenesList = scenesList;
        this.scenesOhID = scenesOhID;
        for (YandexAliceCapabilities capability : capabilities) {
            if (capability.capabilityName.equals(CAP_COLOR_SETTINGS)) {
                capability.setScenesList(scenesList);
                capability.setScenesOhID(scenesOhID);
            }
        }
    }
}
