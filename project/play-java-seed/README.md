faire un .env avec les bonne valeur // TODO pour l'instant (# Database Configuration
POSTGRES_USER=battler_user
POSTGRES_PASSWORD=battler_password
POSTGRES_DB=battler_db
POSTGRES_PORT=5432


APPLICATION_SECRET=OT6sSfOkw7slrF+CyMEX9aleP7K/HXTsIkvNg8mnMX0=
)


# 1. rebuild
docker compose build

# 2. run
docker compose up

# ou tout en un
docker compose up --build