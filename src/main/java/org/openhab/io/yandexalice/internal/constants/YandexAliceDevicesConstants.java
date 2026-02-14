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
package org.openhab.io.yandexalice.internal.constants;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link YandexAliceDevicesConstants} contains constants of devices
 *
 * @author Petr Shatsillo - Initial contribution
 */
@NonNullByDefault
public class YandexAliceDevicesConstants {
    // ---------------------------------------
    public static final String DEV_CAMERA = "devices.types.camera";
    public static final String DEV_COOKING = "devices.types.cooking";
    public static final String DEV_COFFEE_MAKER = "devices.types.cooking.coffee_maker";
    public static final String DEV_KETTLE = "devices.types.cooking.kettle";
    public static final String DEV_MULTICOOKER = "devices.types.cooking.multicooker";
    public static final String DEV_DISHWASHER = "devices.types.dishwasher";
    public static final String DEV_HUMIDIFIER = "devices.types.humidifier";
    public static final String DEV_IRON = "devices.types.iron";
    public static final String DEV_LIGHT = "devices.types.light";
    public static final String DEV_LIGHT_CEILING = "devices.types.light.ceiling";
    public static final String DEV_LIGHT_STRIP = "devices.types.light.strip";
    public static final String DEV_MEDIA_DEVICE = "devices.types.media_device";
    public static final String DEV_RECEIVER = "devices.types.media_device.receiver";
    public static final String DEV_TV = "devices.types.media_device.tv";
    public static final String DEV_TV_BOX = "devices.types.media_device.tv_box";
    public static final String DEV_OPENABLE = "devices.types.openable";
    public static final String DEV_CURTAIN = "devices.types.openable.curtain";
    public static final String DEV_VALVE = "devices.types.openable.valve";
    public static final String DEV_OTHER = "devices.types.other";
    public static final String DEV_PET_DRINKING_FOUNTAIN = "devices.types.pet_drinking_fountain";
    public static final String DEV_PET_FEEDER = "devices.types.pet_feeder";
    public static final String DEV_PURIFIER = "devices.types.purifier";
    public static final String DEV_SENSOR = "devices.types.sensor";
    public static final String DEV_SENSOR_BUTTON = "devices.types.sensor.button";
    public static final String DEV_SENSOR_CLIMATE = "devices.types.sensor.climate";
    public static final String DEV_SENSOR_GAS = "devices.types.sensor.gas";
    public static final String DEV_SENSOR_ILLUMINATION = "devices.types.sensor.illumination";
    public static final String DEV_SENSOR_MOTION = "devices.types.sensor.motion";
    public static final String DEV_SENSOR_OPEN = "devices.types.sensor.open";
    public static final String DEV_SENSOR_SMOKE = "devices.types.sensor.smoke";
    public static final String DEV_SENSOR_VIBRATION = "devices.types.sensor.vibration";
    public static final String DEV_SENSOR_WATER_LEAK = "devices.types.sensor.water_leak";
    public static final String DEV_SMART_METER = "devices.types.smart_meter";
    public static final String DEV_COLD_WATER = "devices.types.smart_meter.cold_water";
    public static final String DEV_ELECTRICITY = "devices.types.smart_meter.electricity";
    public static final String DEV_GAS = "devices.types.smart_meter.gas";
    public static final String DEV_HEAT = "devices.types.smart_meter.heat";
    public static final String DEV_HOT_WATER = "devices.types.smart_meter.hot_water";
    public static final String DEV_SOCKET = "devices.types.socket";
    public static final String DEV_SWITCH = "devices.types.switch";
    public static final String DEV_THERMOSTAT = "devices.types.thermostat";
    public static final String DEV_THERMOSTAT_AC = "devices.types.thermostat.ac";
    public static final String DEV_VACUUM_CLEANER = "devices.types.vacuum_cleaner";
    public static final String DEV_WASHING_MACHINE = "devices.types.washing_machine";
    public static final Collection<String> DEV_LIST = List.of(DEV_SENSOR, DEV_SOCKET, DEV_SWITCH, DEV_LIGHT,
            DEV_LIGHT_CEILING, DEV_LIGHT_STRIP, DEV_OPENABLE, DEV_SENSOR_OPEN, DEV_CURTAIN, DEV_VALVE, DEV_THERMOSTAT,
            DEV_MEDIA_DEVICE, DEV_TV, DEV_TV_BOX, DEV_RECEIVER, DEV_HUMIDIFIER, DEV_PURIFIER, DEV_VACUUM_CLEANER,
            DEV_WASHING_MACHINE, DEV_DISHWASHER, DEV_IRON, DEV_SENSOR_VIBRATION, DEV_SENSOR_ILLUMINATION,
            DEV_SENSOR_CLIMATE, DEV_SENSOR_WATER_LEAK, DEV_SENSOR_BUTTON, DEV_SENSOR_GAS, DEV_SENSOR_SMOKE,
            DEV_PET_DRINKING_FOUNTAIN, DEV_PET_FEEDER, DEV_OTHER, DEV_CAMERA, DEV_COOKING, DEV_COFFEE_MAKER, DEV_KETTLE,
            DEV_MULTICOOKER, DEV_SENSOR_MOTION, DEV_SMART_METER, DEV_COLD_WATER, DEV_ELECTRICITY, DEV_GAS, DEV_HEAT,
            DEV_HOT_WATER, DEV_THERMOSTAT_AC);
}
