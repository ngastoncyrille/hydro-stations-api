# API de données de stations hydrométriques (Java, Spring Boot)

Petite API REST qui reçoit les mesures des stations hydrométriques télétransmises et les stocke sans doublon. Elle les restitue par période et qualifie l'état de chaque station : niveau d'alerte, batterie, silence de transmission.

Auteur : NEBIE Gaston Cyrille, ingénieur hydrologue. Projet de démonstration, novembre 2026.

## Origine

En 2025, à l'Agence de l'Eau du Nakanbé, l'auteur a construit sous R une chaîne de suivi de la station télétransmise de Loumbila. Elle télécharge par FTP les nouveaux fichiers de l'enregistreur, les charge dans une base SQLite, puis alimente un tableau de bord Shiny publié en ligne. Cette API reprend le même modèle de données en Java : station, date de mesure, paramètre (hauteur, batterie), valeur, unité, code qualité. Elle ajoute ce qu'une chaîne opérationnelle demande en plus de l'affichage :
- une réception automatique des lots de mesures ;
- un dédoublonnage, car les téléchargements successifs d'un enregistreur se recouvrent ;
- un état d'alerte calculé côté serveur.

## Points d'accès

| Méthode | URL | Rôle |
|---|---|---|
| GET | `/api/stations` | Liste des stations |
| POST | `/api/stations` | Création d'une station (code, seuils de vigilance et d'alerte, tension minimale de batterie) |
| GET | `/api/stations/{code}` | Fiche d'une station |
| POST | `/api/stations/{code}/mesures` | Réception d'un lot de mesures. Les doublons sont ignorés et comptés |
| GET | `/api/stations/{code}/mesures?parametre=hauteur&debut=…&fin=…` | Série d'un paramètre sur une période |
| GET | `/api/stations/{code}/etat` | Dernière hauteur, niveau d'alerte (normal, vigilance, alerte), batterie faible, station silencieuse |

Les erreurs sont renvoyées au format standard RFC 9457 :
- 404 pour une station inconnue ;
- 409 pour un code de station déjà pris ;
- 400 pour des données invalides.

## Exemple

```bash
curl -X POST http://localhost:8080/api/stations/LOUMBILA/mesures \
  -H "Content-Type: application/json" \
  -d '[{"horodatage":"2025-08-06T10:00:00Z","parametre":"hauteur","valeur":320,"unite":"cm","qualite":"G"},
       {"horodatage":"2025-08-06T10:00:00Z","parametre":"Batt","valeur":11.2,"unite":"V","qualite":"G"}]'

curl http://localhost:8080/api/stations/LOUMBILA/etat
```

Les libellés des enregistreurs sont harmonisés : `Batt` devient `batterie`.

## Lancer

Java 21 est requis. Maven est fourni par le wrapper.

```bash
./mvnw test             # tests unitaires et tests de l'API
./mvnw spring-boot:run  # démarre l'API sur le port 8080
```

La base H2 est en mémoire. La station LOUMBILA y est chargée au démarrage avec des **seuils fictifs**, à ne pas utiliser en opérationnel.

## Architecture

- `model` : entités JPA `Station` et `Mesure`. Une contrainte d'unicité porte sur station, horodatage et paramètre.
- `repository` : accès aux données (Spring Data JPA).
- `service` : règles métier. `EvaluateurAlerte` est sans dépendance et testé isolément.
- `web` : contrôleur REST, objets d'échange, gestion des erreurs.

## Suites possibles

- Import direct des fichiers des enregistreurs (CSV) et de la base SQLite existante.
- Passage à PostgreSQL et TimescaleDB pour les longues séries.
- Authentification des stations émettrices.
- Conversion hauteur-débit par courbe de tarage.
- Envoi des états d'alerte vers un modèle de prévision.
