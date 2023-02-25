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
 * The {@link YandexAliceCapabilities} model for Yandex capabilities
 *
 * @author Petr Shatsillo - Initial contribution
 */
public class YandexAliceCapabilities {
    String capability;

    public void addCapability(String capability) {
        this.capability = capability;
    }

    public String getCapability() {
        return capability;
    };
}
