# DTicket

**Système de tickets de support in-game.** Un joueur ouvre un ticket depuis le
jeu (`/dticket <catégorie> <message>`), le staff le consulte et y répond via
un GUI avec la tête du joueur concerné, le joueur voit la réponse via `/ticket`.
Notifications Discord en option, stockage local ou MySQL, et intégration DAPI
dans les deux sens.

## Fonctionnalités

- **`/dticket <catégorie> <message>`** : ouvre un ticket. 5 catégories :
  `cheat` (triche), `bug`, `question`, `suggestion`, `report` (signaler un
  joueur pour comportement toxique/scam, différent de `cheat`).
- **`/ticket`** : ouvre un GUI listant tes propres tickets (icône selon la
  catégorie, couleur selon le statut), avec ton message et la réponse d'un
  admin si elle existe. Cliquer sur un ticket non fermé le ferme (tu peux
  aussi fermer via un admin).
- **`/dticket admin`** *(permission `dticket.admin`)* : GUI listant tous les
  tickets de tous les joueurs, chaque ticket représenté par **la tête du
  joueur concerné**. Cliquer sur un ticket affiche son détail dans le chat de
  l'admin (catégorie, message, statut, réponse actuelle) avec les commandes
  prêtes à copier pour répondre ou fermer.
- **`/dticket reply <id> <message>`** *(admin)* : répond à un ticket, le
  joueur est notifié en jeu s'il est connecté.
- **`/dticket close <id>`** : ferme un ticket - un admin peut fermer
  n'importe lequel, un joueur seulement les siens.
- **Anti-spam** : un joueur ne peut pas avoir plus de X tickets non fermés en
  même temps (`limits.max-open-tickets-per-player`, 3 par défaut).
- **Stockage au choix** : fichiers YAML locaux par défaut, ou base MySQL,
  comme DFaction (`config.yml`).
- **Webhook Discord** : notifie un salon Discord à la création, la réponse et
  la fermeture d'un ticket (URL de webhook dans `config.yml`, vide =
  désactivé). Pas de dépendance externe : utilise le client HTTP intégré au
  JDK.

## Intégration DAPI

DTicket fonctionne très bien seul, mais s'enrichit si [DAPI](../DAPI) (et
éventuellement [DFaction](../DFaction)) sont installés (`softdepend`, tout le
code est isolé pour ne jamais planter si DAPI est absent) :

- **`TicketService` partagé** : DTicket publie la création de tickets et le
  comptage de tickets ouverts auprès de DAPI. Idée d'usage : un futur
  anti-cheat pourrait ouvrir un ticket "cheat" automatiquement dès qu'il
  détecte un comportement suspect, sans connaître le fonctionnement interne
  de DTicket.
- **Contexte de faction** : dans le GUI admin, si `FactionService` (DFaction)
  est disponible, le détail d'un ticket affiche la faction du joueur
  concerné (`integration.show-faction-in-admin-gui` dans `config.yml`).

### Autres idées d'interconnexion possibles

- Un futur `DLogs` pourrait s'abonner aux tickets créés pour un tableau de
  bord centralisé (bugs vs cheats vs suggestions dans le temps).
- Un futur `DEconomy`/`DShop` pourrait ouvrir un ticket "report" automatique
  en cas de litige détecté sur une transaction.
- `/dapi list` affichera DTicket dès qu'il est actif, avec le service
  `TicketService` qu'il fournit.

## Commandes

| Commande | Description |
|---|---|
| `/dticket <cheat\|bug\|question\|suggestion\|report> <message>` | Ouvrir un ticket. |
| `/dticket admin` | GUI de tous les tickets (têtes de joueurs). *(admin)* |
| `/dticket reply <id> <message>` | Répondre à un ticket. *(admin)* |
| `/dticket close <id>` | Fermer un ticket (le sien, ou n'importe lequel en admin). |
| `/ticket` | GUI de tes propres tickets. |

## Permissions

| Permission | Défaut | Description |
|---|---|---|
| `dticket.use` | `true` | Créer et consulter ses propres tickets. |
| `dticket.admin` | `op` | GUI admin, répondre, fermer les tickets des autres. |

## Configuration (`config.yml`)

```yaml
storage:
  type: local   # local ou mysql
  mysql:
    host: localhost
    port: 3306
    database: dticket
    username: root
    password: ""

limits:
  max-open-tickets-per-player: 3

discord:
  webhook-url: ""
  notify-on-create: true
  notify-on-reply: true
  notify-on-close: true

integration:
  show-faction-in-admin-gui: true
```

## Compiler le projet

Dépend de l'API Spigot 26.1.2 et, en `provided`, de DAPI :

```
cd ../DAPI && mvn install
cd ../DTicket && mvn clean package
```

Voir [`libs/README.md`](libs/README.md) pour la mise en place de l'API
Spigot (une seule fois par machine).

## Roadmap / idées d'extension

- Pagination du GUI admin au-delà de 54 tickets affichables.
- Filtrer le GUI admin par catégorie ou par statut.
- Historique des réponses multiples sur un même ticket (actuellement une
  seule réponse par ticket, la dernière écrase la précédente).
- Notifications Discord avec un vrai embed riche (auteur, timestamp, footer).

## Licence

MIT — voir [`LICENSE`](LICENSE).
