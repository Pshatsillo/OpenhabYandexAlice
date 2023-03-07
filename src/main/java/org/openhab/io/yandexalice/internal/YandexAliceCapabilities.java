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

    public YandexAliceCapabilities() {
        instance = "";
        unit = "";
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
}
