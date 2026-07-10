# GovPay EC Client 2.0.1

Maintenance release sulla linea 2.x (Spring Boot 4.x / Spring Framework 7.x). Allinea il parent POM alla nuova release del BOM. Per i cambiamenti maggiori (breaking changes, migrazione a Spring Boot 4 e Jackson 3, fix di sicurezza Netty) vedere le [note di rilascio della 2.0.0](https://github.com/link-it/govpay-ec-client/releases/tag/2.0.0).

## Novita' della 2.0.1

- **Parent POM `govpay-bom` allineato alla 2.0.1** (era 2.0.0). Le versioni delle librerie principali (Spring Boot 4.1.0, Spring Framework 7.0.8, Jackson 3.1.4, OWASP Dependency-Check 12.2.2) restano invariate rispetto alla 2.0.0.

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
- Parent POM `org.gov4j.govpay:govpay-bom:2.0.1`
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
    <version>2.0.1</version>
</dependency>
```

## Licenza

GNU GPL v3 — Copyright (c) 2014-2026 Link.it srl
