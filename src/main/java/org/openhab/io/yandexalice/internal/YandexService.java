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

import static org.openhab.io.yandexalice.internal.constants.YandexAliceDevicesConstants.DEV_CURTAIN;
import static org.openhab.io.yandexalice.internal.constants.YandexAliceDevicesConstants.DEV_LIGHT;
import static org.openhab.io.yandexalice.internal.constants.YandexAliceDevicesConstants.DEV_LIST;
import static org.openhab.io.yandexalice.internal.constants.YandexAliceDevicesConstants.DEV_OPENABLE;
import static org.openhab.io.yandexalice.internal.constants.YandexAliceDevicesConstants.DEV_SENSOR;
import static org.openhab.io.yandexalice.internal.constants.YandexAliceDevicesConstants.DEV_SENSOR_OPEN;
import static org.openhab.io.yandexalice.internal.constants.YandexAliceDevicesConstants.DEV_SMART_METER;
import static org.openhab.io.yandexalice.internal.constants.YandexAliceDevicesConstants.DEV_SOCKET;
import static org.openhab.io.yandexalice.internal.constants.YandexAliceDevicesConstants.DEV_SWITCH;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventFilter;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.id.InstanceUUID;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.items.events.ItemStateEvent;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class starts communication between openhab and yandex.
 *
 * @author Petr Shatsillo - Initial contribution
 *
 */
@NonNullByDefault
@Component(service = { YandexService.class,
        EventSubscriber.class }, configurationPid = "org.openhab.yandexalice", property = Constants.SERVICE_PID
                + "=org.openhab.yandexalice")
@ConfigurableService(category = "io", label = "Yandex Alice", description_uri = YandexService.CONFIG_URI)
public class YandexService implements EventSubscriber {
    protected static final String CONFIG_URI = "io:yandexalice";
    // private static final String CFG_TOKEN = "token";
    private static final String CFG_SKILLID = "skillID";
    private static final String CFG_OAUTH = "oauth";
    private static @Nullable ThingRegistry things;
    private static @Nullable ItemChannelLinkRegistry link;
    private final Logger logger = LoggerFactory.getLogger(YandexService.class);
    private final HttpClient httpClient;
    protected static @Nullable ItemRegistry itemRegistry;
    protected static @Nullable EventPublisher eventPublisher;
    private final @NonNullByDefault({}) YandexAliceCallbackServlet yandexHTTPCallback;
    private final HttpService httpService;
    // private final HashMap<String, String> yandexId = new HashMap<>();
    private static boolean action;
    // private String yandexToken = "";
    private final YandexAliceCredits credit = new YandexAliceCredits();
    private static String uuid = "";
    private static final HashMap<String, YandexDevice> yandexDevicesList = new HashMap<>();
    private @Nullable ScheduledFuture<?> refreshPollingJob;
    public static List<String> devicesList = new ArrayList<>();
    private int devRefreshTime = 0;
    protected final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(ThreadPoolManager.THREAD_POOL_NAME_COMMON);

    @Activate
    public YandexService(final @Reference HttpClientFactory httpClientFactory,
            final @Reference ItemRegistry itemRegistry, final @Reference EventPublisher eventPublisher,
            final @Reference HttpService httpService, final @Reference ThingRegistry things,
            final @Reference ItemChannelLinkRegistry link) {
        this.httpClient = httpClientFactory.createHttpClient("yandexalice");
        this.httpService = httpService;
        this.httpClient.setStopTimeout(0);
        this.httpClient.setMaxConnectionsPerDestination(200);
        this.httpClient.setConnectTimeout(30000);
        this.httpClient.setFollowRedirects(false);

        yandexHTTPCallback = new YandexAliceCallbackServlet();
        YandexService.itemRegistry = itemRegistry;
        YandexService.eventPublisher = eventPublisher;
        YandexService.things = things;
        YandexService.link = link;
        getItemsList();
        getDevicesList();
        uuid = InstanceUUID.get();

        ScheduledFuture<?> refreshPollingJob = this.refreshPollingJob;
        if (refreshPollingJob == null || refreshPollingJob.isCancelled()) {
            this.refreshPollingJob = scheduler.scheduleWithFixedDelay(this::refresh, 5, 60, TimeUnit.SECONDS);
        }
    }

    private static void getDevicesList() {
        try {
            Document doc = Jsoup.connect("https://yandex.ru/dev/dialogs/smart-home/doc/ru/concepts/device-types")
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 YaBrowser/24.10.0.0 Safari/537.36")
                    .header("Cookie", "yandexuid=8427797031695969902").get();
            Elements attrs = doc.getElementsByAttribute("yfm_patched");
            for (Element dev : attrs) {
                if (!devicesList.contains(dev.attr("alt"))) {
                    devicesList.add(dev.attr("alt"));
                }
            }
            Logger logger = LoggerFactory.getLogger(YandexService.class);
            logger.debug("Jsoup: done");
        } catch (IOException ignored) {
        }
    }

    private void refresh() {
        logger.debug("refreshing...");
        if (devRefreshTime == 60) {
            getDevicesList();
            devRefreshTime = 0;
        } else {
            devRefreshTime++;
        }
        YandexAliceJson eventJson = new YandexAliceJson((double) System.currentTimeMillis() / 1000L, uuid);
        if (itemRegistry != null) {
            Collection<Item> itemsList = Objects.requireNonNull(itemRegistry).getItems();
            for (Item item : itemsList) {
                if (!(item.getState() instanceof PercentType)) {
                    if ((item.getState() instanceof DecimalType) || (item.getState() instanceof QuantityType)) {
                        YandexDevice yaDev = yandexDevicesList.get(item.getName());
                        if (yaDev != null) {
                            eventJson.setDeviceID(yaDev);
                            yaDev.getProperties().forEach(
                                    (property) -> eventJson.addPropertyState(yaDev, property, item.getState()));
                            yaDev.getCapabilities()
                                    .forEach((cap) -> eventJson.addCapabilityState(yaDev, cap, item.getState()));
                            updateCallback(eventJson.returnRequest.toString());
                        }
                    }
                }
            }
        }
    }

    private void getItemsList() {
        getItemsList("");
    }

    @Activate
    protected void activate(BundleContext context, Map<String, ?> config) {
        logger.warn("Activate Config is {}", config);
        // if (config.get(CFG_TOKEN) != null) {
        // // yandexToken = config.get(CFG_TOKEN).toString();
        // }
        if (config.get(CFG_SKILLID) != null) {
            credit.setSkillID((String) config.get(CFG_SKILLID));
        }
        if (config.get(CFG_OAUTH) != null) {
            credit.setoAuth((String) config.get(CFG_OAUTH));
        }
        try {
            this.httpService.registerServlet("/yandex", yandexHTTPCallback, null,
                    this.httpService.createDefaultHttpContext());
        } catch (ServletException | NamespaceException ignored) {
        }
        // getInfoFromYandex();
    }

    @Modified
    protected void modified(Map<String, ?> config) {
        logger.warn("modified Config is {}", config);
        // if (config.get(CFG_TOKEN) != null) {
        // // yandexToken = config.get(CFG_TOKEN).toString();
        // }
        if (config.get(CFG_SKILLID) != null) {
            credit.setSkillID((String) config.get(CFG_SKILLID));
        }
        if (config.get(CFG_OAUTH) != null) {
            credit.setoAuth((String) config.get(CFG_OAUTH));
        }
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return Set.of(ItemStateEvent.TYPE);
    }

    @Override
    public @Nullable EventFilter getEventFilter() {
        return null;
    }

    @Override
    public void receive(Event event) {
        // TODO Action
        // logger.debug("event {}, {}, {}, {}, {}", event.getPayload(), event.getSource(), event.getType(),
        // event.getTopic(), event);
        try {
            if (!action) {
                ItemStateEvent ise = (ItemStateEvent) event;
                String name = ise.getItemName();
                State state = ise.getItemState();
                try {
                    if (itemRegistry != null) {
                        Item item = Objects.requireNonNull(itemRegistry).getItem(name);
                        if (item.hasTag("Yandex")) {
                            if (state instanceof PercentType) {
                                YandexDevice yaDev;
                                yaDev = yandexDevicesList.get(name);
                                if (yaDev != null) {
                                    if (!yaDev.getState().equals(item.getState())) {
                                        YandexAliceJson eventJson = new YandexAliceJson(
                                                (double) System.currentTimeMillis() / 1000L, uuid);
                                        eventJson.setDeviceID(yaDev);
                                        for (YandexAliceCapabilities cap : yaDev.getCapabilities()) {
                                            eventJson.addCapabilityState(cap, state);
                                        }
                                        updateCallback(eventJson.returnRequest.toString());
                                        yaDev.setState(item.getState());
                                    }
                                }
                            } else if (state instanceof OnOffType) {
                                YandexDevice yaDev;
                                yaDev = yandexDevicesList.get(name);
                                if (yaDev != null) {
                                    if (!yaDev.getState().equals(item.getState())) {
                                        YandexAliceJson eventJson = new YandexAliceJson(
                                                (double) System.currentTimeMillis() / 1000L, uuid);
                                        eventJson.setDeviceID(yaDev);
                                        for (YandexAliceProperties prop : yaDev.getProperties()) {
                                            eventJson.addPropertyState(prop, state);
                                        }
                                        for (YandexAliceCapabilities cap : yaDev.getCapabilities()) {
                                            eventJson.addCapabilityState(cap, state);
                                        }
                                        updateCallback(eventJson.returnRequest.toString());
                                        yaDev.setState(item.getState());
                                    }
                                }
                            } else if (state instanceof DecimalType) {
                                YandexDevice yaDev;
                                yaDev = yandexDevicesList.get(name);
                                if (yaDev != null) {
                                    YandexAliceJson eventJson = new YandexAliceJson(
                                            (double) System.currentTimeMillis() / 1000L, uuid);
                                    eventJson.setDeviceID(yaDev);
                                    for (YandexAliceProperties prop : yaDev.getProperties()) {
                                        eventJson.addPropertyState(prop, ((DecimalType) state).intValue());
                                    }
                                    updateCallback(eventJson.returnRequest.toString());
                                }
                            } else if (state instanceof OpenClosedType) {
                                YandexDevice yaDev;
                                yaDev = yandexDevicesList.get(name);
                                if (yaDev != null) {
                                    if (!yaDev.getState().equals(item.getState())) {
                                        YandexAliceJson eventJson = new YandexAliceJson(
                                                (double) System.currentTimeMillis() / 1000L, uuid);
                                        eventJson.setDeviceID(yaDev);
                                        for (YandexAliceProperties prop : yaDev.getProperties()) {
                                            eventJson.addPropertyState(prop, state);
                                        }
                                        updateCallback(eventJson.returnRequest.toString());
                                        yaDev.setState(item.getState());
                                    }
                                }
                            }
                        }
                        if (!item.getGroupNames().isEmpty()) {
                            boolean changed = false;
                            YandexDevice yaDev;
                            for (String itm : item.getGroupNames()) {
                                GroupItem grpitem = (GroupItem) Objects.requireNonNull(itemRegistry).getItem(itm);
                                yaDev = yandexDevicesList.get(grpitem.getName());
                                if (yaDev != null) {
                                    YandexAliceJson eventJson = new YandexAliceJson(
                                            (double) System.currentTimeMillis() / 1000L, uuid);
                                    eventJson.setDeviceID(yaDev);
                                    if (!yaDev.getProperties().isEmpty()) {
                                        for (YandexAliceProperties prop : yaDev.getProperties()) {
                                            if (prop.getOhID().equals(name)) {
                                                State st = prop.getState();
                                                if (st != null) {
                                                    if (!st.equals(item.getState())) {
                                                        eventJson.addPropertyState(prop, item.getState());
                                                        prop.setState(item.getState());
                                                        changed = true;
                                                    } else {
                                                        changed = false;
                                                    }
                                                } else {
                                                    prop.setState(item.getState());
                                                    eventJson.addPropertyState(prop, item.getState());
                                                    changed = true;
                                                }
                                            }
                                        }
                                    }
                                    if (!yaDev.getCapabilities().isEmpty()) {
                                        for (YandexAliceCapabilities cap : yaDev.getCapabilities()) {
                                            if (cap.getOhID().equals(name)) {
                                                State st = cap.getState();
                                                if (st != null) {
                                                    if (!st.equals(item.getState())) {
                                                        eventJson.addCapabilityState(cap, item.getState());
                                                        cap.setState(item.getState());
                                                        changed = true;
                                                    } else {
                                                        changed = false;
                                                    }
                                                } else {
                                                    cap.setState(item.getState());
                                                    eventJson.addCapabilityState(cap, item.getState());
                                                    changed = true;
                                                }
                                            } else if (cap.getCapabilityName()
                                                    .equals(YandexDevice.CAP_COLOR_SETTINGS)) {
                                                if (!item.hasTag("noyandex") && !item.hasTag("noYandex")) {
                                                    if (state instanceof StringType) {
                                                        eventJson.addCapabilityState(cap, item.getState());
                                                        changed = true;
                                                    } else if (state instanceof DecimalType) {
                                                        State st = cap.getTemperatureK().getState();
                                                        if (st != null) {
                                                            if (!st.equals(item.getState())) {
                                                                eventJson.addCapabilityState(cap, item.getState());
                                                                cap.setState(item.getState());
                                                                changed = true;
                                                            } else {
                                                                changed = false;
                                                            }
                                                        } else {
                                                            cap.getTemperatureK().setState(item.getState());
                                                            eventJson.addCapabilityState(cap, item.getState());
                                                            changed = true;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (changed) {
                                        updateCallback(eventJson.returnRequest.toString());
                                    } // else {
                                      // //logger.debug("Item {} has no changes", grpitem);
                                      // }

                                }
                                // logger.debug("it is group member {}", yaDev);
                            }

                        }
                    }
                } catch (Exception ex) {
                    logger.debug("Event Error {}", ex.getLocalizedMessage());
                }
            } else {
                action = false;
            }
        } catch (Exception e) {
            logger.error("ERROR Event {}", e.getLocalizedMessage());
        }
    }

    private void updateCallback(String json) {
        YandexCallbackUpdate updateAliceItem = new YandexCallbackUpdate(json);
        Thread updateAliceItemTread = new Thread(updateAliceItem);
        updateAliceItemTread.start();
    }

    public static String getItemState(String json, @Nullable String header) {
        Logger logger = LoggerFactory.getLogger(YandexService.class);
        YandexAliceJson aliceJson = new YandexAliceJson(Objects.requireNonNull(header));
        try {
            JSONObject parseItem = new JSONObject(json);
            JSONArray dev = (JSONArray) parseItem.get("devices");
            String itemID = dev.getJSONObject(0).getString("id");
            YandexDevice yDev = yandexDevicesList.get(itemID);
            // Item item = itemRegistry.getItem(itemID.getString("id"));
            aliceJson.setDeviceID(yDev);
            if (yDev != null) {
                if (itemRegistry != null) {
                    Item item = Objects.requireNonNull(itemRegistry).getItem(yDev.getId());
                    Collection<ItemChannelLink> lnk = Objects.requireNonNull(link).getLinks(item.getName());
                    boolean realDevice = false;
                    var getStatusObj = new Object() {
                        ThingStatus status = ThingStatus.UNKNOWN;
                    };
                    if (!lnk.isEmpty()) {
                        realDevice = true;
                    }
                    lnk.forEach(itemChannelLink -> {
                        String iname = itemChannelLink.getItemName();
                        logger.debug("linked item {} to {} channel", iname, itemChannelLink.getLinkedUID().getId());
                        Channel cnl = Objects.requireNonNull(things).getChannel(itemChannelLink.getLinkedUID());
                        if (cnl != null) {
                            Thing tng = Objects.requireNonNull(things).get(cnl.getUID().getThingUID());
                            logger.debug("status thing {}", Objects.requireNonNull(tng).getStatus().name());
                            getStatusObj.status = tng.getStatus();
                        }
                    });
                    if (realDevice) {
                        if (!getStatusObj.status.equals(ThingStatus.ONLINE)) {
                            aliceJson.addError("DEVICE_UNREACHABLE", yDev.getId());
                        } else {
                            if (!(item instanceof GroupItem)) {
                                for (YandexAliceCapabilities cap : yDev.getCapabilities()) {
                                    aliceJson.addCapabilityState(cap, item.getState());
                                }
                                for (YandexAliceProperties prop : yDev.getProperties()) {
                                    aliceJson.addPropertyState(prop, item.getState());
                                }
                            } else {
                                GroupItem groupItem = (GroupItem) item;
                                Set<Item> grpMembers = groupItem.getAllMembers();
                                for (Item itemGrp : grpMembers) {
                                    for (YandexAliceCapabilities cap : yDev.getCapabilities()) {
                                        if (cap.getOhID().equals(itemGrp.getName())) {
                                            aliceJson.addCapabilityState(cap, itemGrp.getState());
                                        }
                                    }
                                    for (YandexAliceProperties prop : yDev.getProperties()) {
                                        if (prop.getOhID().equals(itemGrp.getName())) {
                                            aliceJson.addPropertyState(prop, itemGrp.getState());
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (!(item instanceof GroupItem)) {
                            for (YandexAliceCapabilities cap : yDev.getCapabilities()) {
                                aliceJson.addCapabilityState(cap, item.getState());
                            }
                            for (YandexAliceProperties prop : yDev.getProperties()) {
                                aliceJson.addPropertyState(prop, item.getState());
                            }
                        } else {
                            GroupItem groupItem = (GroupItem) item;
                            Set<Item> grpMembers = groupItem.getAllMembers();
                            for (Item itemGrp : grpMembers) {
                                for (YandexAliceCapabilities cap : yDev.getCapabilities()) {
                                    if (cap.getOhID().equals(itemGrp.getName())) {
                                        aliceJson.addCapabilityState(cap, itemGrp.getState());
                                    } else if (cap.getOhID().isEmpty()) {
                                        if (!itemGrp.hasTag("noyandex") && !itemGrp.hasTag("noYandex")) {
                                            if (itemGrp instanceof ColorItem) {
                                                aliceJson.addCapabilityState(cap, itemGrp.getState());
                                            } else if (itemGrp instanceof NumberItem) {
                                                aliceJson.addCapabilityState(cap, itemGrp.getState());
                                            } else if (itemGrp instanceof StringItem) {
                                                logger.warn("item {} updating", itemGrp.getName());
                                                aliceJson.addCapabilityState(cap, itemGrp.getState());
                                            }
                                        }
                                    }
                                }
                                for (YandexAliceProperties prop : yDev.getProperties()) {
                                    if (prop.getOhID().equals(itemGrp.getName())) {
                                        aliceJson.addPropertyState(prop, itemGrp.getState());
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                logger.debug("This device does not exist");
            }
        } catch (Exception e) {
            logger.debug("Error get item {} state", e.getLocalizedMessage());
        }
        return aliceJson.returnRequest.toString();
    }

    public static String getItemsList(@Nullable String header) {
        final Logger logger = LoggerFactory.getLogger(YandexService.class);
        if (itemRegistry != null) {
            Collection<Item> itemsList = Objects.requireNonNull(itemRegistry).getItems();
            YandexAliceJson json = new YandexAliceJson(Objects.requireNonNull(header));
            json.setUserDevices(uuid);
            for (Item item : itemsList) {
                if (item.hasTag("Yandex")) {
                    if (item instanceof ColorItem) {
                        YandexDevice yDev = new YandexDevice(item.getName(), Objects.requireNonNull(item.getLabel()),
                                DEV_LIGHT, item.getState());
                        yDev.addCapabilities(item.getName(), YandexDevice.CAP_COLOR_SETTINGS);
                        json.createDevice(yDev);
                        json.addCapabilities(yDev);
                        yandexDevicesList.put(item.getName(), yDev);
                    } else if (item instanceof DimmerItem) {
                        logger.debug("this is Dimmer Item");
                        YandexDevice yDev = new YandexDevice(item.getName(), Objects.requireNonNull(item.getLabel()),
                                DEV_LIGHT, item.getState());
                        yDev.addCapabilities(item.getName(), YandexDevice.CAP_RANGE);
                        json.createDevice(yDev);
                        json.addCapabilities(yDev);
                        yandexDevicesList.put(item.getName(), yDev);
                    } else if (item instanceof SwitchItem) {
                        if (item.hasTag("Lightbulb")) {
                            YandexDevice yDev = new YandexDevice(item.getName(),
                                    Objects.requireNonNull(item.getLabel()), DEV_LIGHT, item.getState());
                            yDev.addCapabilities(item.getName(), YandexDevice.CAP_ON_OFF);
                            json.createDevice(yDev);
                            json.addCapabilities(yDev);
                            yandexDevicesList.put(item.getName(), yDev);
                        } else if (item.hasTag("PowerOutlet")) {
                            YandexDevice yDev = new YandexDevice(item.getName(),
                                    Objects.requireNonNull(item.getLabel()), DEV_SOCKET, item.getState());
                            yDev.addCapabilities(item.getName(), YandexDevice.CAP_ON_OFF);
                            json.createDevice(yDev);
                            json.addCapabilities(yDev);
                            yandexDevicesList.put(item.getName(), yDev);
                        } else {
                            YandexDevice yDev = new YandexDevice(item.getName(),
                                    Objects.requireNonNull(item.getLabel()), DEV_SWITCH, item.getState());
                            yDev.addCapabilities(item.getName(), YandexDevice.CAP_ON_OFF);
                            json.createDevice(yDev);
                            json.addCapabilities(yDev);
                            yandexDevicesList.put(item.getName(), yDev);
                        }
                    } else if (item instanceof NumberItem) {
                        // todo number item list
                        YandexDevice yDev;
                        if (item.hasTag("smart_meter")) {
                            yDev = new YandexDevice(item.getName(), Objects.requireNonNull(item.getLabel()),
                                    DEV_SMART_METER, item.getState());
                        } else {
                            yDev = new YandexDevice(item.getName(), Objects.requireNonNull(item.getLabel()), DEV_SENSOR,
                                    item.getState());
                        }
                        var ref = new Object() {
                            String instance = "";
                            String unit = "";
                        };
                        Set<String> tags = item.getTags();
                        json.createDevice(yDev);
                        for (String tag : tags) {
                            YandexDevice.FLOAT_LIST.forEach((v) -> {
                                if (v.contains(tag.toLowerCase())) {
                                    ref.instance = v;
                                    switch (v) {
                                        case YandexDevice.FLOAT_AMPERAGE:
                                            ref.unit = YandexDevice.UNIT_AMPERE;
                                            break;
                                        case YandexDevice.FLOAT_BATTERY_LEVEL:
                                        case YandexDevice.FLOAT_WATER_LEVEL:
                                        case YandexDevice.FLOAT_HUMIDITY:
                                        case YandexDevice.FLOAT_FOOD_LEVEL:
                                            ref.unit = YandexDevice.UNIT_PERCENT;
                                            break;
                                        case YandexDevice.FLOAT_CO2:
                                            ref.unit = YandexDevice.UNIT_PPM;
                                            break;
                                        case YandexDevice.FLOAT_ILLUMINATION:
                                            ref.unit = YandexDevice.UNIT_LUX;
                                            break;
                                        case YandexDevice.FLOAT_PM1_DENSITY:
                                        case YandexDevice.FLOAT_TVOC:
                                        case YandexDevice.FLOAT_PM10_DENSITY:
                                        case YandexDevice.FLOAT_PM25_DENSITY:
                                            ref.unit = YandexDevice.UNIT_MCG_M3;
                                            break;
                                        case YandexDevice.FLOAT_POWER:
                                            ref.unit = YandexDevice.UNIT_WATT;
                                            break;
                                        case YandexDevice.FLOAT_PRESSURE:
                                            ref.unit = YandexDevice.UNIT_BAR;
                                            break;
                                        case YandexDevice.FLOAT_TEMP:
                                            ref.unit = YandexDevice.UNIT_TEMP_CELSIUS;
                                            break;
                                        case YandexDevice.FLOAT_VOLTAGE:
                                            ref.unit = YandexDevice.UNIT_VOLT;
                                            break;
                                        case YandexDevice.FLOAT_ELECTRICITY_METER:
                                            ref.unit = YandexDevice.UNIT_KILOWATT_HOUR;
                                            break;
                                        case YandexDevice.FLOAT_GAS_METER, YandexDevice.FLOAT_WATER_METER:
                                            ref.unit = YandexDevice.UNIT_CUBIC_METER;
                                            break;
                                        case YandexDevice.FLOAT_HEAT_METER:
                                            ref.unit = YandexDevice.UNIT_GIGACALORIE;
                                            break;

                                    }
                                }
                            });
                        }
                        if (item.hasTag("Temperature")) {
                            if (item.hasTag("kelvin")) {
                                yDev.addProperties(item.getName(), YandexDevice.PROP_FLOAT, YandexDevice.FLOAT_TEMP,
                                        YandexDevice.UNIT_TEMP_KELVIN);
                            } else {
                                yDev.addProperties(item.getName(), YandexDevice.PROP_FLOAT, YandexDevice.FLOAT_TEMP,
                                        YandexDevice.UNIT_TEMP_CELSIUS);
                            }
                        } else {
                            yDev.addProperties(item.getName(), YandexDevice.PROP_FLOAT, ref.instance, ref.unit);
                        }
                        json.addProperties(yDev);
                        yandexDevicesList.put(item.getName(), yDev);
                    } else if (item instanceof ContactItem) {
                        if ((item.hasTag("Door")) || (item.hasTag("GarageDoor")) || (item.hasTag("FrontDoor"))
                                || (item.hasTag("CellarDoor")) || (item.hasTag("SideDoor"))
                                || (item.hasTag("BackDoor"))) {
                            YandexDevice yDev = new YandexDevice(item.getName(),
                                    Objects.requireNonNull(item.getLabel()), DEV_SENSOR_OPEN, item.getState());
                            json.createDevice(yDev);
                            yDev.addProperties(YandexDevice.PROP_EVENT, YandexDevice.EVENT_OPEN);
                            json.addProperties(yDev);
                            yandexDevicesList.put(item.getName(), yDev);
                        } else if ((item.hasTag("Blinds")) || (item.hasTag("Window"))) {
                            YandexDevice yDev = new YandexDevice(item.getName(),
                                    Objects.requireNonNull(item.getLabel()), DEV_OPENABLE, item.getState());
                            json.createDevice(yDev);
                            yDev.addProperties(YandexDevice.PROP_EVENT, YandexDevice.EVENT_OPEN);
                            json.addProperties(yDev);
                            yandexDevicesList.put(item.getName(), yDev);
                        }
                    } else if (item instanceof RollershutterItem) {
                        logger.debug("RollerShutter");
                        YandexDevice yDev = new YandexDevice(item.getName(), Objects.requireNonNull(item.getLabel()),
                                DEV_CURTAIN, item.getState());
                        json.createDevice(yDev);
                        yDev.addProperties(YandexDevice.PROP_EVENT, YandexDevice.EVENT_OPEN);
                        yDev.addCapabilities(item.getName(), YandexDevice.CAP_RANGE, YandexDevice.EVENT_OPEN,
                                YandexDevice.UNIT_PERCENT, 0, 100, 1.0);
                        // yDev.addCapabilities(YandexDevice.CAP_ON_OFF);
                        json.addProperties(yDev);
                        json.addCapabilities(yDev);
                        yandexDevicesList.put(item.getName(), yDev);
                    } else if (item instanceof GroupItem) {
                        // logger.debug("It`s a GROUP!");
                        GroupItem groupItem = (GroupItem) item;
                        Set<Item> grpMembers = groupItem.getAllMembers();
                        var dev = new Object() {
                            String devType = "";
                        };
                        // if (devicesList.isEmpty()) {
                        // logger.warn("Cannot get list of devices");
                        // getDevicesList();
                        // }
                        DEV_LIST.forEach((v) -> {
                            for (String tag : groupItem.getTags()) {
                                if (v.endsWith(tag.toLowerCase())) {
                                    dev.devType = v;
                                    break;
                                }
                            }
                        });
                        YandexDevice yDev = new YandexDevice(item.getName(), Objects.requireNonNull(item.getLabel()),
                                dev.devType, item.getState());
                        json.createDevice(yDev);
                        for (Item grpItem : grpMembers) {
                            if (!grpItem.hasTag("noyandex")) {
                                if (grpItem.getType().equals("Switch")) {
                                    if (grpItem.hasTag("toggle")) {
                                        // TODO toggle
                                        logger.debug("this is GROUP Switch TOGGLE mode");
                                        Set<String> tags = grpItem.getTags();
                                        var ref = new Object() {
                                            String instance = "";
                                        };
                                        for (String tag : tags) {
                                            YandexDevice.TOGGLE_LIST.forEach((v) -> {
                                                if (v.contains(tag.toLowerCase())) {
                                                    ref.instance = v;
                                                }
                                            });
                                        }
                                        yDev.addCapabilities(grpItem.getName(), YandexDevice.CAP_TOGGLE, ref.instance,
                                                "", 0, 0, 0.0);
                                    } else if (grpItem.hasTag(YandexDevice.EVENT_MOTION)) {
                                        yDev.addProperties(grpItem.getName(), YandexDevice.PROP_EVENT,
                                                YandexDevice.EVENT_MOTION, "");
                                    } else if (grpItem.hasTag(YandexDevice.EVENT_WATER_LEAK)) {
                                        yDev.addProperties(grpItem.getName(), YandexDevice.PROP_EVENT,
                                                YandexDevice.EVENT_WATER_LEAK, "");
                                    } else if (grpItem.hasTag(YandexDevice.EVENT_SMOKE)) {
                                        yDev.addProperties(grpItem.getName(), YandexDevice.PROP_EVENT,
                                                YandexDevice.EVENT_SMOKE, "");
                                    } else {
                                        logger.debug("this is GROUP Switch ON_OFF mode");
                                        yDev.addCapabilities(grpItem.getName(), YandexDevice.CAP_ON_OFF);
                                    }
                                } else if (grpItem instanceof ColorItem) {
                                    Set<String> tags = grpItem.getTags();
                                    if (tags.stream().anyMatch(tag -> tag.equals("color_model"))) {
                                        YandexAliceCapabilities.ColorSettingsModel colorSettingsModel = new YandexAliceCapabilities.ColorSettingsModel();
                                        colorSettingsModel.setOhID(grpItem.getName());
                                        colorSettingsModel.setModel(true);
                                        List<YandexAliceCapabilities> caps = yDev.getCapabilities();
                                        if (caps.isEmpty()) {
                                            yDev.addCapabilities("", YandexDevice.CAP_COLOR_SETTINGS,
                                                    colorSettingsModel);
                                        } else {
                                            if (caps.stream().anyMatch(
                                                    cs -> cs.capabilityName.equals(YandexDevice.CAP_COLOR_SETTINGS))) {
                                                caps.stream()
                                                        .filter(cs -> cs.capabilityName
                                                                .equals(YandexDevice.CAP_COLOR_SETTINGS))
                                                        .findFirst().get().setColorModel(colorSettingsModel);
                                            } else {
                                                yDev.addCapabilities("", YandexDevice.CAP_COLOR_SETTINGS);
                                                caps = yDev.getCapabilities();
                                                caps.stream()
                                                        .filter(cs -> cs.capabilityName
                                                                .equals(YandexDevice.CAP_COLOR_SETTINGS))
                                                        .findFirst().get().setColorModel(colorSettingsModel);
                                            }
                                        }
                                    }
                                    // json.addCapabilities(yDev);
                                } else if ((grpItem instanceof NumberItem) || (grpItem instanceof DimmerItem)) {
                                    logger.debug("this is GROUP Number ");
                                    Set<String> tags = grpItem.getTags();
                                    String capName = "";
                                    var ref = new Object() {
                                        String instance = "";
                                        String unit = "";
                                    };
                                    int minRange = 0, maxRange = 100;
                                    double precision = 1.0;
                                    StateDescription sd = grpItem.getStateDescription();
                                    if (sd != null) {
                                        if (sd.getMinimum() != null) {
                                            BigDecimal mr = sd.getMinimum();
                                            if (mr != null) {
                                                minRange = mr.intValue();
                                            }
                                        }
                                        if (sd.getMaximum() != null) {
                                            BigDecimal mr = sd.getMaximum();
                                            if (mr != null) {
                                                maxRange = mr.intValue();
                                            }
                                        }

                                        if (sd.getStep() != null) {
                                            BigDecimal st = sd.getStep();
                                            if (st != null) {
                                                precision = st.doubleValue();
                                            }
                                        }
                                    }
                                    // int minRange = 0, maxRange = 0, precision = 0;
                                    for (String tag : tags) {
                                        if (YandexDevice.CAP_RANGE.contains(tag)) {
                                            capName = YandexDevice.CAP_RANGE;
                                        } else if (tag.contains("min=")) {
                                            minRange = Integer.parseInt(tag.split("=")[1]);
                                        } else if (tag.contains("max=")) {
                                            maxRange = Integer.parseInt(tag.split("=")[1]);
                                        } else if (tag.contains("step=")) {
                                            precision = Double.parseDouble(tag.split("=")[1]);
                                        }
                                        YandexDevice.RANGE_LIST.forEach((v) -> {
                                            if (v.contains(tag.toLowerCase())) {
                                                ref.instance = v;
                                                switch (v) {
                                                    case YandexDevice.RANGE_TEMPERATURE:
                                                        ref.unit = YandexDevice.UNIT_TEMP_CELSIUS;
                                                        break;
                                                    case YandexDevice.RANGE_BRIGHTNESS:
                                                    case YandexDevice.RANGE_HUMIDITY:
                                                    case YandexDevice.RANGE_OPEN:
                                                        ref.unit = YandexDevice.UNIT_PERCENT;
                                                        break;
                                                    case YandexDevice.RANGE_CHANNEL:
                                                        ref.unit = "";
                                                        break;
                                                    case YandexDevice.RANGE_VOLUME:
                                                        if ("percent".equals(tag)) {
                                                            ref.unit = YandexDevice.UNIT_PERCENT;
                                                        } else
                                                            ref.unit = "";
                                                        break;
                                                }
                                            }
                                        });
                                        YandexDevice.FLOAT_LIST.forEach((v) -> {
                                            if (v.contains(tag.toLowerCase())) {
                                                ref.instance = v;
                                                switch (v) {
                                                    case YandexDevice.FLOAT_AMPERAGE:
                                                        ref.unit = YandexDevice.UNIT_AMPERE;
                                                        break;
                                                    case YandexDevice.FLOAT_BATTERY_LEVEL:
                                                    case YandexDevice.FLOAT_WATER_LEVEL:
                                                    case YandexDevice.FLOAT_HUMIDITY:
                                                    case YandexDevice.FLOAT_FOOD_LEVEL:
                                                        ref.unit = YandexDevice.UNIT_PERCENT;
                                                        break;
                                                    case YandexDevice.FLOAT_CO2:
                                                        ref.unit = YandexDevice.UNIT_PPM;
                                                        break;
                                                    case YandexDevice.FLOAT_ILLUMINATION:
                                                        ref.unit = YandexDevice.UNIT_LUX;
                                                        break;
                                                    case YandexDevice.FLOAT_PM1_DENSITY:
                                                    case YandexDevice.FLOAT_TVOC:
                                                    case YandexDevice.FLOAT_PM10_DENSITY:
                                                    case YandexDevice.FLOAT_PM25_DENSITY:
                                                        ref.unit = YandexDevice.UNIT_MCG_M3;
                                                        break;
                                                    case YandexDevice.FLOAT_POWER:
                                                        ref.unit = YandexDevice.UNIT_WATT;
                                                        break;
                                                    case YandexDevice.FLOAT_PRESSURE:
                                                        ref.unit = YandexDevice.UNIT_BAR;
                                                        break;
                                                    case YandexDevice.FLOAT_TEMP:
                                                        ref.unit = YandexDevice.UNIT_TEMP_CELSIUS;
                                                        break;
                                                    case YandexDevice.FLOAT_VOLTAGE:
                                                        ref.unit = YandexDevice.UNIT_VOLT;
                                                        break;
                                                    case YandexDevice.FLOAT_ELECTRICITY_METER:
                                                        ref.unit = YandexDevice.UNIT_KILOWATT_HOUR;
                                                        break;
                                                    case YandexDevice.FLOAT_GAS_METER, YandexDevice.FLOAT_WATER_METER:
                                                        ref.unit = YandexDevice.UNIT_CUBIC_METER;
                                                        break;
                                                    case YandexDevice.FLOAT_HEAT_METER:
                                                        ref.unit = YandexDevice.UNIT_GIGACALORIE;
                                                        break;

                                                }
                                            }
                                        });
                                        if ("temperature_k".equals(tag)) {
                                            List<YandexAliceCapabilities> caps = yDev.getCapabilities();
                                            if (caps.isEmpty() || caps.stream().noneMatch(
                                                    c -> c.capabilityName.equals(YandexDevice.CAP_COLOR_SETTINGS))) {
                                                yDev.addCapabilities("", YandexDevice.CAP_COLOR_SETTINGS);
                                            }
                                            caps.forEach(colset -> {
                                                String capname = colset.getCapabilityName();
                                                if (YandexDevice.CAP_COLOR_SETTINGS.equals(capname)) {
                                                    YandexAliceCapabilities.ColorSettingsTemperature colorSettingsTemperature = new YandexAliceCapabilities.ColorSettingsTemperature();
                                                    colorSettingsTemperature.setOhID(grpItem.getName());
                                                    colorSettingsTemperature.setTemp(true);
                                                    colset.setTemperatureK(colorSettingsTemperature);
                                                }
                                            });
                                        }
                                    }
                                    if (capName.isEmpty()) {
                                        List<YandexAliceCapabilities> caps = yDev.getCapabilities();
                                        if (caps.stream().noneMatch(temper -> temper.getTemperatureK().isTemp())) {
                                            yDev.addProperties(grpItem.getName(), YandexDevice.PROP_FLOAT, ref.instance,
                                                    ref.unit);
                                        }
                                    } else {
                                        yDev.addCapabilities(grpItem.getName(), capName, ref.instance, ref.unit,
                                                minRange, maxRange, precision);
                                    }
                                } else if (grpItem instanceof StringItem) {
                                    String capName = "";
                                    String instance = "";
                                    logger.debug("This is string");
                                    Set<String> tags = grpItem.getTags();
                                    Collection<String> modesCol = null;
                                    ArrayList<String> toRemove = new ArrayList<>();
                                    ArrayList<String> toAdd = new ArrayList<>();
                                    List<StateOption> opt = null;
                                    StateDescription options = grpItem.getStateDescription();
                                    boolean setDefaultValues = true;
                                    if (options != null) {
                                        opt = options.getOptions();
                                        if (!opt.isEmpty()) {
                                            if (!opt.get(0).getValue().isEmpty())
                                                setDefaultValues = false;
                                        }
                                    }
                                    for (String tag : tags) {
                                        if ("scenes".equalsIgnoreCase(tag)) {
                                            ArrayList<String> scenes = new ArrayList<>();
                                            if (options != null) {
                                                opt = options.getOptions();
                                                if (!opt.isEmpty()) {
                                                    if (!opt.get(0).getValue().isEmpty()) {
                                                        for (StateOption stateOption : opt) {
                                                            if (YandexDevice.SCENES_LIST
                                                                    .contains(stateOption.getValue())) {
                                                                scenes.add(stateOption.getValue());
                                                            } else
                                                                logger.debug("I don't know scene {}",
                                                                        stateOption.getValue());
                                                        }
                                                        // yDev.setSceneColorCapabilities(scenes, grpItem.getName());
                                                    }
                                                }
                                            }
                                            for (String scnTags : tags) {
                                                if (YandexDevice.SCENES_LIST.contains(scnTags.toLowerCase())) {
                                                    YandexDevice.SCENES_LIST.forEach((list) -> {
                                                        if (list.equals(scnTags)) {
                                                            scenes.add(scnTags);
                                                        }
                                                    });

                                                }
                                            }
                                            if (scenes.isEmpty()) {
                                                yDev.setSceneColorCapabilities(YandexDevice.SCENES_LIST,
                                                        grpItem.getName());
                                            } else {
                                                yDev.setSceneColorCapabilities(scenes, grpItem.getName());
                                            }
                                        }
                                        if (YandexDevice.CAP_MODE.contains(tag.toLowerCase())) {
                                            capName = YandexDevice.CAP_MODE;
                                        }
                                        if (tag.equalsIgnoreCase(YandexDevice.MODE_CLEANUP)) {
                                            instance = YandexDevice.MODE_CLEANUP;
                                            if (setDefaultValues) {
                                                modesCol = new ArrayList<>(YandexDevice.DEFAULT_CLEANUP);
                                            }
                                        } else if (tag.equalsIgnoreCase(YandexDevice.MODE_COFFEE)) {
                                            instance = YandexDevice.MODE_COFFEE;
                                            if (setDefaultValues) {
                                                modesCol = new ArrayList<>(YandexDevice.DEFAULT_COFFEE);
                                            }
                                        } else if (tag.equalsIgnoreCase(YandexDevice.MODE_DISHWASHING)) {
                                            instance = YandexDevice.MODE_DISHWASHING;
                                            if (setDefaultValues) {
                                                modesCol = new ArrayList<>(YandexDevice.DEFAULT_DISHWASHING);
                                            }
                                        } else if (tag.equalsIgnoreCase(YandexDevice.MODE_FAN_SPEED)) {
                                            instance = YandexDevice.MODE_FAN_SPEED;
                                            if (setDefaultValues) {
                                                modesCol = new ArrayList<>(YandexDevice.DEFAULT_FAN_SPEED);
                                            }
                                        } else if (tag.equalsIgnoreCase(YandexDevice.MODE_HEAT)) {
                                            instance = YandexDevice.MODE_HEAT;
                                            if (setDefaultValues) {
                                                modesCol = new ArrayList<>(YandexDevice.DEFAULT_HEAT);
                                            }
                                        } else if (tag.equalsIgnoreCase(YandexDevice.MODE_INPUT_SOURCE)) {
                                            instance = YandexDevice.MODE_INPUT_SOURCE;
                                            if (setDefaultValues) {
                                                modesCol = new ArrayList<>(YandexDevice.DEFAULT_INPUT_SOURCE);
                                            }
                                        } else if (tag.equalsIgnoreCase(YandexDevice.MODE_PROGRAM)) {
                                            instance = YandexDevice.MODE_PROGRAM;
                                            if (setDefaultValues) {
                                                modesCol = new ArrayList<>(YandexDevice.DEFAULT_PROGRAM);
                                            }
                                        } else if (tag.equalsIgnoreCase(YandexDevice.MODE_SWING)) {
                                            instance = YandexDevice.MODE_SWING;
                                            if (setDefaultValues) {
                                                modesCol = new ArrayList<>(YandexDevice.DEFAULT_SWING);
                                            }
                                        } else if (tag.equalsIgnoreCase(YandexDevice.MODE_THERMOSTAT)) {
                                            instance = YandexDevice.MODE_THERMOSTAT;
                                            if (setDefaultValues) {
                                                modesCol = new ArrayList<>(YandexDevice.DEFAULT_THERMOSTAT);
                                            }
                                        } else if (tag.equalsIgnoreCase(YandexDevice.MODE_TEA)) {
                                            instance = YandexDevice.MODE_TEA;
                                            if (setDefaultValues) {
                                                modesCol = new ArrayList<>(YandexDevice.DEFAULT_TEA);
                                            }
                                        } else if (tag.equalsIgnoreCase(YandexDevice.MODE_WORK_SPEED)) {
                                            instance = YandexDevice.MODE_WORK_SPEED;
                                            if (setDefaultValues) {
                                                modesCol = new ArrayList<>(YandexDevice.DEFAULT_WORK_SPEED);
                                            }
                                        } else if (tag.equalsIgnoreCase(YandexDevice.EVENT_MOTION)) {
                                            yDev.addProperties(grpItem.getName(), YandexDevice.PROP_EVENT,
                                                    YandexDevice.EVENT_MOTION, "");
                                            ((StringItem) grpItem).setStateDescriptionService((text,
                                                    locale) -> StateDescriptionFragmentBuilder.create().withOptions(
                                                            List.of(new StateOption("detected", "detected"),
                                                                    new StateOption("not_detected", "not detected")))
                                                            .build().toStateDescription());
                                        } else if (tag.equalsIgnoreCase(YandexDevice.EVENT_VIBRATION)) {
                                            yDev.addProperties(grpItem.getName(), YandexDevice.PROP_EVENT,
                                                    YandexDevice.EVENT_VIBRATION, "");
                                            ((StringItem) grpItem).setStateDescriptionService(
                                                    (text, locale) -> StateDescriptionFragmentBuilder.create()
                                                            .withOptions(List.of(new StateOption("tilt", "tilt"),
                                                                    new StateOption("fall", "fall"),
                                                                    new StateOption("vibration", "vibration")))
                                                            .build().toStateDescription());
                                        } else if (tag.equalsIgnoreCase(YandexDevice.EVENT_BUTTON)) {
                                            yDev.addProperties(grpItem.getName(), YandexDevice.PROP_EVENT,
                                                    YandexDevice.EVENT_BUTTON, "");
                                            ((StringItem) grpItem).setStateDescriptionService(
                                                    (text, locale) -> StateDescriptionFragmentBuilder.create()
                                                            .withOptions(List.of(new StateOption("click", "click"),
                                                                    new StateOption("double_click", "double_click"),
                                                                    new StateOption("long_press", "long_press")))
                                                            .build().toStateDescription());
                                        }
                                        if (setDefaultValues) {
                                            if (tag.toLowerCase().startsWith("-")) {
                                                YandexDevice.OPER_LIST.forEach((ops) -> {
                                                    if (ops.equals(tag.toLowerCase().substring(1))) {
                                                        toRemove.add(tag.toLowerCase().substring(1));
                                                    }
                                                });
                                            } else if (tag.toLowerCase().startsWith("+")) {
                                                YandexDevice.OPER_LIST.forEach((ops) -> {
                                                    if (ops.equals(tag.toLowerCase().substring(1))) {
                                                        toAdd.add(tag.toLowerCase().substring(1));
                                                    }
                                                });
                                            }
                                        }
                                    }
                                    if (!setDefaultValues) {
                                        modesCol = new ArrayList<>();
                                        if (opt != null) {
                                            for (StateOption stateOption : opt) {
                                                if (YandexDevice.OPER_LIST.contains(stateOption.getValue())) {
                                                    modesCol.add(stateOption.getValue());
                                                } else
                                                    logger.debug("I don't know operation {}", stateOption.getValue());
                                            }
                                        } else
                                            logger.debug("options is null");
                                    }
                                    if (modesCol != null) {
                                        modesCol.addAll(toAdd);
                                        modesCol.removeAll(toRemove);
                                        logger.warn("Group init complete. modesCol: {}, toRemove {}, toAdd {}",
                                                modesCol, toRemove, toAdd);
                                        if (!modesCol.isEmpty()) {
                                            yDev.addCapabilities(grpItem.getName(), capName, instance, modesCol);
                                        }
                                    } else {
                                        logger.debug("modesCol is null!");
                                    }
                                } else if (grpItem instanceof ContactItem) {
                                    if ((grpItem.hasTag("Door")) || (grpItem.hasTag("GarageDoor"))
                                            || (grpItem.hasTag("FrontDoor")) || (grpItem.hasTag("CellarDoor"))
                                            || (grpItem.hasTag("SideDoor")) || (grpItem.hasTag("BackDoor"))) {
                                        yDev.addProperties(grpItem.getName(), YandexDevice.PROP_EVENT,
                                                YandexDevice.EVENT_OPEN, "");
                                    } else if ((grpItem.hasTag("Blinds")) || (grpItem.hasTag("Window"))) {
                                        yDev.addProperties(grpItem.getName(), YandexDevice.PROP_EVENT,
                                                YandexDevice.EVENT_OPEN, "");
                                    }
                                }
                            }
                        }
                        json.addCapabilities(yDev);
                        json.addProperties(yDev);
                        yandexDevicesList.put(item.getName(), yDev);
                    }
                }
            }
            logger.debug("Items list response: {}", json.returnRequest);
            return json.returnRequest.toString();
        }
        return "";
    }

    public static String setItemState(String json, @Nullable String header) {
        final Logger logger = LoggerFactory.getLogger(YandexService.class);
        action = true;
        JSONObject answer = new JSONObject();
        JSONObject payload = new JSONObject();
        JSONArray devices = new JSONArray();
        answer.put("payload", payload);
        answer.put("request_id", header);
        // Collection<ItemChannelLink> lnk = link.getAll();
        // lnk.forEach(itemChannelLink -> {
        // String iname = itemChannelLink.getItemName();
        // logger.debug("linked item {} to {} channel", iname, itemChannelLink.getLinkedUID().getId());
        // Thing cnl = things.get(things.getChannel(itemChannelLink.getLinkedUID()).getUID().getThingUID());
        // logger.debug("status thing {}", cnl.getStatus().name());
        // });
        try {
            JSONArray parseItem = new JSONObject(json).getJSONObject("payload").getJSONArray("devices");
            for (int i = 0; i < parseItem.length(); i++) {
                JSONObject dev = parseItem.getJSONObject(i);
                String id = dev.getString("id");
                JSONArray capabilities = dev.getJSONArray("capabilities");
                JSONObject state = capabilities.getJSONObject(0).getJSONObject("state");
                String type = capabilities.getJSONObject(0).getString("type");
                boolean realDevice = false;
                var getStatusObj = new Object() {
                    ThingStatus status = ThingStatus.UNKNOWN;
                };
                if ((itemRegistry != null) && (eventPublisher != null)) {
                    logger.debug("setItemState {}", id);
                    try {
                        Item item = Objects.requireNonNull(itemRegistry).getItem(id);
                        Collection<ItemChannelLink> lnk = Objects.requireNonNull(link).getLinks(item.getName());
                        if (!lnk.isEmpty()) {
                            realDevice = true;
                        }
                        lnk.forEach(itemChannelLink -> {
                            String iname = itemChannelLink.getItemName();
                            logger.debug("linked item {} to {} channel", iname, itemChannelLink.getLinkedUID().getId());
                            Channel cnl = Objects.requireNonNull(things).getChannel(itemChannelLink.getLinkedUID());
                            if (cnl != null) {
                                Thing tng = Objects.requireNonNull(things).get(cnl.getUID().getThingUID());
                                logger.debug("status thing {}", Objects.requireNonNull(tng).getStatus().name());
                                getStatusObj.status = tng.getStatus();
                            }
                        });
                        String status;
                        if (realDevice) {
                            if (!getStatusObj.status.equals(ThingStatus.ONLINE)) {
                                status = "ERROR";
                            } else {
                                status = publishState(id, item, state, type);
                            }
                        } else {
                            status = publishState(id, item, state, type);
                        }
                        JSONObject itemJson = new JSONObject();
                        itemJson.put("id", item.getName());
                        if ("DONE".equals(status)) {
                            itemJson.put("action_result", new JSONObject().put("status", "DONE"));
                        } else if ("ERROR".equals(status)) {
                            itemJson.put("action_result",
                                    new JSONObject().put("status", "ERROR").put("error_code", "DEVICE_UNREACHABLE"));
                        }
                        devices.put(itemJson);
                    } catch (Exception e) {
                        JSONObject itemJson = new JSONObject();
                        itemJson.put("id", id);
                        itemJson.put("action_result",
                                new JSONObject().put("status", "ERROR").put("error_code", "DEVICE_UNREACHABLE"));
                        devices.put(itemJson);
                        logger.debug("Error {}", e.getLocalizedMessage());
                    }
                }
            }
            payload.put("devices", devices);
        } catch (Exception e) {
            logger.error("Error get item {} state", e.getLocalizedMessage());
        }
        logger.debug("State JSON is: {}", answer);
        return answer.toString();
    }

    private static String publishState(String id, Item item, JSONObject state, String type) {
        try {
            if (item instanceof ColorItem) {
                JSONObject value = state.getJSONObject("value");
                Objects.requireNonNull(eventPublisher).post(ItemEventFactory.createCommandEvent(id,
                        HSBType.valueOf(value.get("h") + "," + value.get("s") + "," + value.get("v"))));
            } else if (item instanceof DimmerItem) {
                int value = state.getInt("value");
                Objects.requireNonNull(eventPublisher)
                        .post(ItemEventFactory.createCommandEvent(id, PercentType.valueOf(String.valueOf(value))));
            } else if (item instanceof SwitchItem) {
                boolean value = state.getBoolean("value");
                if (value) {
                    Objects.requireNonNull(eventPublisher).post(ItemEventFactory.createCommandEvent(id, OnOffType.ON));
                } else {
                    Objects.requireNonNull(eventPublisher).post(ItemEventFactory.createCommandEvent(id, OnOffType.OFF));
                }
            } else if (item instanceof GroupItem) {
                GroupItem groupItem = (GroupItem) item;
                Set<Item> grpMembers = groupItem.getAllMembers();
                YandexDevice yaDev = yandexDevicesList.get(item.getName());
                if (yaDev != null) {
                    List<YandexAliceCapabilities> caps = yaDev.getCapabilities();
                    for (YandexAliceCapabilities cp : caps) {
                        if (cp.getCapabilityName().equals(type)) {
                            grpMembers.forEach((memItem) -> {
                                if (cp.getOhID().equals(memItem.getName())) {
                                    if (memItem instanceof ColorItem) {
                                        String instance = state.getString("instance");
                                        if ("scene".equals(instance)) {
                                            Objects.requireNonNull(eventPublisher)
                                                    .post(ItemEventFactory.createCommandEvent(cp.getScenesOhID(),
                                                            StringType.valueOf(state.getString("value"))));
                                        } else if ("temperature_k".equals(instance)) {
                                            Objects.requireNonNull(eventPublisher)
                                                    .post(ItemEventFactory.createCommandEvent(
                                                            cp.getTemperatureK().getOhID(), DecimalType
                                                                    .valueOf(String.valueOf(state.getInt("value")))));
                                        } else {
                                            JSONObject value = state.getJSONObject("value");
                                            Objects.requireNonNull(eventPublisher)
                                                    .post(ItemEventFactory.createCommandEvent(cp.getOhID(),
                                                            HSBType.valueOf(value.get("h") + "," + value.get("s") + ","
                                                                    + value.get("v"))));
                                        }
                                    } else if (memItem instanceof DimmerItem) {
                                        int value = 0;
                                        boolean relative = false;
                                        if (!state.isNull("relative")) {
                                            relative = state.getBoolean("relative");
                                        }
                                        var itemState = memItem.getState().toString();
                                        String instance = state.getString("instance");
                                        if (relative) {
                                            int setValue = state.getInt("value");
                                            value = Integer.parseInt(itemState) + setValue;
                                            if (value <= 0) {
                                                value = 0;
                                            }
                                        } else {
                                            value = state.getInt("value");
                                        }
                                        if (instance.equals(cp.getInstance())) {
                                            Objects.requireNonNull(eventPublisher)
                                                    .post(ItemEventFactory.createCommandEvent(cp.getOhID(),
                                                            PercentType.valueOf(String.valueOf(value))));
                                        }
                                    } else if (memItem instanceof SwitchItem) {
                                        boolean value = state.getBoolean("value");
                                        if (value) {
                                            Objects.requireNonNull(eventPublisher).post(
                                                    ItemEventFactory.createCommandEvent(cp.getOhID(), OnOffType.ON));
                                        } else {
                                            Objects.requireNonNull(eventPublisher).post(
                                                    ItemEventFactory.createCommandEvent(cp.getOhID(), OnOffType.OFF));
                                        }
                                    } else if (memItem instanceof NumberItem) {
                                        double value = state.getDouble("value");
                                        String instance = state.getString("instance");
                                        if (instance.equals(cp.getInstance())) {
                                            Objects.requireNonNull(eventPublisher)
                                                    .post(ItemEventFactory.createCommandEvent(cp.getOhID(),
                                                            DecimalType.valueOf(String.valueOf(value))));
                                        }
                                    } else if (memItem instanceof StringItem) {
                                        Objects.requireNonNull(eventPublisher).post(ItemEventFactory.createCommandEvent(
                                                cp.getOhID(), StringType.valueOf(state.getString("value"))));
                                    }
                                } else if (cp.getOhID().isEmpty()) {
                                    if (!memItem.hasTag("noyandex") && !memItem.hasTag("noYandex")) {
                                        if (memItem instanceof ColorItem) {
                                            String instance = state.getString("instance");
                                            if ("hsv".equals(instance)) {
                                                JSONObject value = state.getJSONObject("value");
                                                Objects.requireNonNull(eventPublisher)
                                                        .post(ItemEventFactory.createCommandEvent(
                                                                cp.getColorModel().getOhID(),
                                                                HSBType.valueOf(value.get("h") + "," + value.get("s")
                                                                        + "," + value.get("v"))));
                                            }
                                        } else if (memItem instanceof NumberItem) {
                                            String instance = state.getString("instance");
                                            if ("temperature_k".equals(instance)) {
                                                Objects.requireNonNull(eventPublisher)
                                                        .post(ItemEventFactory.createCommandEvent(
                                                                cp.getTemperatureK().getOhID(), DecimalType.valueOf(
                                                                        String.valueOf(state.getInt("value")))));
                                            }
                                        } else if (memItem instanceof StringItem) {

                                            String instance = state.getString("instance");
                                            if ("scene".equals(instance)) {
                                                Objects.requireNonNull(eventPublisher)
                                                        .post(ItemEventFactory.createCommandEvent(cp.getScenesOhID(),
                                                                StringType.valueOf(state.getString("value"))));
                                            }
                                        }
                                    }
                                }
                            });

                        }
                    }
                }
            }
            return "DONE";
        } catch (Exception ex) {
            return "ERROR";
        }
    }
    // private static JSONArray getCapabilitiesState(Item item, String status) {
    // JSONObject itemJson = new JSONObject();
    // JSONObject state = new JSONObject();
    // JSONArray capabilitiesArray = new JSONArray();
    // if (item instanceof ColorItem) {
    // JSONObject capabilitiesObj = new JSONObject();
    // capabilitiesObj.put("type", "devices.capabilities.color_setting");
    // state.put("instance", "hsv");
    // state.put("action_result", new JSONObject().put("status", status));
    // capabilitiesObj.put("state", state);
    // capabilitiesArray.put(capabilitiesObj);
    // itemJson.put("capabilities", capabilitiesArray);
    // } else if (item instanceof DimmerItem) {
    // JSONObject capabilitiesObj = new JSONObject();
    // capabilitiesObj.put("type", "devices.capabilities.range");
    // state.put("instance", "brightness");
    // state.put("action_result", new JSONObject().put("status", status));
    // capabilitiesObj.put("state", state);
    // capabilitiesArray.put(capabilitiesObj);
    // itemJson.put("capabilities", capabilitiesArray);
    // } else if (item instanceof SwitchItem) {
    // JSONObject capabilitiesObj = new JSONObject();
    // capabilitiesObj.put("type", "devices.capabilities.on_off");
    // state.put("instance", "on");
    // state.put("action_result", new JSONObject().put("status", status));
    // capabilitiesObj.put("state", state);
    // capabilitiesArray.put(capabilitiesObj);
    // itemJson.put("capabilities", capabilitiesArray);
    // }
    // return capabilitiesArray;
    // }

    @Deactivate
    protected void deactivate() {
        try {
            httpClient.stop();
        } catch (Exception ignored) {
        }
        ScheduledFuture<?> refreshPollingJob = this.refreshPollingJob;
        if (refreshPollingJob != null && !refreshPollingJob.isCancelled()) {
            refreshPollingJob.cancel(true);
            this.refreshPollingJob = null;
        }
        yandexDevicesList.clear();
    }

    // private static JSONArray getCapabilitiesState(Item item) {
    // JSONObject itemJson = new JSONObject();
    // JSONObject state = new JSONObject();
    // JSONArray capabilitiesArray = new JSONArray();
    // if (item.getType().equals("Switch") && item.hasTag("Lightbulb")) {
    // JSONObject capabilitiesObj = new JSONObject();
    // capabilitiesObj.put("type", "devices.capabilities.on_off");
    // state.put("instance", "on");
    // if (item.getState().toString().equals("ON")) {
    // state.put("value", true);
    // } else {
    // state.put("value", false);
    // }
    // capabilitiesObj.put("state", state);
    // capabilitiesArray.put(capabilitiesObj);
    // itemJson.put("capabilities", capabilitiesArray);
    // } else if (item.getType().equals("Switch") && item.hasTag("RadiatorControl")) {
    // JSONObject capabilitiesObj = new JSONObject();
    // capabilitiesObj.put("type", "devices.capabilities.on_off");
    // state.put("instance", "on");
    // if (item.getState().toString().equals("ON")) {
    // state.put("value", true);
    // } else {
    // state.put("value", false);
    // }
    // capabilitiesObj.put("state", state);
    // capabilitiesArray.put(capabilitiesObj);
    // itemJson.put("capabilities", capabilitiesArray);
    // }
    // return capabilitiesArray;
    // }

    // private void getInfoFromYandex() {
    // if (!yandexId.isEmpty()) {
    // yandexId.clear();
    // }
    // HttpURLConnection con;
    // URL yandexURL = null;
    // try {
    // yandexURL = new URL("https://api.iot.yandex.net/v1.0/user/info");
    // con = (HttpURLConnection) yandexURL.openConnection();
    // con.setRequestMethod("GET");
    // con.setRequestProperty("Authorization", "Bearer " + yandexToken);
    // con.setRequestProperty("Content-Type", "application/json");
    // BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    // String inputLine;
    // StringBuilder response = new StringBuilder();
    // while ((inputLine = in.readLine()) != null) {
    // response.append(inputLine);
    // }
    // in.close();
    // String result = response.toString().trim();
    // // logger.debug("input string from REST: {}", result);
    // con.disconnect();
    // JSONObject yandexResponse = new JSONObject(result);
    // JSONArray devices = yandexResponse.getJSONArray("devices");
    // for (Object dev : devices) {
    // JSONObject device = (JSONObject) dev;
    // String id = device.get("id").toString();
    // String external_id = device.get("external_id").toString();
    // Item itm = itemRegistry.get(external_id);
    // if (itemRegistry.get(external_id) != null) {
    // yandexId.put(external_id, id);
    // }
    // }
    //
    // } catch (Exception e) {
    // logger.debug("getInfoFromYandex error {}", e.getLocalizedMessage());
    // }
    // }
}
