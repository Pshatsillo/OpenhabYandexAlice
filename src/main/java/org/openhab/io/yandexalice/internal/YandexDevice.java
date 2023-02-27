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
import java.util.List;

/**
 * The {@link YandexDevice} model for Yandex item
 *
 * @author Petr Shatsillo - Initial contribution
 */
public class YandexDevice {
    private String id;
    private String name;
    private String type;
    private final List<YandexAliceProperties> properties = new ArrayList<>();
    private List<YandexAliceCapabilities> capabilities;

    public YandexDevice(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
        capabilities = new ArrayList<>();
    }

    public void addCapabilities(String capability) {
        YandexAliceCapabilities cp = new YandexAliceCapabilities();
        cp.addCapability(capability);
        capabilities.add(cp);
    }

    public List<YandexAliceCapabilities> getCapabilities() {
        return capabilities;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCapabilities(List<YandexAliceCapabilities> capabilities) {
        this.capabilities = capabilities;
    }

    public void addProperties(String propName, String instance, String unit) {
        YandexAliceProperties prop = new YandexAliceProperties(propName, instance, unit);
        this.properties.add(prop);
    }

    public List<YandexAliceProperties> getProperties() {
        return properties;
    }
}
