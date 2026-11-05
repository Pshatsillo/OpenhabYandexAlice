# AGENTS.md — org.openhab.io.yandexalice

openHAB I/O add-on that exposes tagged openHAB Items to the Yandex Smart Home (Alice) skill API.
Parent repo conventions apply — read `../../AGENTS.md` (Java 21, spotless, code analysis at build time).

## Git layout (easy to get wrong)

This directory is its **own git repository** (own `.git`, remote = fork `Pshatsillo/OpenhabYandexAlice`), nested inside an
`openhab-addons` checkout that does **not** track it. Commit/push from inside this directory; never copy sources into
the outer openhab-addons repo.

`YandexAliceCredits.java` (holds real skillID/OAuth token) is **not stored in this repo** — the tracked path
`src/main/java/.../internal/YandexAliceCredits.java` is a git **symlink** (mode 120000) into the private submodule
`private-src/` (repo `Pshatsillo/YandexAliceCredit`, also carries `openhabcloud/secret` and `uuid`). Fresh clone:
`git clone --recursive` — the link materializes automatically and the bundle compiles. Windows caveat: git needs
symlink support to check it out (Developer Mode or elevated git); without it the file lands as plain text and
compilation fails visibly — enable Developer Mode and re-checkout. Never commit real credential values here.

## Build / verify

The bundle is **not listed in `bundles/pom.xml` modules** — reactor `-pl` does not work. Build from the bundle folder:

```powershell
mvn spotless:apply
mvn clean install        # from this directory
```

- The `<parent><version>` in `pom.xml` must equal the repo reactor version (currently `5.2.4-SNAPSHOT`). After rebasing
  onto updated upstream, Maven fails with "Non-resolvable parent POM" until the version is bumped here too. Never set a
  project `<version>` in this pom — it comes from the parent reactor.
- Unit tests: `src/test/java` (currently `YandexAliceJsonTest`, `YandexDeviceTest`; JUnit 5 only). junit/mockito/assertj
  come from `org.openhab.core.bom.test` inherited via `bundles/pom.xml` — no test deps needed in this pom.
  Run one test: `mvn test -Dtest=YandexDeviceTest`.
- Static analysis (checkstyle/PMD/SpotBugs, null annotations) runs during `install`; results in
  `target/code_analysis/report.html`. Null-analysis (`@NonNullByDefault`) violations fail the build — annotate `@Nullable` early.
- SpotBugs forks a ~1 GB JVM and fails with a native OOM (`hs_err_pid*.log`) on low-memory machines; retry or free RAM
  (an environment issue, not a code issue).
- `src/main/history/dependencies.xml` is a build-generated Karaf baseline — after a version bump the karaf plugin
  reports "Dependencies have changed"; delete the file and rebuild instead of hand-editing.
- Output jar `target/org.openhab.io.yandexalice-*-SNAPSHOT.jar` is deployed manually to openHAB's `addons/` folder
  (not published to the openHAB addon repo). User-facing docs live in the wiki: https://github.com/Pshatsillo/OpenhabYandexAlice/wiki

## Architecture (not obvious from file names)

Everything lives in `org.openhab.io.yandexalice.internal`; `YandexService` is the single OSGi component
(config PID `org.openhab.yandexalice`, config-description URI `io:yandexalice`) and the hub of all logic (~1400 lines).

There are **no Things/thing handlers** despite this being an `io` binding. Flow:

- `YandexService.activate()` registers `YandexAliceCallbackServlet` at `/yandex` via `HttpService`. Yandex (tunnelled
  through the OH Cloud/webhook) calls `GET /yandex` (device list), `POST /yandex/v1.0/user/devices/query` and
  `POST /yandex/v1.0/user/devices/action`.
- Outbound state pushes go from `YandexCallbackUpdate` to `https://dialogs.yandex.net/api/v1/skills/<skillID>/callback/state`
  with `OAuth <token>` — both values come from config params `skillID` / `oauth` (see `src/main/resources/OH-INF/config/config.xml`).
- `YandexService` is an `EventSubscriber` for `ItemStateEvent` (entry point `receive(Event)`); a
  `scheduleWithFixedDelay(this::refresh, 5, 60, SECONDS)` job also re-pushes states every 60 s.
- Devices are synthesized from **Item tags**: an item is exposed only if it has the `Yandex` tag; extra tags select the
  Yandex device type / capabilities (`Lightbulb`, `PowerOutlet`, `brightness`, `temperature_k`, `smart_meter`,
  sensor-unit tags, device-type suffixes matched against `constants.YandexAliceDevicesConstants.DEV_LIST` via
  case-insensitive suffix matching). Changing exposure/mapping means editing `getItemsList()`, `receive()` and
  `refresh()` in `YandexService` — the mapping logic is duplicated between those methods.
- JSON model builders: `YandexAliceJson` (request/response shaping), `YandexDevice` (device + capability/instance
  constants), `YandexAliceCapabilities` / `YandexAliceProperties`, `YandexAliceCredits` (skillID + oauth holder).

## Dependencies & config gotchas

- Maven dependency `org.json:json` is at `20231013` in `pom.xml`, but `src/main/feature/feature.xml` hardcodes the
  **older `20180813`** — these are currently out of sync; keep both in sync when bumping.
- `src/main/history/dependencies.xml` is a build-generated Karaf features snapshot — do not hand-edit.
- `lib/jsoup-1.15.3.jar` and the `<jsoup.version>` property in `pom.xml` are stale leftovers: not used by any build
  config or source file.
- Servlet API is `javax.servlet` / `javax.ws.rs` (not Jakarta) — openHAB 5 runtime.
- New config parameters require changes in two places: `src/main/resources/OH-INF/config/config.xml` and the `CFG_*`
  constants handled in `YandexService.activate()`/`modified()`.
- Every `.java` file needs the EPL-2.0 license header (`licenses/epl-2.0/header.txt`) or spotless fails.
