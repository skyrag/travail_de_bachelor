= Conclusion <conclusion>
#v(2em)
== Bilan du projet
#v(2em)

Ce travail de bachelor avait pour objectif de concevoir et de développer un jeu multijoueur de type auto-battler, accessible via le web, en mettant en œuvre une architecture logicielle complète et professionnelle : interface utilisateur, logique métier, persistance des données et communication client-serveur en temps réel.
#v(1em)

Sur le plan technique, une architecture client-serveur a été mise en place autour du Play Framework et de Pekko, avec un système d'acteurs permettant de gérer les connexions et le déroulement des parties de manière concurrente, sans race condition. La persistance des données repose sur une base relationnelle PostgreSQL, conçue pour offrir à la fois une tolérance aux pannes et un système de replay basé sur un modèle événementiel normalisé. Le système de combat s'appuie sur une architecture Entity Component System adaptée à nos besoins, combinée à un algorithme $A^*$ pour le déplacement des unités sur une grille hexagonale. Enfin, le frontend repose sur PixiJS pour l'affichage performant d'un grand nombre de sprites, et l'ensemble du projet est accompagné d'un pipeline CI/CD basé sur GitHub Actions et Docker.

//TODO a compléter sur la fin de l'avancement
#v(2em)

== Limitations et perspectives
#v(2em)

Plusieurs pistes d'amélioration ont été identifiées au cours du développement, mais n'ont pas pu être traitées dans le cadre de ce travail :
#v(1em)

La gestion du départ d'un joueur de la file d'attente de matchmaking avant le lancement d'une partie n'est pas encore implémentée.
L'authentification déléguée via OpenID Connect, initialement envisagée, a été abandonnée au profit d'une authentification classique par formulaire et hachage Argon2, en raison des contraintes administratives liées à la certification auprès des fournisseurs tiers ; elle pourrait être réintroduite en complément dans une version future soit en passant par Keycloak soit en faisant la demande au fournisseurs tiers.

//TODO a compléter

Ces éléments constituent des axes d'amélioration naturels pour une éventuelle poursuite du projet au-delà de ce travail de bachelor.
#v(2em)

== Apprentissages personnels
#v(2em)

Au-delà de l'aspect technique, ce projet représente le plus important que j'aie eu l'occasion de mener à ce jour, et il m'a permis d'apprendre énormément, tant sur le plan technique que méthodologique.
#v(1em)

Techniquement, j'ai dû combler plusieurs lacunes en cours de route, notamment en bases de données relationnelles — j'ai dû rattraper le cours de Bases de Données Relationnelles de la HEIG en parallèle du projet — ainsi que sur le pattern acteur avec Pekko, que je découvrais pour la première fois et dont la logique de programmation concurrente m'a demandé un temps d'adaptation certain.
#v(1em)

Méthodologiquement, ce travail m'a surtout confronté à l'importance d'une conceptualisation rigoureuse en amont de l'implémentation. Étant moi-même à l'origine du sujet, j'avais une vision du jeu que je voulais construire, mais j'ai sous-estimé l'ampleur de la réflexion logicielle nécessaire pour y parvenir et ma vision des besoins logiciels était floue, ce qui a entraîné de nombreux allers-retours au cours de l'implémentation faute d'avoir posé les choses à plat suffisamment tôt. Si c'était à refaire, je privilégierais une approche par fonctionnalités minimales (MVP), en utilisant mon temps de l'état de l'art pour faire des recherches plus approfondis sur ce dont j'ai besoin, et aussi poser très tôt tous mes fonctionnements pour gagner du temps plus tard et voir les problèmes plus tôt.
#v(1em)

Ce projet m'a ainsi permis non seulement de renforcer mes compétences techniques, mais surtout de mieux comprendre ma propre manière de travailler sur un projet de cette envergure, un enseignement que je compte mettre à profit dans mes futurs projets, notamment professionnels dans le domaine du jeu vidéo.