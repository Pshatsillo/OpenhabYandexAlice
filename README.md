# OpenHab connector for Yandex Alice

В разработке. Требуется посильная помощь

Клонируем в папку openhab-addons\bundles

```git submodule add https://github.com/Pshatsillo/OpenhabYandexAlice.git org.openhab.io.yandexalice```

### Данный плагин работает только с частными версиями Openhab-cloud!

Инструкция по установке:

1. Получаем доступ к облаку Openhab (или свой, или тот, у кого есть доступ к авторизации через Openhab)
   
   * [Openhab-cloud](https://github.com/openhab/openhab-cloud)
   * Вносим в MongoDB Openhab запись для авторизации по Oauth 
  
```
    use openhab
    db.oauth2clients.insert({ clientId: "<CLIENT-ID>", clientSecret: "<CLIENT SECRET>"})
    db.oauth2scopes.insert({ name: "yandex"})
    db.oauth2scopes.insert( { name : "Yandex Alice", description: "Access to openHAB Cloud specific API for Yandex Alice", } )
 
  ```

  * Запоминаем логин и пароль <CLIENT-ID> - <CLIENT SECRET> - scope "yandex"

2. Создаём навык в [Алисе](https://dialogs.yandex.ru/developer/).
   * Заполняем следующие поля: Endpoint URL, URL авторизации, URL для получения токена, Идентификатор приложения, Секрет приложения, Идентификатор группы действий (scope)
   * Endpoint URL: https://home.OPENHABCLOUD/yandex (именно home)
   * URL авторизации: https://OPENHABCLOUD/oauth2/authorize (НЕ home)
   * URL для получения токена: https://OPENHABCLOUD/oauth2/token (НЕ home)
   * Идентификатор приложения: <CLIENT-ID>
   * Секрет приложения: <CLIENT SECRET>
   * Идентификатор группы действий (scope): yandex
3. Устанавливаем Openhab Cloud Connector, подключаемся к локальному облаку
4. Устанавливаем Yandex Alice
5. Авторизовываем навык в https://dialogs.yandex.ru/developer/ как черновой
6. Регистрируем навык в приложении Умный дом Яндекс
7. Получаем токен по ссылке в Openhab
8. Настраиваем Items в Openhab

## Работа с Openhab

1. Добавляем в желаемый Item Non-Semantic Tags - Yandex, обновляем приложение "Умный дом" Яндекса
2. Тип устройства определяется по Semantic Class, [Устройства Алисы](https://yandex.ru/dev/dialogs/smart-home/doc/concepts/device-types.html)

| OpenHab tag/item                     | Yandex                                                                     |  Describe   |
|--------------------------------------|----------------------------------------------------------------------------|-----|
| (Semantic)Lightbulb                  | devices.types.light                                                        |   Лампочка, светильник, ночник, люстра  |
| (Semantic)PowerOutlet                | devices.types.socket                                                       |  Розетка   |
| (Item no tag)Switch                  | devices.types.switch                                                       |  Выключатель   |
| (Item) Number (Semantic) Temperature | devices.types.sensor/ devices.properties.float/ temperature                |  Отображение показаний температуры   |
| (Item) Number (Semantic) Humidity    | devices.types.sensor/ devices.properties.float/           humidity         |  Отображение показаний влажности.   |
| (Item) Number (Semantic) CO2         | devices.types.sensor/ devices.properties.float/           co2_level        |   Отображение показаний уровня углекислого газа.|
| (Item) Color                         | devices.types.light/ devices.capabilities.color_setting                    |  Управление цветом для светящихся элементов в устройстве   |
| (Item) Dimmer                        | devices.types.light/                            devices.capabilities.range |  Изменение яркости световых элементов.   |
| (Item) RolleRshutter                 |   devices.types.openable.curtain                                                   |  Шторы, жалюзи.   |

## **Составные устройства**


1. Создаём Item Group и в тэге указываем что за устройство, исходя из [списка](https://yandex.ru/dev/dialogs/smart-home/doc/concepts/device-types.html)
2. Добавляем нужные нам Items, указываем нужные тэги.
   * Тэги указываем по [умениям](https://yandex.ru/dev/dialogs/smart-home/doc/concepts/capability-types.html) или [свойствам](https://yandex.ru/dev/dialogs/smart-home/doc/concepts/properties-types.html)
   * Если тэгов нет, то будут применяться свойства по умолчанию, а Number будет применять в качестве настроек [свойства](https://yandex.ru/dev/dialogs/smart-home/doc/concepts/float.html) float


