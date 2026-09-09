# Travail de Bachelor — Auto-Battler

Backend d'un jeu de type auto-battler développé avec [Play Framework](https://www.playframework.com/) (Java), utilisant PostgreSQL comme base de données. L'ensemble de l'environnement (backend + base de données) est conteneurisé avec Docker.

## Stack technique

- **Backend** : Play Framework (Java), Hibernate/JPA, Apache Pekko (acteurs)
- **Base de données** : PostgreSQL
- **Build tool** : sbt
- **Conteneurisation** : Docker / Docker Compose

## Prérequis

Avant de lancer le projet, assurez-vous d'avoir installé :

- [Docker](https://docs.docker.com/get-docker/) et [Docker Compose](https://docs.docker.com/compose/install/)
- [sbt](https://www.scala-sbt.org/download.html) (uniquement nécessaire pour le développement local hors conteneur, voir [Développement](#développement))

## Configuration

Avant le premier lancement, copiez le fichier d'exemple de configuration et adaptez-le si nécessaire :

```bash
cp .env.example .env
```

Variables principales à connaître (définies dans `.env` ou `docker-compose.yml`) :

| Variable            | Description                          | Valeur par défaut |
|---------------------|---------------------------------------|--------------------|
| `POSTGRES_DB`       | Nom de la base de données             | `auto_battler`     |
| `POSTGRES_USER`     | Utilisateur PostgreSQL                | `postgres`         |
| `POSTGRES_PASSWORD` | Mot de passe PostgreSQL               | `postgres`         |
| `POSTGRES_PORT`     | Port exposé pour PostgreSQL           | `5432`             |
| `CURRENT_VERSION`   | La version de l'application           | `0.1`              |
|`APPLICATION_SECRET` | le secret de l'application Play       | en généré un ou juste prendre `OT6sSfOkw7slrF+CyMEX9aleP7K/HXTsIkvNg8mnMX0=`|

## Lancement

Pour démarrer l'ensemble des services (backend + base de données) :

```bash
docker compose up
```

Pour lancer en arrière-plan (mode détaché) :

```bash
docker compose up -d
```

Le backend est alors accessible sur [http://localhost:9000](http://localhost:9000).

Pour arrêter les services :

```bash
docker compose down
```

Pour arrêter les services **et supprimer les volumes** (réinitialise complètement la base de données) :

```bash
docker compose down -v
```

## Logs

Pour suivre les logs en temps réel :

```bash
docker compose logs -f
```



## Base de données

Le schéma est initialisé automatiquement au démarrage du conteneur PostgreSQL via les scripts présents dans `conf/evolutions/` (ou `db/init/` selon votre organisation).


### Tests

```bash
sbt test
```

## Auteur·e·s

Projet réalisé dans le cadre d'un travail de bachelor par Nicolas Duprat.