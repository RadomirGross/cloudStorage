# CloudStorage

Веб-приложение для облачного хранения файлов.

## 📋 ТЗ проекта
**Техническое задание:** https://zhukovsd.github.io/java-backend-learning-course/projects/cloud-file-storage/

## 🎨 Frontend
**Готовый фронтенд:** https://github.com/zhukovsd/cloud-storage-frontend/

## 🚀 Основные возможности

- **Загрузка файлов** с проверкой размера и доступного места
- **Управление директориями** (создание, навигация)
- **Поиск файлов** по имени
- **Скачивание** отдельных файлов и архивов папок
- **Перемещение ресурсов** между папками
- **Аутентификация и авторизация** пользователей
- **Защита от переполнения диска** с резервированием места

## 🏗️ Архитектура

### Backend
- **Java 17** + **Spring Boot 3**
- **Spring Security** для аутентификации
- **MinIO** в качестве объектного хранилища
- **PostgreSQL** для метаданных
- **Redis** для сессий
- **Liquibase** для миграций БД

### Frontend
- **React** 




## ⚙️ Настройка и запуск



1. **Клонируйте репозиторий:**
```bash
git clone https://github.com/RadomirGross/cloudStorage.git
cd cloudStorage
```

2. **Запустите инфраструктуру:**
```bash
docker-compose up -d 
```


Приложение будет доступно по адресу: `http://{HOST}`

## 📝 API Endpoints

### Аутентификация
- `POST /api/auth/sign-up` - Регистрация
- `POST /api/auth/sign-in` - Вход
- `POST /api/auth/sign-out` - Выход
- `GET /api/user/me` - Текущий пользователь

### Управление файлами
- `GET /api/directory` - Содержимое папки
- `POST /api/directory` - Создать папку
- `GET /api/resource` - Информация о файле
- `POST /api/resource` - Загрузить файл(ы)
- `DELETE /api/resource` - Удалить ресурс
- `GET /api/resource/move` - Переместить ресурс
- `GET /api/resource/download` - Скачать файл/папку
- `GET /api/resource/search` - Поиск файлов

Деплой [45.141.103.192](http://45.141.103.192)