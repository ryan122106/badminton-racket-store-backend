\# Badminton Racket Store Backend



Spring Boot REST API for an Android badminton racket shopping application.



\## Features



\* User registration and login

\* JWT authentication

\* User and admin roles

\* Racket product management

\* Shopping cart and wishlist support

\* Orders and order items

\* Stripe test payments

\* Payment status tracking

\* Product reviews

\* Product image uploads



\## Technologies



\* Java

\* Spring Boot

\* Spring Security

\* Spring Data JPA

\* JWT

\* Stripe API

\* Microsoft SQL Server

\* Maven



\## Requirements



\* JDK 17 or newer

\* Microsoft SQL Server

\* Maven Wrapper

\* Stripe test account for payment testing



\## Project Configuration



The real `application.properties` file is excluded from Git because it contains private information.



Create your local configuration from the example:



```powershell

Copy-Item `

&#x20; src/main/resources/application.properties.example `

&#x20; src/main/resources/application.properties

```



Configure these environment variables or replace their placeholders only in your local `application.properties`:



\* `DB\_URL`

\* `DB\_USERNAME`

\* `DB\_PASSWORD`

\* `JWT\_SECRET`

\* `JWT\_EXPIRATION`

\* `STRIPE\_SECRET\_KEY`

\* `STRIPE\_PUBLISHABLE\_KEY`

\* `STRIPE\_WEBHOOK\_SECRET`

\* `STRIPE\_CURRENCY`



Never commit real passwords, API secret keys, JWT secrets or webhook secrets.



\## Run the Backend



```powershell

.\\mvnw.cmd spring-boot:run

```



The API runs at:



```text

http://localhost:8080

```



Example endpoint:



```text

http://localhost:8080/rackets

```



\## Android Phone Connection



To access the backend fro
m an Android device through ADB:



```powershell

adb reverse tcp:8080 tcp:8080

```



Use this Retrofit base URL:



```text

http://127.0.0.1:8080/

```



\## Important



This project uses Stripe test mode. Do not use real card information or commit Stripe secret keys.
