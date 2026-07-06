# GovPay EC Client 2.0.0

Major release della libreria client Java per le **API Notifica e Verifica** esposte dagli applicativi gestori delle posizioni debitorie dell'Ente Creditore verso la piattaforma GovPay. Allineata a **Spring Boot 4.x / Spring Framework 7.x** e a **Jackson 3.x**.

## Breaking changes

- **Jackson 3.x**: il groupId dei moduli databind/core passa da `com.fasterxml.jackson.*` a `tools.jackson.*`. I consumer del client generato che tipano esplicitamente `ObjectMapper`, `JsonMapper`, `JacksonException` ecc. devono aggiornare i propri import. Le annotation Jackson (`com.fasterxml.jackson.annotation.*`) restano invariate.
- **Spring Boot 4.x / Spring Framework 7.x**: requisiti minimi Java 17+ (progetto su Java 21), Jakarta EE 11 (Servlet 6.1, JPA 3.2, Bean Validation 3.1), Tomcat 11.0+. Applicazioni ospitanti che ancora usano Spring Boot 3.x non sono compatibili con questa release.
- **Bugfix schema OpenAPI**: `NuovaRendicontazione.esito` cambia da `type: number` a `type: string` per coerenza con l'enum (`ESEGUITO`, `REVOCATO`, `ESEGUITO_STANDIN`, `ESEGUITO_STANDIN_SENZA_RPT`, `ESEGUITO_SENZA_RPT`). In 1.0.0 il mismatch causava `NumberFormatException` in fase di inizializzazione dell'enum, rendendo di fatto inutilizzabile `notificaRendicontazione` lato client (fix backportato in 1.0.1 sulla linea Spring Boot 3.x).

## Security

- **Netty 4.2.15.Final** (transitivo, nessun override). Il precedente override a `4.2.14.Final` e' stato rimosso: Spring Boot 4.1.0 (via `govpay-bom 2.0.0`) porta gia' la 4.2.15.Final, che risolve le seguenti advisory attive sulla 4.2.14:
  - `netty-codec-classes-quic`: [GHSA-cmm3-54f8-px4j](https://osv.dev/GHSA-cmm3-54f8-px4j), [GHSA-cq4q-cv5g-r8q5](https://osv.dev/GHSA-cq4q-cv5g-r8q5)
  - `netty-codec-http`: [GHSA-hvcg-qmg6-jm4c](https://osv.dev/GHSA-hvcg-qmg6-jm4c)
  - `netty-codec-http2`: [GHSA-563q-j3cm-6jxm](https://osv.dev/GHSA-563q-j3cm-6jxm), [GHSA-5x3r-wrvg-rp6q](https://osv.dev/GHSA-5x3r-wrvg-rp6q), [GHSA-c2gf-v879-257j](https://osv.dev/GHSA-c2gf-v879-257j)
  - `netty-codec-http3`: [GHSA-4grm-h2qv-h6w6](https://osv.dev/GHSA-4grm-h2qv-h6w6), [GHSA-c2rx-5r8w-8xr2](https://osv.dev/GHSA-c2rx-5r8w-8xr2)
  - `netty-handler`: [GHSA-3qp7-7mw8-wx86](https://osv.dev/GHSA-3qp7-7mw8-wx86), [GHSA-c653-97m9-rcg9](https://osv.dev/GHSA-c653-97m9-rcg9), [GHSA-x4gw-5cx5-pgmh](https://osv.dev/GHSA-x4gw-5cx5-pgmh)
  - `netty-resolver-dns`: [GHSA-5pvg-856g-cp85](https://osv.dev/GHSA-5pvg-856g-cp85), [GHSA-676x-f7gg-47vc](https://osv.dev/GHSA-676x-f7gg-47vc), [GHSA-xmv7-r254-6q78](https://osv.dev/GHSA-xmv7-r254-6q78)
  - `netty-transport-native-epoll`: [GHSA-w573-9ffj-6ff9](https://osv.dev/GHSA-w573-9ffj-6ff9)

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
- Netty 4.2.15.Final (transitivo, nessun override)
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
