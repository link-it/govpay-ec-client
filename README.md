<p align="center">
<img src="https://www.link.it/wp-content/uploads/2025/01/logo-govpay.svg" alt="GovPay Logo" width="200"/>
</p>

# GovPay - Porta di accesso al sistema pagoPA - EC Client

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=link-it_govpay-ec-client&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=link-it_govpay-ec-client)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://raw.githubusercontent.com/link-it/govpay-ec-client/main/LICENSE)
[![Spring Boot](https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Fraw.githubusercontent.com%2Flink-it%2Fgovpay-bom%2Fmain%2Fpom.xml&query=%2F%2F*%5Blocal-name()%3D'spring-boot.version'%5D&label=Spring%20Boot&color=brightgreen)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)

Libreria client Java, generata da OpenAPI, per invocare le **API Notifica e Verifica** che il gestionale dell'Ente Creditore espone a GovPay: verifica delle pendenze, notifica delle ricevute di pagamento e delle rendicontazioni.

## Sommario

La libreria non è un'applicazione: è un jar che i componenti GovPay includono come dipendenza Maven per chiamare il gestionale dell'ente. Il verso della chiamata è GovPay → ente: il gestionale implementa le API, GovPay è il client.

Il codice del client non è nel repository: è generato a ogni build, nella fase `generate-sources`, a partire dalla specifica [src/main/resources/openapi/govpay-api-ec.yaml](src/main/resources/openapi/govpay-api-ec.yaml) (**GovPay - API Notifica e Verifica**, versione `2.1.1`, OpenAPI 3.0) e dai file che richiama (`govpay-api-schemas.yaml`, `govpay-api-parameters.yaml`, `govpay-api-responses.yaml`, `govpay-api-examples.yaml`).

Generazione:

| Parametro | Valore |
|-----------|--------|
| Plugin | `org.openapitools:openapi-generator-maven-plugin`, versione da govpay-bom (proprietà `openapi.tool.codegen.version`, oggi `7.26.0`) |
| Generatore | `java` (client), libreria `resttemplate` |
| Serializzazione | Jackson 3 (`useJackson3`), date `java8` (`java.time`) |
| Altre opzioni | `useSpringBoot4`, `useJakartaEe`, `openApiNullable=false`, `useBeanValidation=false`, `useAnnotatedBasePath`, `booleanGetterPrefix=is` |
| Destinazione | `target/generated-sources/java/` (inclusa nel jar e nel jar dei sorgenti) |

Contenuto del jar:

| Package | Contenuto |
|---------|-----------|
| `it.govpay.ec.client.api` | Una classe per tag OpenAPI: `VerificaPendenzeApi`, `NotificaRicevuteApi`, `NotificaRendicontazioniApi` (generate) |
| `it.govpay.ec.client.api.impl` | `ApiClient` e infrastruttura di invocazione su `RestTemplate` (generati) |
| `it.govpay.ec.client.api.impl.auth` | Classi di autenticazione del generatore (`HttpBasicAuth`, `HttpBearerAuth`, `ApiKeyAuth`): la specifica non dichiara schemi di sicurezza, quindi non sono registrate (vedi [Autenticazione](#autenticazione)) |
| `it.govpay.ec.client.beans` | Modelli (DTO) generati dagli schemi: `PendenzaVerificata`, `NuovaPendenza`, `Ricevuta`, `NuovaRendicontazione`, ... |
| `it.govpay.ec.client` | `GovPayEcApiClient`, estensione scritta a mano di `ApiClient` per i payload legacy (vedi [Compatibilità con i payload EC legacy](#compatibilità-con-i-payload-ec-legacy)); `Costanti` |

Operazioni esposte:

| Classe | Metodo | Operazione HTTP | Scopo |
|--------|--------|-----------------|-------|
| `VerificaPendenzeApi` | `getAvviso(idDominio, numeroAvviso)` | `GET /avvisi/{idDominio}/{numeroAvviso}` | Verifica di una pendenza a partire dall'avviso |
| `VerificaPendenzeApi` | `verifyPendenza(idA2A, idPendenza)` | `GET /pendenze/{idA2A}/{idPendenza}` | Verifica di una pendenza a partire dal suo identificativo |
| `VerificaPendenzeApi` | `verifyPendenzaMod4(idDominio, idTipoPendenza, body)` | `POST /pendenze/{idDominio}/{idTipoPendenza}` | Acquisizione di una pendenza da dati custom (modello 4) |
| `NotificaRicevuteApi` | `notificaRicevuta(idDominio, iuv, idRicevuta, idSession, idSessionePortale, idCarrello, ricevuta)` | `PUT /ricevute/{idDominio}/{iuv}/{idRicevuta}` | Notifica di una ricevuta di pagamento pagoPA |
| `NotificaRendicontazioniApi` | `notificaRendicontazione(nuovaRendicontazione)` | `POST /rendicontazioni` | Notifica di una rendicontazione di un flusso pagoPA |

Ogni metodo ha anche la variante `...WithHttpInfo`, che restituisce la `ResponseEntity` con stato e header. Gli errori HTTP arrivano come `RestClientException` (in particolare `HttpClientErrorException` e `HttpServerErrorException`) di Spring.

## Integrazione con GovPay

| Componente | Quando serve | Obbligatorio | Ruolo |
|------------|--------------|--------------|-------|
| [govpay-bom](https://github.com/link-it/govpay-bom) | build | sì | Parent Maven: versioni delle dipendenze e dei plugin, compresi Spring Boot, Jackson e il generatore OpenAPI (`openapi.tool.codegen.version`) |
| Gestionale dell'Ente Creditore | runtime | sì | Implementa le API Notifica e Verifica. Il client lo chiama all'URL impostato con `setBasePath(...)`; nei componenti GovPay è l'URL del connettore dell'applicazione |
| [govpay-common](https://github.com/link-it/govpay-common) | runtime, nei progetti che usano il client | no | Non è una dipendenza di questa libreria. I progetti GovPay ne usano il `ConnettoreService`, che costruisce dal connettore su database il `RestTemplate` (autenticazione, TLS, timeout, correlation id, eventi GDE) e fornisce l'URL da passare al client |
| Progetti che usano la libreria | build | - | Dipendenza diretta nel `pom.xml`: [govpay-notify-batch](https://github.com/link-it/govpay-notify-batch) (versione `2.0.1`: notifica delle ricevute e delle rendicontazioni con `ApiClient`), [govpay-pafornode-api](https://github.com/link-it/govpay-pafornode-api) (proprietà `govpay-ec-client.version`, oggi `2.0.3-SNAPSHOT`: verifica dell'avviso con `GovPayEcApiClient`) |

Il core [govpay](https://github.com/link-it/govpay) chiama le stesse API con un proprio client e non usa questa libreria.

## Compilazione

Requisiti: JDK 21 e Maven 3.8.1 o successivo.

```bash
mvn clean install
```

Il risultato è `target/govpay-ec-client-<versione>.jar` (più i jar dei sorgenti e della javadoc), installato nel repository Maven locale. La build genera il client dalla specifica, esegue i test con il report di copertura JaCoCo (`target/site/jacoco/`) e, nella fase `verify`, l'analisi OWASP Dependency-Check delle dipendenze. Per escludere l'analisi OWASP, per esempio in sviluppo:

```bash
mvn clean install -Dowasp.phase=none
```

Altri comandi utili:

```bash
# Solo la generazione del client (in target/generated-sources/java/)
mvn generate-sources

# Solo i test
mvn clean test

# Test singolo
mvn test -Dtest=GovPayEcApiClientTest
```

I test (11) verificano la creazione e la configurazione di `ApiClient`, la serializzazione dei modelli e, con `MockWebServer`, le chiamate HTTP di `GovPayEcApiClient` con i payload legacy.

## Utilizzo

Coordinate Maven:

```xml
<dependency>
    <groupId>org.gov4j.govpay</groupId>
    <artifactId>govpay-ec-client</artifactId>
    <version>${govpay-ec-client.version}</version>
</dependency>
```

La versione non è gestita da govpay-bom: il progetto che usa la libreria la imposta da sé. La versione corrente di sviluppo è `2.0.3-SNAPSHOT` (pom su `main`); l'ultima pubblicata su Maven Central è `2.0.1`. Le versioni SNAPSHOT sono pubblicate su `https://central.sonatype.com/repository/maven-snapshots`. La linea `2.x` richiede Spring Boot 4 e Jackson 3; la linea `1.x` (ramo `1.x.x`) è per Spring Boot 3.

La libreria porta con sé `spring-boot-starter-webflux` e `spring-boot-starter-validation`; non ha auto-configurazioni né bean Spring: il client si costruisce nel codice.

Uso tipico in un componente GovPay, con il `RestTemplate` e l'URL presi dal connettore:

```java
import it.govpay.ec.client.GovPayEcApiClient;
import it.govpay.ec.client.api.VerificaPendenzeApi;
import it.govpay.ec.client.beans.PendenzaVerificata;

RestTemplate restTemplate = connettoreService.getRestTemplate(codConnettore);
String url = connettoreService.getConnettore(codConnettore).getUrl();

GovPayEcApiClient apiClient = new GovPayEcApiClient(restTemplate);
apiClient.setBasePath(url);

PendenzaVerificata pendenza = new VerificaPendenzeApi(apiClient).getAvviso(idDominio, numeroAvviso);
```

Senza `RestTemplate` esterno:

```java
GovPayEcApiClient apiClient = new GovPayEcApiClient();
apiClient.setBasePath("https://gestionale.ente.example/govpay/ec/v2");

new NotificaRicevuteApi(apiClient)
        .notificaRicevuta(idDominio, iuv, idRicevuta, null, null, null, ricevuta);
```

### Compatibilità con i payload EC legacy

I modelli generati delle voci di pendenza (`NuovaVocePendenza`, `VocePendenza`, `VocePendenzaPagata`) e dei documenti (`NuovoDocumento`, `Documento`) hanno il discriminatore Jackson `tipoRiferimento`, che i payload dei gestionali esistenti non contengono. Con il solo `ApiClient` generato la lettura di quei payload fallisce.

`GovPayEcApiClient` estende `ApiClient` e applica a quelle gerarchie un mix-in che disattiva il discriminatore: il tipo di voce o di documento si deduce dai campi presenti, `tipoRiferimento` viene ignorato in lettura e non viene scritto. La validazione della combinazione dei campi resta a carico del chiamante; nessun IBAN o riferimento contabile viene inventato quando manca.

- Con `new GovPayEcApiClient(restTemplate)` il `RestTemplate` conserva interceptor, autenticazione, request factory, timeout e observation: vengono sostituiti solo i converter JSON Jackson 3, ricostruiti dal mapper esistente con i mix-in aggiunti. Il mapper originale, anche se condiviso con l'applicazione, non viene modificato; il template va però dedicato al client EC. Un template senza converter JSON Jackson 3 viene rifiutato con `IllegalArgumentException`.
- `new ApiClient()`, `new ApiClient(restTemplate)` e il costruttore senza argomenti delle API generate non abilitano la compatibilità.
- Né la specifica né i sorgenti generati sono modificati: le API generate accettano `GovPayEcApiClient` perché estende `ApiClient`.

### Compatibilità

- Java 21+
- Spring Boot 4.x e Jackson 3 (versioni definite dal parent [govpay-bom](https://github.com/link-it/govpay-bom))
- Specifica GovPay - API Notifica e Verifica `2.1.1` (OpenAPI 3.0)

## Configurazione

La libreria non legge proprietà (`application.properties`, variabili d'ambiente o `@ConfigurationProperties`): si configura nel codice, sull'`ApiClient` e sul `RestTemplate` che gli si passa.

| Aspetto | Come si imposta | Default |
|---------|-----------------|---------|
| URL del gestionale | `apiClient.setBasePath(url)` | `https://host/baseurl` (segnaposto della specifica: va sempre impostato) |
| Autenticazione | Sul `RestTemplate` passato al costruttore (interceptor, request factory con certificato client); nei componenti GovPay la configura `ConnettoreService` dal connettore | nessuna |
| Timeout | Sulla request factory del `RestTemplate` passato al costruttore | quelli del `RestTemplate` di Spring creato da `buildRestTemplate()`, cioè nessun timeout esplicito |
| Header aggiuntivi | `apiClient.addDefaultHeader(nome, valore)` | nessuno |
| User agent | `apiClient.setUserAgent(valore)` | `OpenAPI-Generator/2.1.1/java` |
| Tentativi | `apiClient.setMaxAttemptsForRetry(n)` e `setWaitTimeMillis(ms)`: il client ripete la chiamata sulle risposte `5xx` e `429 Too Many Requests` | `1` tentativo (nessuna ripetizione), `10` ms di attesa |
| Log delle chiamate | `apiClient.setDebugging(true)`: aggiunge al `RestTemplate` un interceptor che scrive a livello INFO URL, header (compreso `Authorization`) e corpo di richieste e risposte. Da non usare in produzione | `false` |

### Autenticazione

La specifica non dichiara `securitySchemes`, quindi la mappa `getAuthentications()` dell'`ApiClient` è vuota e non ci sono metodi come `setUsername` o `setBearerToken` utilizzabili. L'autenticazione verso il gestionale si configura sul `RestTemplate`, per esempio:

```java
RestTemplate restTemplate = new RestTemplateBuilder()
        .basicAuthentication(utente, password)
        .connectTimeout(Duration.ofSeconds(10))
        .readTimeout(Duration.ofSeconds(30))
        .build();

GovPayEcApiClient apiClient = new GovPayEcApiClient(restTemplate);
apiClient.setBasePath(url);
```

Nei componenti GovPay non serve: il `RestTemplate` restituito da `ConnettoreService.getRestTemplate(codConnettore)` di govpay-common applica già l'autenticazione del connettore (HTTP Basic, certificato client, header personalizzato, API key, OAuth2 client credentials), il TLS e i timeout.

## Licenza

GovPay EC Client è distribuito con licenza [GNU General Public License v3.0](LICENSE).

Copyright (c) 2014-2026 Link.it srl.
