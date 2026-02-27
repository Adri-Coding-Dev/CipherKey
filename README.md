<p align="center">
  <img src="./assets/logo.png" width="180"/>
</p>

<h1 align="center">🔐 CipherKey</h1>

<p align="center">
  <strong>Robusto. Simple. Seguro.</strong><br>
  Gestor de contraseñas cifrado, 100% offline, desarrollado en Java.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-JDK%2025-orange?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Build-Maven-blue?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Status-Beta-yellow?style=for-the-badge"/>
</p>

---

## 📌 Descripción

**CipherKey** es un gestor de contraseñas cifrado y completamente offline desarrollado en **Java (JDK 25)** con **Java Swing**.

Permite:

- 🔐 Crear una bóveda protegida por contraseña maestra
- 🗂️ Almacenar credenciales estructuradas
- 🔑 Generar contraseñas seguras
- ☁️ Operar sin depender de servicios en la nubee

> Tu seguridad no debería depender de terceros.

---

## 🛡️ Seguridad

| Característica | Implementación |
|---------------|----------------|
| Contraseña maestra | Protección obligatoria |
| Hash seguro | SHA-256 |
| Persistencia | JSON cifrado |
| Archivo de clave | `.ckey` |
| Seguridad en memoria | Limpieza de datos sensibles tras uso |

---

## 🏗️ Arquitectura

| Tecnología | Uso |
|------------|------|
| Java JDK 25 | Núcleo del sistema |
| Java Swing | Interfaz gráfica |
| Maven | Gestión de dependencias |
| Arquitectura | Monolítica |
| Persistencia | JSON cifrado |

---

## ⚙️ Funcionalidades

- ✅ Creación de bóveda
- ✅ Acceso mediante clave maestra
- ✅ Generación de contraseñas seguras
- ✅ Gestión por usuario y dominio
- ✅ Eliminación segura en memoria

---

## 🚀 Instalación

### 📋 Requisitos

- Java JDK 25
- Maven

### 🔨 Compilación

```bash
mvn clean package
```
▶️ Ejecución
```bash
java -jar target/cipherkey.jar
```
📊 Estado del Proyecto

🟡 **Beta**

El proyecto es funcional pero está en evolución constante, con búsqueda de errores y posibles mejoras de optimización y seguridad.
Se planean mejoras en:

> UI/UX

> Optimización de cifrado

> Tests automatizados

> Modularización futura

### 📄 Licencia

Distribuido bajo licencia MIT.

<p align="center"> Hecho con ☕ y Java </p>
