# Vaglia — Approval Gateway

Piattaforma di **human-in-the-loop approval** per automazioni AI che agiscono su dati sensibili (email, pagamenti, dati cliente). Un'automazione propone un'azione, un dato sensibile viene mascherato e mostrato in una dashboard, e l'azione viene eseguita solo dopo l'approvazione esplicita di una persona.

Il caso d'uso: sempre più processi aziendali vengono automatizzati con l'AI, ma affidare all'AI l'esecuzione diretta di azioni su dati sensibili (inviare un'email a nome dell'azienda, confermare un pagamento) è rischioso. Questo progetto implementa il pattern **"AI propone, umano dispone"**: ogni azione sensibile passa da una coda di revisione prima di essere eseguita.

## Stack

| Layer | Tecnologia |
|---|---|
| Backend | Java 21, Spring Boot 4, Spring Data JPA |
| Frontend | Angular 17 (standalone components) |
| Database | H2 file-based di default (zero setup) — profilo MySQL pronto per produzione |
| AI | Google Gemini API, tier gratuito (con fallback a mock se non configurata) |

## Come funziona

1. Un'automazione (es. "bozza di risposta email a un cliente") invia i dati grezzi al backend
2. Il backend genera una bozza dell'azione tramite l'API di Gemini e **maschera il dato sensibile** (es. l'email del cliente) prima di salvarlo
3. La richiesta finisce in coda con stato `PENDING`, visibile nella dashboard Angular
4. Un revisore umano apre il dettaglio, vede il dato mascherato e la proposta dell'AI, e **approva o rifiuta**, con una nota facoltativa
5. Solo dopo l'approvazione l'azione verrebbe effettivamente eseguita (in questo MVP il punto di esecuzione è isolato in un unico metodo — `ApprovalService.approve()` — pronto per essere collegato a un servizio email/pagamenti reale)

## Avvio rapido (nessuna configurazione richiesta)

**Backend** (porta 8080, database H2 su file, dati demo pre-caricati):

```bash
cd backend
./mvnw spring-boot:run
```

**Frontend** (porta 4200):

```bash
cd frontend
npm install
npm start
```

Apri `http://localhost:4200`.

### Abilitare la generazione AI reale

Senza configurazione, le bozze sono generate da un mock chiaramente etichettato (`[MOCK — set GEMINI_API_KEY...]`), utile per demo/CI senza credenziali. Per usare Gemini davvero:

1. Crea una API key gratuita su [aistudio.google.com/apikey](https://aistudio.google.com/apikey) (nessuna carta di credito richiesta, tier gratuito con rate limit)
2. Impostala come variabile d'ambiente prima di avviare il backend:

```bash
export GEMINI_API_KEY=AIza...   # Windows PowerShell: $env:GEMINI_API_KEY="AIza..."
```

### Passare a MySQL

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

Richiede un'istanza MySQL locale (vedi `application-mysql.properties` per la configurazione).

## Decisioni di design

- **Mascheramento dei dati sensibili prima della persistenza**: il valore completo (es. l'email) viene usato solo in memoria per costruire il prompt AI; quello che finisce a database e in UI è già mascherato (`SensitiveDataMasker`).
- **Fallback mock per l'AI**: se `GEMINI_API_KEY` non è configurata, l'app resta comunque completamente funzionante e dimostrabile — scelta pensata per demo, colloqui tecnici e CI senza segreti.
- **H2 di default, MySQL come profilo**: stesso schema JPA, zero setup per chi clona il repo, ma pronto per un database reale in produzione.

## Roadmap (non ancora implementato)

- [ ] Autenticazione multi-utente (JWT)
- [ ] Seconda automazione demo (es. conferma pagamento)
- [ ] Storico/audit trail filtrabile
- [ ] Test JUnit sul flusso di approvazione
- [ ] Docker Compose (backend + frontend + MySQL)
- [ ] CI con GitHub Actions
- [ ] Notifica opzionale via Telegram con link diretto alla richiesta

## Perché questo progetto

Sviluppato per mettere in pratica in un unico stack (Java, Spring Boot, Angular, MySQL) l'esperienza maturata in un tirocinio su automazioni digitali e AI. Il codice è stato scritto con l'assistenza di Claude Code, a partire da scelte architetturali definite consapevolmente: mascheramento dati, fallback mock, separazione tra proposta e esecuzione dell'azione.
