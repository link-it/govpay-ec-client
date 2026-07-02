# GovPay EC Client 2.0.0

Major release della libreria client Java per le **API Notifica e Verifica** esposte dagli applicativi gestori delle posizioni debitorie dell'Ente Creditore verso la piattaforma GovPay. Allineata a **Spring Boot 4.x / Spring Framework 7.x** e a **Jackson 3.x**.

## Breaking changes

- **Jackson 3.x**: il groupId dei moduli databind/core passa da `com.fasterxml.jackson.*` a `tools.jackson.*`. I consumer del client generato che tipano esplicitamente `ObjectMapper`, `JsonMapper`, `JacksonException` ecc. devono aggiornare i propri import. Le annotation Jackson (`com.fasterxml.jackson.annotation.*`) restano invariate.
- **Spring Boot 4.x / Spring Framework 7.x**: requisiti minimi Java 17+ (progetto su Java 21), Jakarta EE 11 (Servlet 6.1, JPA 3.2, Bean Validation 3.1), Tomcat 11.0+. Applicazioni ospitanti che ancora usano Spring Boot 3.x non sono compatibili con questa release.
- **Bugfix schema OpenAPI**: `NuovaRendicontazione.esito` cambia da `type: number` a `type: string` per coerenza con l'enum (`ESEGUITO`, `REVOCATO`, `ESEGUITO_STANDIN`, `ESEGUITO_STANDIN_SENZA_RPT`, `ESEGUITO_SENZA_RPT`). In 1.0.0 il mismatch causava `NumberFormatException` in fase di inizializzazione dell'enum, rendendo di fatto inutilizzabile `notificaRendicontazione` lato client.

## Funzionalita'

Client generato automaticamente da OpenAPI (`openapi-generator-maven-plugin`, libreria `resttemplate`, Jakarta EE, Jackson 3, Spring Boot 4) sulla specifica **GovPay - API Notifica e Verifica v2.1.1**:

- **Verifica pendenze**: `getAvviso`, `verifyPendenza`, `verifyPendenzaMod4`
- **Notifica ricevute**: `notificaRicevuta` (ricevute di pagamento pagoPA)
- **Notifica rendicontazioni**: `notificaRendicontazione` (singola rendicontazione di un flusso pagoPA)

## Stack tecnologico

- Java 21
- Spring Boot 4.1.0 (WebFlux + Validation)
- Spring Framework 7.0.8
- Jackson 3.1.4 (groupId `tools.jackson`)
- Parent POM `org.gov4j.govpay:govpay-bom:2.0.0`
- Override Netty 4.2.14.Final (CVE-2026-42577..42587, CVE-2026-44248, CVE-2026-41417)
- OpenAPI Generator con `useSpringBoot4=true`, `useJackson3=true`, `openApiNullable=false`

## Pipeline CI/CD

- Build, test e code coverage (JaCoCo)
- Analisi qualita' codice su SonarCloud
- Verifica vulnerabilita' dipendenze: OWASP Dependency-Check (NVD + OSS Index) e OSV Scanner (SARIF)
- Analisi compatibilita' licenze
- Generazione **SBOM CycloneDX** (json + xml) tramite `cyclonedx-maven-plugin`
- Gating obbligatorio `build + osv-scan + sbom` prima di ogni step di deploy o release
- Cache OWASP Dependency-Check versionata sulla `owasp.plugin.version` (bump del plugin → cache invalidata) e refresh notturno del DB NVD via workflow schedulato
- Pubblicazione SNAPSHOT su Maven Central su push in `main`
- Release automatica su tag con JAR firmato e archivio `release-reports-{tag}.zip` contenente report OWASP, JaCoCo, OSV, SBOM e licenze

## Artefatto Maven

```xml
<dependency>
    <groupId>org.gov4j.govpay</groupId>
    <artifactId>govpay-ec-client</artifactId>
    <version>2.0.0</version>
</dependency>
```

## Licenza

GNU GPL v3 — Copyright (c) 2014-2026 Link.it srl
