–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––

PROJECTE DE SEGURETAT I CRIPTOGRAFIA EN JAVA  
Mòdul: Programació de serveis i processos  
UF1: Seguretat i criptografia  
Pràctica: P1

–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––

DESCRIPCIÓ GENERAL
------------------  
Aquest projecte té com a objectiu demostrar diversos conceptes criptogràfics utilitzant el llenguatge de programació Java. S'implementen algorismes simètrics i asimètrics, la generació i validació de firmes digitals i sistemes híbrids de xifrat. El projecte està dissenyat per ser modular, de manera que cada activitat és independent i s'executa des d'un menú interactiu presentat per la classe principal.

Els conceptes principals inclouen:  
•	Xifrat simètric (AES): Generació de claus AES de 128 bits, xifrat i descifrat de text i fitxers.  
•	Xifrat asimètric (RSA): Generació de parells de claus RSA (clau pública i privada), envolvat (wrap) de claus i firma digital.  
•	Sistemes híbrids: Combinació d’AES i RSA per protegir claus simètriques i xifrar missatges.  
•	Gestió de fitxers i logs: Cada activitat desa els seus resultats en carpetes específiques, i part del procés queda registrat per facilitar-ne la revisió.

Tota la interfície i els missatges de l'aplicació estan en català per mantenir una coherència amb la llengua de la pràctica.

–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––

ESTRUCTURA DEL PROJECTE
------------------------  
El projecte està organitzat de la següent manera:
```
M09_P1_Villalba_MarcoS/  
├── src/  
│   └── main/  
│       └── java/  
│           ├── activitat_1/  
│           │   └── Activitat1.java  
│           ├── activitat_2/  
│           │   └── Activitat2.java  
│           ├── activitat_3/  
│           │   └── Activitat3.java  
│           ├── activitat_4/  
│           │   └── Activitat4.java  
│           ├── activitat_5/  
│           │   └── Activitat5.java  
│           ├── activitat_6/  
│           │   └── Activitat6.java  
│           ├── Functions/  
│           │   └── Utilities.java  
│           └── MainApp.java  
└── pom.xml
```
- **Functions/Utilities.java:**  
  Aquesta classe conté mètodes i constants útils per a tota l'aplicació: gestió de rutes, creació de carpetes, impressió de missatges estilitzats (errors, advertències, èxit) i creació de menús interactius.

- **activitat_x:**  
  Cada carpeta (activitat_1 fins a activitat_6) conté la classe corresponent, on s'implementa un procés criptogràfic específic (per exemple, generació de claus, xifrat/descifrat de text o fitxers, firma digital o sistema de missatgeria).

- **MainApp.java:**  
  Aquesta és la classe principal que mostra un menú interactiu a l'usuari per executar les diferents activitats.

–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––

DESCRIPCIÓ DE LES ACTIVITATS
----------------------------

**Activitat 1: Gestió de Claus AES per a Usuaris**  
Objectiu:
- Generar una clau AES única (de 128 bits) per a cada usuari d'una llista predeterminada: "Anna", "Joan", "Laura" i "Pau".
- Mostrar el nom de cada usuari amb la seva clau en format hexadecimal.

Funcionalitats:
- Generació automàtica de la clau AES per a cada usuari.
- Possibilitat de regenerar la clau d’un usuari específic.
- Visualització d’una taula amb els noms i claus dels usuaris.
- Opcions per desar les claus en un fitxer CSV i carregar-les.

**Activitat 2: Cifrat i Desxifrat de Text amb AES**  
Objectiu:
- Derivar una clau AES a partir de la contrasenya compartida "projecte2025", afegint un salt aleatori, mitjançant PBKDF2 amb HMAC-SHA256.
- Xifrar el text "Document confidencial" utilitzant AES en mode CBC i posteriorment descifrar-lo per verificar que el text recuperat coincideix amb l’original.

Funcionalitats:
- Derivació segura de la clau AES afegint un salt i utilitzant iteracions per reforçar la seguretat.
- Xifrat i descifrat amb AES/CBC/PKCS5Padding.
- Desa dels resultats (text xifrat, vector IV i salt) en un fitxer CSV.

**Activitat 3: Cifrat i Desxifrat de Fitxers amb AES**  
Objectiu:
- Xifrar el contingut d’un fitxer de text (entrada.txt) utilitzant AES en mode CBC i després recuperar el contingut original mitjançant el descifrat.

Funcionalitats:
- Creació automàtica del fitxer `entrada.txt` amb contingut predefinit si aquest no existeix.
- Xifrat del fitxer amb AES/CBC, utilitzant un vector IV aleatori.
- Desa en fitxers separats el fitxer xifrat (`sortida.enc`), la clau AES i el IV (amb codificació Base64).
- Descifrat i visualització del contingut original per consola.

**Activitat 4: Cifrat i Desxifrat de Fitxers amb AES i RSA**  
Objectiu:
- Combinar AES i RSA per xifrar i desxifrar un fitxer de text (entrada.txt).
- Es genera (o carrega) un parell de claus RSA, s'utilitza una clau AES per xifrar el fitxer, la clau AES es xifra amb RSA (envoltant-la) i es desa, i finalment es descifra el fitxer recuperant la clau AES mitjançant la clau privada RSA.

Funcionalitats:
- Gestió de claus RSA (generació i càrrega).
- Xifrat del fitxer amb AES en mode CBC.
- Envoltat (wrap) de la clau AES amb RSA i desat dels diferents elements en fitxers separats.
- Descifrat i recuperació del contingut original.

**Activitat 5: Firma Digital de Documents amb RSA**  
Objectiu:
- Llegir el contingut d’un fitxer de text (`document.txt`).
- Generar una firma digital del contingut mitjançant la clau privada RSA.
- Verificar que la signatura és correcta amb la clau pública RSA.

Funcionalitats:
- Generació d’un parell de claus RSA per al procés de signatura.
- Signatura digital del fitxer i desa de la signatura en format Base64 en un fitxer (`firmaDigital.txt`).
- Verificació de la signatura per assegurar la integritat del contingut.

**Activitat 6: Sistema de Missatgeria Híbrid (RSA + AES)**  
Objectiu:
- Implementar un sistema de missatgeria en el qual es generin dos parells de claus RSA (un per a l’emissor i un per al receptor).
- L’emissor introdueix un missatge, el xifra amb la clau pública del receptor i el signa digitalment amb la seva clau privada.
- El receptor desxifra el missatge amb la seva clau privada i verifica la signatura amb la clau pública de l’emissor.

Funcionalitats:
- Generació de parells de claus RSA per a l’emissor i el receptor.
- Entrada interactiva del missatge per part de l’emissor.
- Xifrat del missatge amb RSA utilitzant la clau pública del receptor.
- Generació d'una signatura digital amb la clau privada de l’emissor i verificació amb la seva clau pública.
- Mètodes opcionals per xifrar amb AES (envoltant la clau AES amb RSA) per demostrar un procés híbrid.
- Tota la informació rellevant del procés es registra en un fitxer de log (process_log.txt) que es desa dins de la carpeta activitat6.

–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––

NOTES I CONSIDERACIONS
----------------------  
- **Organització del Projecte:**  
  Cada activitat desa els seus fitxers (com ara claus, fitxers xifrats, signatura digital, etc.) dins d'una carpeta específica dins del directori `output/`. Això permet tenir una organització clara i facilita la revisió de cada procés criptogràfic.

- **Interfície d'Usuari:**  
  El menú interactiu i tots els missatges es mostren en català. La classe **Utilities** s'utilitza per imprimir missatges estilitzats (amb colors i formats) per millorar la presentació i l'experiència d'usuari.

- **Gestió de Fitxers i Registre:**  
  Tota la sortida rellevant del procés (especialment en l’Activitat 6) es desa en fitxers dins de la carpeta corresponent. Per exemple, el fitxer `process_log.txt` es desa dins de `output/activitat6/` i conté informació neta sobre el procés amb timestamps.

- **Seguretat:**  
  Tot i que aquest projecte és didàctic, les tècniques utilitzades (generació de claus, xifrat/descifrat, firma digital) són fonamentals per protegir dades sensibles. En entorns de producció, s'haurien de considerar mesures de seguretat addicionals per a la gestió i emmagatzematge de claus.

- **Modularitat i Mantenibilitat:**  
  Cada activitat està encapsulada en la seva pròpia classe i la classe **MainApp** gestiona la selecció i execució de les activitats. Això facilita l'extensió i la modificació del projecte.

- **Documentació Interna:**  
  El codi està àmpliament comentat per explicar la finalitat de cada mètode i lògica de treball, facilitant la comprensió per part d'estudiants i desenvolupadors nous.

–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––

AUTOR
------  
Marcos
