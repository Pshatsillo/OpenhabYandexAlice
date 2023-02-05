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
package org.openhab.binding.yandexalice.internal;

import static org.openhab.binding.yandexalice.internal.YandexAliceBindingConstants.*;

import java.util.Set;

import javax.servlet.ServletException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

/**
 * The {@link YandexAliceHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Petr Shatsillo - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.yandexalice", service = ThingHandlerFactory.class)
public class YandexAliceHandlerFactory extends BaseThingHandlerFactory {
    private final HttpService httpService;
    private @NonNullByDefault({}) YandexAliceCallbackServlet yandexHTTPCallback;
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_SAMPLE);

    @Activate
    public YandexAliceHandlerFactory(final @Reference HttpService httpService) {
        this.httpService = httpService;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_SAMPLE.equals(thingTypeUID)) {
            return new YandexAliceHandler(thing);
        }

        return null;
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        try {
            yandexHTTPCallback = new YandexAliceCallbackServlet();
            this.httpService.registerServlet("/yandex", yandexHTTPCallback, null,
                    this.httpService.createDefaultHttpContext());
        } catch (ServletException | NamespaceException ignored) {
        }
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        this.httpService.unregister("/yandex");
        super.deactivate(componentContext);
    }
}
