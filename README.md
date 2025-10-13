# Majetek - Asset Inventory System

A full-stack web application for managing company assets, built for a specific school's deployment environment. Built for bachelor's thesis.

---

## 🛠️ Tech Stack

* **Backend:** Java 17, Spring Boot 3, JPA/Hibernate, PostgreSQL, Maven
* **Frontend:** React 19, TypeScript, Material-UI (MUI), Zustand
* **Android:** Java 17, Android SDK 36, SQLite
* **Deployment:** Nginx, Ubuntu VPS

---

## 🌐 Deployment

This application is deployed on a dedicated VPS using Nginx as a reverse proxy.

* The React frontend is built into static files and served directly by Nginx.
* Nginx forwards all API requests (under `/api`) to the backend Spring Boot application.
* The Spring Boot application runs as a `systemd` service, connected to a production PostgreSQL database.
