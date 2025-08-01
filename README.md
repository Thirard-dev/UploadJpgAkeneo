## Présentation

Le projet a pour but de déposer dans Akeneo les assets qui sont au format PDF en JPG.

## Flux du projet

⚠️ En cours ⚠️

## env

Un template est disponible à la racine du projet : [.env.template](./.env.template)

### Connexion

Pour récupérer les informations de connexion à l'API Akeneo, 
suivre la documentation Akeneo : [authentification Akeneo Pim](https://api.akeneo.com/documentation/authentication.html)

- `URL_AUTH` : Url pour la récupération des token de connexion à l'API
- `AUTH_AUTHORIZATION` : Clé d'authentification pour la récupération des tokens (format Basic + clé)
- `USER` : Nom d'utilisateur de connexion
- `PASSWORD` : Mot de passe de l'utilisateur

### Requêtage
- `BASE_URL` : Base de l'url pour les requêtes

### Exécution
- `CONFIG_FILE_PATH` : Chemin du fichier de configuration pour stocké l'état de l'exécution
- `TEMP_FOLDER` : Chemin du dossier qui stock les pdf et jpg temporairement

