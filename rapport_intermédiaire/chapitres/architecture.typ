= Architecture Logiciel <architecture>

Après avoir analyser les différentes solutions qui nous étaient a disposition on va maintenant vous présentez l’architecture logiciel choisit ainsi que une brève présentation de ces technologie. 
#v(2em)
#v(2em)

 

== Frontend

Le frontend soit l’interface client va être développer en JavaScript notamment avec l’aide de la librairie Pixie.JS 
#v(2em)
#v(2em)

=== PixiJS

PixiJS est un moteur de rendu 2D open source permettant de créer des visuels de haute qualité sur le web à l’aide de JavaScript. Ce moteur est construit sur WebGL et, de manière optionnelle, sur WebGPU, ce qui lui permet d’offrir des performances élevées et d’exécuter sans difficulté des scènes contenant de nombreux sprites. 
#v(2em)

Dans le cadre de mon projet, PixiJS gère l’affichage de nombreux Sprites comme tous les personnages ou encore l’interface utilisateur, mais aussi toutes les animations de combats. Il permet ainsi de supporter un grand nombre d’éléments graphiques simultanés sans dégradation significative des performances. 
#v(2em)
#v(2em)

== Backend

Le backend va être en Java, il va exécuter toute la logique métier de l’application donc, faire des authentifications, gérer les connexions avec les utilisateurs, gérer les parties, simuler les combats, etc… 
#v(2em)
#v(2em)

=== Play

Le Play Framework est un framework web open source conçu pour le développement d’applications web en Java et Scala. Il suit une architecture moderne basée sur le modèle MVC (Model-View-Controller), ce qui permet de structurer clairement le code et de faciliter la maintenance des applications. 
#v(2em)

Play se distingue par son approche réactive et non bloquante, ce qui lui permet de gérer efficacement un grand nombre de requêtes simultanées. Il est également fortement intégré à l’écosystème Java, ce qui le rend compatible avec de nombreuses bibliothèques et outils utilisés en entreprise. 
#v(2em)

Dans le cadre de ce projet, Play Framework est utilisé pour implémenter la partie backend de l’application, notamment la gestion des connexions avec le pattern acteur et pekko stream, tout cela sans soucis de concurrence. 
#v(2em)
#v(2em)



=== 0auth2/OpenID connect

OAuth 2.0 est un protocole d’autorisation largement utilisé qui permet à une application d’accéder à des ressources protégées au nom d’un utilisateur, sans avoir besoin de connaître ses identifiants. Il repose sur l’émission de tokens d’accès, qui définissent des permissions limitées et temporaires. 
#v(2em)

OpenID Connect est une couche d’authentification construite au-dessus d’OAuth 2.0. Elle permet de vérifier l’identité d’un utilisateur et de récupérer des informations de profil de manière standardisée, via un mécanisme basé sur des tokens d’identité (ID Token). 
#v(2em)

Dans le cadre de ce projet, OAuth 2.0 et OpenID Connect sont utilisés pour sécuriser l’authentification des utilisateurs et centraliser la gestion des accès en faisant appel à un gestionnaire tiers (google, github). Cela, permet de ne pas se préoccuper de toute la partie sécurité, stockage d’empreinte de mot de passe et identification. 
#v(2em)
#v(2em)


=== Hibernate

Hibernate est un framework de type ORM (Object-Relational Mapping) utilisé en Java pour faciliter la persistance des objets en base de données relationnelle. Il permet de faire le lien entre le modèle objet de l’application et le modèle relationnel de la base de données, en automatisant la génération des requêtes SQL. 
#v(2em)

Grâce à Hibernate, les développeurs peuvent manipuler directement des objets Java sans avoir à écrire explicitement la majorité des requêtes SQL. Le framework se charge de la traduction entre les objets et les tables, ainsi que de la gestion des relations, des transactions et du cycle de vie des entités. 
#v(2em)

Dans le cadre de ce projet, Hibernate est utilisé pour simplifier l’accès à la base de données et réduire la complexité liée à l’écriture et à la maintenance des requêtes SQL. 
#v(2em)
#v(2em)
 

== Base de données

=== PostgreSQL

PostgreSQL est un système de gestion de base de données relationnelle (SGBDR) open source, reconnu pour sa robustesse, sa conformité aux standards SQL et ses performances. Il permet de stocker, organiser et interroger des données structurées à l’aide du langage SQL. 
#v(2em)

PostgreSQL offre de nombreuses fonctionnalités avancées, telles que la gestion des transactions ACID, le support des index performants, ainsi que la possibilité de définir des types de données personnalisés et des requêtes complexes. Il est également fortement apprécié pour sa fiabilité et sa capacité à gérer de grandes quantités de données. 
#v(2em)

Dans le cadre de ce projet, PostgreSQL est utilisé comme base de données principale afin de stocker les informations liées aux utilisateurs et aux données applicatives, tout en garantissant la cohérence et l’intégrité des données. 
#v(2em)
#v(2em)


== CI/CD Pipeline

Le but de notre CI/CD pipeline serait d’automatiser la parti build et test de chaque nouvel realease tout en étant sur que il n’y aura pas de surprise car les tests auront été réalisé sur le même environnement que celui qui sera utiliser pour le serveur. De plus, il n’y aura aussi que de la continuous delivery, et donc pas de deployment automatique. 
#v(2em)
#v(2em)

=== Github actions

GitHub Actions est une plateforme d’intégration et de déploiement continus (CI/CD) intégrée directement à GitHub. Elle permet d’automatiser des workflows tels que les tests, la compilation, le linting ou encore le déploiement d’une application à chaque modification du code source. 
#v(2em)

Les workflows GitHub Actions sont définis sous forme de fichiers YAML et s’exécutent automatiquement en réponse à des événements du dépôt, comme un push, une pull request ou la création d’une release. Cette approche permet de standardiser et fiabiliser les processus de développement. 
#v(2em)

Dans le cadre de ce projet, GitHub Actions est utilisé pour automatiser les tests et les étapes de déploiement, afin de garantir la qualité du code tout en simplifiant la mise en place du pipeline CI/CD. 
#v(2em)
#v(2em)

=== Docker

Docker est une plateforme open source permettant de créer, déployer et exécuter des applications dans des conteneurs. Un conteneur est un environnement isolé qui regroupe une application ainsi que toutes ses dépendances, garantissant ainsi un comportement identique quel que soit le système hôte. 
#v(2em)

Grâce à cette approche, Docker simplifie la gestion des environnements de développement, de test et de production, en réduisant les problèmes liés aux différences de configuration entre machines. Les conteneurs sont légers, rapides à démarrer et plus efficaces en ressources que les machines virtuelles traditionnelles. 
#v(2em)

Dans le cadre de ce projet, Docker est utilisé afin de standardiser les environnements d’exécution et de faciliter le déploiement des différentes parties de l’application, notamment le backend et les services associés. 
#v(2em)
#v(2em)

== Schéma de l'architecture
#figure(
  image("../images/Architecture.png", width: 100%),
  caption: [
    Schéma architecture logiciel
  ]
) <stack>
 

 

 

 

 

 

 

 

 

 

 

 

 

 

 

 

 

 

Schéma 