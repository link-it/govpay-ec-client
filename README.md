<p align="center">
<img src="https://www.link.it/wp-content/uploads/2025/01/logo-govpay.svg" alt="GovPay Logo" width="200"/>
</p>

# GovPay EC Client

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://raw.githubusercontent.com/link-it/govpay-ec-client/main/LICENSE)

Libreria client Spring Boot per l'integrazione con le **API Notifica e Verifica** che gli applicativi gestori delle posizioni debitorie dell'Ente Creditore espongono verso la piattaforma GovPay.

Le classi (API e modelli) sono generate automaticamente in fase di build a partire dalla specifica OpenAPI tramite `openapi-generator-maven-plugin` (libreria `resttemplate`, serializzazione Jackson, Jakarta EE).

## Installazione

```xml
<dependency>
    <groupId>org.gov4j.govpay</groupId>
    <artifactId>govpay-ec-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

## API supportate

Specifica di riferimento: **GovPay - API Notifica e Verifica v2.1.1**.

### Verifica pendenze

| Metodo | Path | Operation |
|--------|------|-----------|
| `GET`  | `/avvisi/{idDominio}/{numeroAvviso}` | `getAvviso` - verifica di una pendenza da avviso |
| `GET`  | `/pendenze/{idA2A}/{idPendenza}` | `verifyPendenza` - verifica di una pendenza da identificativo |
| `POST` | `/pendenze/{idDominio}/{idTipoPendenza}` | `verifyPendenzaMod4` - acquisizione di una pendenza con dati custom |

### Notifica ricevute

| Metodo | Path | Operation |
|--------|------|-----------|
| `PUT`  | `/ricevute/...` | `notificaRicevuta` - notifica di una ricevuta di avvenuto pagamento pagoPA |

### Notifica rendicontazioni

| Metodo | Path | Operation |
|--------|------|-----------|
| `POST` | `/rendicontazioni` | `notificaRendicontazione` - notifica di una singola rendicontazione di un flusso pagoPA |

## Struttura dei package generati

| Package | Contenuto |
|---------|-----------|
| `it.govpay.ec.client.api` | Interfacce client (una per tag OpenAPI) |
| `it.govpay.ec.client.api.impl` | `ApiClient` e infrastruttura di invocazione (`RestTemplate`) |
| `it.govpay.ec.client.beans` | Modelli (DTO) generati dagli schemi OpenAPI |

I sorgenti generati sono prodotti in `target/generated-sources/java/` durante la fase `generate-sources`.

## Utilizzo

```java
import it.govpay.ec.client.api.NotificaRicevuteApi;
import it.govpay.ec.client.api.impl.ApiClient;
import it.govpay.ec.client.beans.Ricevuta;

ApiClient apiClient = new ApiClient();
apiClient.setBasePath("https://ente.example.it/govpay/ec/v2");

NotificaRicevuteApi notifiche = new NotificaRicevuteApi(apiClient);
notifiche.notificaRicevuta(idDominio, iuv, ccp, ricevuta);
```

In contesto Spring Boot, l'`ApiClient` puo' essere esposto come `@Bean` per condividere configurazione, interceptor (es. autenticazione, logging, GDE) e timeout fra tutte le API.

## Build

```bash
# Build completa con generazione sorgenti, compilazione, test e packaging
mvn clean install

# Solo generazione delle classi dall'OpenAPI
mvn generate-sources

# Test
mvn clean test

# OWASP dependency check
mvn clean verify

# Disabilitare OWASP durante sviluppo
mvn clean verify -Dowasp.phase=none
```

## Compatibilita'

- Java 21+
- Spring Boot 3.x (parent: `govpay-bom` 1.1.3)
- OpenAPI 3.0 / GovPay API EC v2.1.1

## Licenza

Copyright (c) 2014-2026 Link.it srl - Licenza GNU GPL v3
