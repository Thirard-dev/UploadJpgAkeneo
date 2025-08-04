## Présentation

Le projet a pour but de déposer dans Akeneo les assets qui sont au format PDF en JPG.

## Flux du projet

> 🚧 En cours 🚧

## Contenu du fichier .env

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

### Logger
- `LOG4J_CONFIG_FILE` : Chemin vers le fichier de configuration log4j
- `URL_GRAYLOG` : Url du graylog interne

## Mise en production

### Téléchargement

Télécharger le fichier exécutable .jar disponible sur github : [dernière version](https://github.com/Thirard-dev/UploadJpgAkeneo/packages/2602571).

> ⚠️ Attention à bien prendre le fichier avec le suffixe jar-with-dependencies, sinon il ne fonctionnera pas
>
> <img width="220" height="476" alt="image" src="https://github.com/user-attachments/assets/4e8ef7d4-308d-4196-b679-9c00d5e8c2ec" />

### Installation

Déposer le fichier dans un dossier.

Créer un fichier .bat à côté et copié-collé le contenu suivant : `java -jar upload-jpg-akeneo-1.0.0-jar-with-dependencies.jar`

> 🚨 Pour que l'exécutable fonctionne, n'oubliez pas de le fichier .env.prod et fichier config.yml
