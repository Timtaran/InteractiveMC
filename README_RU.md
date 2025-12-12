# InteractiveMC

<p align="center">
    :ru: <strong>Русский</strong> | :gb: <a href="/README.md">English</a>
</p>

Аддон для ViveCraft, который добавляет в игру поддержки физических взаимодействий с предметами (более подробно в README конкретных проектов)

## Структура проекта

Этот репозиторий является моно-репозиторием и включает:
- [InteractiveMC](/mod/README_RU.md) - Основной мод
- [InteractiveMC Physics Server](/physics-server/README_RU.md) - Библиотеку, основанную на [Velthoric](https://github.com/xI-Mx-Ix/Velthoric), реализующую сервер физики для расчёта физических взаимодействий. Сервер является необязательным для клиента, но его использование рекомендуется. **Данная библиотека должна использоваться только в основном моде**
- [InteractiveMC Debug Utils]() - Мод с набором утилит для тестирования мода, не предполагаемых в основном моде

## Благодарности
* [Velthoric](https://github.com/xI-Mx-Ix/Velthoric) - База для сервера физики
* [Stonecutter](https://codeberg.org/stonecutter/stonecutter) - Gradle-плагин для мультиверси
* [Jolt Physics](https://github.com/jrouwe/JoltPhysics) - Физический движок
* [JoltJNI](https://github.com/stephengold/jolt-jni) - Привязки JNI для Jolt Physics