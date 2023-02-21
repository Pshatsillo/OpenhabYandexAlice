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
 Простое устройство Switch item, (не в составе группы) выбираем Semantic class:
   * Lightbulb - Лампочка, светильник, ночник, люстра.
   * PowerOutlet - Розетка
   * Без класса - Выключатель
