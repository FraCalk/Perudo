# Perudo

App Android in Kotlin e Jetpack Compose per usare i propri dadi durante una partita di Perudo. All'avvio crea sei dadi casuali e imposta a 30 il contatore manuale dei dadi complessivi. Permette di aggiungere dadi, rimuoverli al tocco, lanciarli, leggere il riepilogo e alternare tema chiaro e scuro.

## Come si collegano le parti

1. Android avvia MainActivity, dichiarata nel manifest.
2. L'Activity crea la UI Compose e conserva la scelta del tema con rememberSaveable.
3. PerudoTheme fornisce colori e tipografia ai componenti.
4. DiceScreen ottiene il DiceViewModel, osserva i suoi StateFlow e invia le azioni dell'utente.
5. Il ViewModel aggiorna la lista dei dadi e ricava il riepilogo.
6. DiceView disegna ogni dado e anima il lancio; DiceSummaryView mostra le statistiche.

Il ViewModel sopravvive alla ricreazione dell'Activity, ma i suoi dati non vengono salvati su disco. Un nuovo processo ricrea dadi e contatore iniziali. La scelta del tema usa lo stato salvabile di Android, non una preferenza permanente.

## Codice applicativo

I percorsi seguenti sono relativi ad app/src/main/java/com/example/perudo/.

| File | Responsabilità |
| --- | --- |
| MainActivity.kt | Punto di ingresso, contenuto Compose, stato del tema e callback per cambiarlo. |
| DiceViewModel.kt | Modelli dati, lista dei dadi, contatore di partita, lancio casuale e conteggi con Jolly. |
| ui/screen/DiceScreen.kt | Barra superiore, griglia, pulsanti, dialogo del totale e regolamento. |
| ui/components/MultiplayerMenu.kt | Menu a tendina con le azioni Crea una Partita ed Entra in una partita. |
| ui/components/DiceView.kt | Animazioni, geometria del cubo, proiezione 3D sul Canvas, ombra e scie. |
| ui/components/DiceSummaryView.kt | Conteggio Jolly, etichette delle combinazioni e miniature dei conteggi naturali. |
| ui/theme/Color.kt | Palette chiara e scura di riserva. |
| ui/theme/Theme.kt | Scelta della palette e applicazione di MaterialTheme; usa colori dinamici per default. |
| ui/theme/Type.kt | Personalizzazione dello stile bodyLarge; gli altri stili restano quelli Material. |

### Stato e conteggi

- DieItem identifica un dado con un ID stabile e un valore da 1 a 6.
- CombinationInfo descrive una combinazione, distinguendo naturali e Jolly.
- ValueCountInfo contiene il conteggio di una faccia, con e senza Jolly.
- DiceSummary raccoglie Jolly, combinazioni e conteggi delle facce da 2 a 6.

I flussi mutabili sono privati; la schermata legge le versioni pubbliche in sola lettura. diceSummary deriva dalla lista con map e viene condiviso con stateIn: resta attivo fino a cinque secondi dopo la disconnessione dell'ultimo osservatore.

Per ogni faccia da 2 a 6, il totale comprende i dadi naturali e tutti gli 1. Una combinazione viene elencata solo se contiene almeno un naturale e almeno due dadi complessivi. Per esempio, [1, 1, 5] produce un Tris da 5; [1, 1] non produce etichette di combinazione e la UI mostra un messaggio sui Jolly. I riquadri inferiori riportano soltanto i naturali.

Il totale di partita è un contatore manuale: non genera dadi avversari e non entra nel riepilogo. Aggiungere o rimuovere un dado proprio modifica anche questo totale; i comandi nella barra superiore modificano solo il totale. Il minimo del contatore è 1, anche quando non rimangono dadi propri.

### Lancio e disegno

rollDice() decide subito nuovi valori casuali con Random.nextInt(1, 7) e blocca il lancio per 480 ms. Il riepilogo può quindi cambiare prima che finisca l'animazione.

DiceView esegue in parallelo salto, scala e rotazioni mediante Animatable e coroutine di LaunchedEffect. L'indice nella griglia alterna il verso di rotazione, senza ritardare la partenza. Alla fine del blocco di 480 ms l'effetto precedente viene cancellato e i parametri vengono ripristinati.

A riposo viene disegnata una faccia piatta arrotondata. Durante il movimento:

1. Si costruiscono gli otto vertici e le sei facce del cubo.
2. Punti e vettori vengono ruotati attorno agli assi X, Y e Z.
3. Si proiettano le coordinate 3D sul Canvas 2D con una scala dipendente dalla profondità.
4. Si selezionano le facce secondo la normale z e si ordinano per profondità.
5. Si disegnano superfici arrotondate, bordi e pallini, con illuminazione simulata.
6. Tre copie trasparenti e scie sui pallini simulano il movimento; l'ombra cambia con l'altezza.

Sono calcoli di disegno su Canvas, senza un motore fisico o una scena 3D esterna.

### Schermata e dialoghi

La barra superiore mostra il menu Multiplayer a sinistra e i pulsanti tema e regolamento a destra. Il contatore dei dadi occupa una seconda riga. Le due voci multiplayer chiudono il menu e aprono un avviso: creazione e accesso alle partite non sono ancora collegati a sessioni di rete.

La griglia sceglie da una a quattro colonne in base a numero di dadi e larghezza disponibile. Le dimensioni dei dadi sono prefissate per ciascun numero di colonne. Gli ID stabili mantengono l'identità dei componenti nelle modifiche alla lista.

Durante il lancio la schermata impedisce aggiunte, rimozioni e nuovi lanci. Il contatore manuale di partita resta modificabile.

Nel dialogo del totale ogni testo convertibile in intero aggiorna subito il ViewModel, che applica il minimo di 1. Testo vuoto, non numerico o fuori dall'intervallo di Int lascia il valore precedente. Conferma chiude il dialogo; la chiusura non annulla modifiche già applicate.

Il regolamento descrive dichiarazioni, Dubito, Calza e Palafico. Queste azioni non sono implementate: il riepilogo tratta sempre gli 1 come Jolly.

## Configurazione e risorse

| Percorso | Funzione |
| --- | --- |
| settings.gradle.kts | Nome del progetto, modulo :app, repository e resolver delle toolchain. |
| build.gradle.kts | Plugin condivisi, dichiarati senza applicarli alla radice. |
| app/build.gradle.kts | Configurazione Android, Compose e dipendenze: minSdk 33, compileSdk/targetSdk 37, compatibilità Java 11. |
| gradle/libs.versions.toml | Catalogo di versioni e alias di librerie/plugin; BOM per allineare Compose. |
| gradle.properties | Memoria del daemon, UTF-8, cache di configurazione e percorso locale del JDK. |
| gradle/gradle-daemon-jvm.properties | Criteri generati per il daemon JDK 21 e indirizzi delle toolchain per piattaforma. |
| gradlew, gradlew.bat, gradle/wrapper/* | Wrapper generato che avvia la versione Gradle prevista dal progetto e ne verifica la distribuzione. |
| local.properties | Configurazione locale della macchina, per esempio percorso SDK Android; non contiene logica applicativa. |
| app/src/main/AndroidManifest.xml | Activity di avvio, metadati, tema della finestra e impostazioni di backup. |
| app/src/main/res/values/strings.xml | Nome dell'app; gli altri testi UI sono attualmente scritti nei composable. |
| app/src/main/res/values/themes.xml | Tema Android senza ActionBar, distinto dal tema Compose. |
| app/src/main/res/values/colors.xml | Palette XML del template; il tema Compose usa le proprie palette. |
| app/src/main/res/drawable/* | Vettori di sfondo e primo piano dell'icona. |
| app/src/main/res/mipmap-*/* | Icone adattive e immagini del launcher per le diverse densità. |
| app/src/main/res/xml/* | Regole di backup del template senza filtri personalizzati attivi; non salvano lo stato del ViewModel. |
| app/src/main/keepRules/rules.keep | Esempi commentati di regole R8; nessuna regola personalizzata attiva. |

I file generati del wrapper, le immagini e le configurazioni locali sono descritti qui. I commenti nel sorgente spiegano funzioni e blocchi logici; import e istruzioni elementari restano leggibili senza annotazioni ripetitive.

## Test e verifiche

app/src/test/.../ExampleUnitTest.kt contiene soltanto un test dimostrativo di addizione sulla JVM. app/src/androidTest/.../ExampleInstrumentedTest.kt verifica il package su dispositivo/emulatore. Non coprono la logica dei dadi o l'interfaccia.

Da PowerShell, con JDK e SDK configurati:

~~~powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
.\gradlew.bat connectedDebugAndroidTest
~~~

L'ultimo comando richiede un dispositivo o emulatore collegato. Questi comandi sono istruzioni di utilizzo, non attestazioni di esecuzione.

## Comportamenti da considerare

- Il totale manuale può essere inferiore ai dadi propri: manca un vincolo tra i due.
- removeDie(id) decrementa il totale anche per un ID assente; la UI passa ID esistenti.
- addDie() non controlla isRolling internamente; il blocco è nella schermata.
- Il termine del lancio dipende da 480 ms fissi, senza attendere la conclusione delle molle.
- Non sono presenti salvataggio della partita, gestione dei giocatori o sincronizzazione di rete.

La documentazione descrive il comportamento attuale e non modifica queste scelte.

