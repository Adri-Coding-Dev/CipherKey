![Logo](./assets/logo.png)

<strong>Robusto, fácil y seguro.</strong>
Gestor de contraseñas cifrado, 100% offline, desarrollado en Java.

---

![Java](https://img.shields.io/badge/Java-JDK%2025-orange)
![Build](https://img.shields.io/badge/Build-Maven-blue)
![License](https://img.shields.io/badge/License-MIT-green)
![Status](https://img.shields.io/badge/Status-Alpha-red)

---

## Descripción

CipherKey es un gestor de contraseñas cifrado y completamente offline desarrollado en Java (JDK 25) con Java Swing.

Permite crear una bóveda protegida por una contraseña maestra, almacenar credenciales de forma estructurada y generar contraseñas seguras, todo sin depender de servicios en la nube.

---

## Seguridad

- Protección mediante contraseña maestra
- Hash de contraseña usando SHA-256
- Persistencia en JSON cifrado
- Archivo de clave en formato .ckey
- Eliminación de datos sensibles de memoria tras su uso

---

## Arquitectura

- Java JDK 25
- Java Swing
- Maven
- Arquitectura monolítica
- Persistencia en JSON cifrado

---

## Funcionalidades

- Creación de bóveda
- Acceso mediante clave maestra
- Generación de contraseñas seguras
- Gestión de credenciales por usuario y dominio
- Eliminación segura en memoria

---

## Instalación

Requisitos:
- Java JDK 25
- Maven

Compilación:
mvn clean package

Ejecución:
java -jar target/cipherkey.jar

---

## Estado

Beta

---

## Licencia

MIT
