**Link Tracker**   

Микросервисное приложение для отслеживания обновлений по ссылкам (GitHub-репозитории, StackOverflow-вопросы) с уведомлениями через Telegram-бота.                                
   
  Архитектура                                                                                                                                                                      
                  
  Проект состоит из 3 модулей (multi-module Maven, Spring Boot 4):                                                                                                                 
   
  - Bot — Telegram-бот для взаимодействия с пользователем. Команды: /start, /track, /untrack, /list, /help, /cancel. Управление состоянием пользователя через UserStateManager.    
  - Scrapper — сервис, который периодически проверяет отслеживаемые ссылки на обновления через GitHub API и StackOverflow API, и отправляет уведомления через бота. Поддерживает
  пакетную и многопоточную обработку ссылок.                                                                                                                                       
  - AI Agent — модуль для интеграции с AI.
                                                                                                                                                                                   
  Технологии      

  - Java 25, Spring Boot 4, Maven                                                                                                                                                  
  - PostgreSQL + Liquibase (миграции)
  - gRPC (межсервисное взаимодействие) + REST API                                                                                                                                  
  - JDBC и JPA (два варианта работы с БД)
  - Docker Compose
  - Lombok, Checkstyle, PMD, SpotBugs                                                                                                                                              
   
  В планах                                                                                                                                                                         
                  
  - Apache Kafka - очередь сообщений для асинхронного взаимодействия между сервисами                                                                                               
  - Redis — кэширование для снижения нагрузки на БД и внешние API
                                                                                                                                                                                   
  Запуск          
                                                                                                                                                                                   
  # Поднять БД и применить миграции                                                                                                                                                
  docker-compose up -d
                                                                                                                                                                                   
  # Собрать проект
  ./mvnw clean install

  # Запустить модули
  ./mvnw -pl scrapper spring-boot:run
  ./mvnw -pl bot spring-boot:run
