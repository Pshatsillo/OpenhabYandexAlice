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

/**
 * The {@link YandexAliceProperties} model for Yandex properties
 *
 * @author Petr Shatsillo - Initial contribution
 */
public class YandexAliceProperties {
    String propName;
    String instance;
    String unit;

    public YandexAliceProperties(String propName, String instance, String unit) {
        this.propName = propName;
        this.instance = instance;
        this.unit = unit;
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
}
