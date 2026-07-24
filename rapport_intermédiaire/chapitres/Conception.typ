= Conception <conception>
#v(2em)

Cette section présente les choix de conception réalisés lors du développement du projet. Avant d'aborder les aspects techniques liés à l'implémentation. C'est une étape primordiale afin d'avoir de bonnes bases pour le projet.
#v(1em)

Dans un premier temps, la conception orientée objet est présentée afin de décrire les principales classes du projet, leurs responsabilités et les relations qu'elles entretiennent entre elles.
#v(1em)

La section s'intéresse ensuite au modèle de données, qui décrit la manière dont les informations sont représentées et persistées. Les choix effectués en matière de stockage des données, notamment l'approche retenue pour conserver l'état des parties, y sont également justifiés.
#v(1em)

Enfin, les principaux mécanismes de l'application sont détaillés, notamment l'architecture client-serveur et le fonctionnement du système de combat.

#v(2em)
== Conception Orientée Objet
#v(2em)

=== Controller
#v(2em)
Avant toute chose, afin d'améliorer la lisibilité des schémas, les accesseurs (*getters* et *setters*) ainsi que les constructeurs ne seront pas représentés. En effet, la majorité des constructeurs utilisent l'injection de dépendances et leur présence alourdirait considérablement les diagrammes sans apporter d'informations pertinentes.
#v(1em)

La base de notre application Play est le *HomeController*, qui est chargé de répondre aux requêtes des utilisateurs et de les rediriger via les différentes routes de l'application.
#figure(
image("../images/controller.png", width: 100%),
caption: [
UML du HomeController
]
) <controller>
#v(2em)
Ce contrôleur a pour objectif de gérer toute la partie liée à l'authentification, en s'appuyant sur des formulaires ainsi que sur un service de hachage des mots de passe, qui sera détaillé ultérieurement. Son rôle est également de créer une WebSocket, puis de déléguer la gestion des connexions à l'*ActorMonitor* et la gestion du matchmaking ainsi que le lancement des parties au *MatchmakingService*, lorsque toutes les conditions sont réunies.
#v(1em)

L'*ActorMonitor* est responsable de la création et de la gestion des acteurs de l'application. Son fonctionnement sera présenté plus en détail dans une section dédiée.
#v(1em)

Cette séparation permet d'isoler la gestion du cycle de vie des connexions WebSocket de la logique de matchmaking, qui pourrait évoluer indépendamment: par exemple si l'on souhaite plus tard changer l'algorithme d'appariement des joueurs sans toucher à la gestion des connexions.
#v(1em)

Le *GameLevelService* est chargé de stocker les données relatives aux niveaux des joueurs. Comme expliqué précédemment, le niveau d'un joueur détermine le nombre maximal d'unités que son équipe peut posséder. Il influence également les probabilités d'obtenir des unités plus ou moins rares dans la boutique. Ce service centralise donc ces informations ainsi que l'expérience nécessaire pour atteindre le niveau suivant.
Ces règles étant identiques pour toutes les parties en cours, les stocker directement sur chaque Team/Game aurait entraîné une duplication inutile.
#v(1em)

Le *SeedMakerService* est chargé de générer les *seeds* utilisées pendant une partie afin de maîtriser les mécanismes aléatoires et de garantir un comportement reproductible, mais cela sera expliqué plus en détails lors de l'implémentation.
#v(1em)

Enfin, le *SimulationService* est responsable de simuler les combats entre deux équipes afin d'en déterminer le vainqueur.


#v(2em)
=== Unit
#v(2em)
Comme expliqué précédemment, les unités sont au coeurs du jeu. Les unités (*Unit*) stockent leur statistiques, leur nom, ainsi que leur compétence contenu sous formes de fragment (*AbilityFragment*).
Chaque fragment permet a l'aide de sa stratégie de détermine la target de l'effect et de la lui appliquer.
#figure(
image("../images/unitAbility.png", width: 100%),
caption: [
UML de l'Unité
]
) <unit>
#v(2em)
La compétence est fragmentée afin que l'on puisse avoir plusieurs type d'effect qui affectent des cibles différentes. Sans cette fragmentation on aurait soit un système de ciblage commun à tous les effets soit chaque effet doit gérer lui même son ciblage.  Avec cette fragmentation et l'aide du pattern Stratégie, il devient possible de décrire un système de ciblage différent pour chaque fragment, offrant une grande modularité.

#v(2em)
=== Effet
#v(2em)
Les deux sous-classes d'effet actuelles sont les *scalingEffect* et les *statChangingEffect*. Les *ScalingEffect* sont des effets possédant un puissance de base mais qui gagne en intensité en fonction d'une certaine statistique du lanceur de l'effect. La ou les *StatChangingEffect* n'ont pour but que de changer les statistiques de la cible. C'est notamment à l'aide de ces effects que les objets améliorent les unités.
#figure(
image("../images/effect.png", width: 100%),
caption: [
UML des Effets
]
) <effect>
#v(2em)
Cette séparation permet d'utiliser le polymorphisme à son plein potentiel afin que chaque sous-classe d'effet traduise un comportement d'effet bien différent des autres.

#v(2em)
=== User
#v(2em)
Pour que notre jeu fonctionne il nous faut des utilisateurs qui jouent des parties. On a donc des comptes représentant les utilisateurs (*User*), qui ont une équipe par partie auquel ils ont participé ou participent actuellement. Les équipes (*Team*) ont pour but de changer en fonction des actions de l'utilisateur, ainsi que selon le déroulement de la partie. La partie (*Game*), aura pour but de stocker les équipes et de savoir lesquelles sont disqualifiées ou non.
#figure(
image("../images/user.png", width: 100%),
caption: [
UML de l'utilisateur
]
) <user>
#v(2em)
Un joueur pouvant participer à plusieurs parties, la Team représente sa participation à une partie donnée, distincte de son identité de joueur (User).

#v(2em)
=== Game
#v(2em)
La partie a aussi comme objectif de gérer les stocks de champions et de donner une unité aléatoirement provenant d'un stockage. Les champions sont répartis dans différents stockage (*Pool*), chacun différencié par la rareté des champions qu'il stocke. Et ces stockages sont eux aussi séparés en entrées (*PoolEntry*), afin que l'on sache combien de fois de la même unité il reste encore dans ce stockage.
#figure(
image("../images/game.png", width: 100%),
caption: [
UML de la partie
]
) <game>
#v(2em)
Les *PoolEntry* permettent de simplifier la gestion des unités. Sans eux, il faudrait manipuler directement une liste d'unités et la mettre à jour en permanence.

Comme l'objectif est de conserver au moins un exemplaire de chaque unité en stock, il serait nécessaire de parcourir toute la liste à chaque fois que l'on souhaite connaître le nombre d'unités disponibles. Cette approche est peu pratique et potentiellement coûteuse.

Avec les *PoolEntry*, ce problème disparaît : chaque entrée centralise les informations nécessaires, notamment la quantité disponible, ce qui permet d'obtenir rapidement un décompte sans avoir à parcourir l'ensemble des unités.


#v(2em)
=== Team
#v(2em)
Enfin l'équipe doit stocker les champions qui la composent. Pour cela elle utilise des *InstanceUnit* qui stockent l'unité de base ainsi que toutes les données modifiables de l'unité telles que ses objets, sa position ou encore son niveau.
L'équipe doit aussi stocker le magasin actuellement proposé au joueur.
#figure(
image("../images/team.png", width: 100%),
caption: [
UML de l'équipe
]
) <team>
#v(2em)
L'équipe stocke aussi un snapshot du dernier état du magasin connu en stockant les unités présentent dedans.

L'utilisation d'*InstanceUnit* nous permet d'avoir des unités qui peuvent subir des modifications sans jamais devoir changer l'unité de base, ceci permet aussi d'éviter la duplication des statistiques lorsque l'on possède plusieurs fois la même unité.


#v(2em)
== Modèle des données
#v(2em)
Afin de pouvoir gérer et faire évoluer nos données sans complexifier excessivement le code applicatif, il a semblé évident de devoir stocker l'ensemble des données de notre application dans une base de données structurée. On y a donc persisté nos unités, ainsi que leurs compétences et les objets.
On y persiste également les parties, les équipes qui y participent, ainsi que les utilisateurs.

#v(2em)
=== Choix de persistance des événements : relationnel normalisé plutôt qu'événementiel en JSON
#v(2em)
L'un de nos souhaits était de permettre de rejouer les parties une fois terminé, afin de pouvoir les étudier et s'améliorer, comme c'est l'usage dans la plupart des jeux compétitifs. Une solution basée sur des événements semble naturelle, chaque partie pouvant se traduire simplement comme une suite d'actions amenant un utilisateur d'un état A à un état B.
Pour cela, deux approches étaient envisageables :
#figure(
image("../images/replayRelationnel.png", width: 75%),
caption: [
Schéma replay sans Json
]
) <replay>
Un schéma relationnel normalisé, avec une table dédiée pour chaque type d'événement, où chaque colonne est typée et validée par la base de données elle-même.
#v(2em)
#figure(
image("../images/replayJson.png", width: 100%),
caption: [
Schéma replay avec Json
]
) <replayJson>
Un stockage événementiel basé sur du JSON, où chaque événement serait stocké dans une unique table, sous la forme d'un simple champ JSON contenant les détails propres à son type.
#v(1em)

Cette deuxième option présentait un avantage réel : une base de données plus compacte, ainsi qu'une conception s'arrêtant à une seule table, sans avoir à modéliser individuellement chaque type d'événement.
#v(1em)


Le problème de cette approche est qu'elle nous aurait fait perdre l'intégrité référentielle et la validation structurelle de nos événements, normalement garanties par le moteur de la base de données lorsqu'un schéma est explicitement défini pour chaque table. Il aurait alors fallu réaliser nous-mêmes, côté backend, la validation et le parsing de chaque événement afin de nous assurer de leur cohérence, ca aurait été un travail supplémentaire non négligeable, pour un résultat offrant moins de garanties que ce qu'un schéma relationnel strict assure nativement.
#v(1em)

On a donc préféré normaliser complètement notre modèle de données plutôt que d'utiliser un champ JSON générique, afin de conserver une validation stricte du schéma de chaque type d'événement directement au niveau de la base de données, plutôt que de devoir réimplémenter cette validation côté backend.
#v(1em)

Concrètement, cela signifie que chaque type d'action possible du joueur correspond à sa propre table, héritant d'une table event commune (portant les champs partagés comme l'horodatage et le round concerné), sur le même principe d'héritage relationnel déjà présenté pour nos entités de jeu (Effect, AbilityFragment, etc.). Ne stocker que ce qui n'est pas dérivable autrement permet de garder ces tables légères : par exemple, lors de l'achat d'une unité, seul l'identifiant de l'unité achetée est conservé, son coût restant récupérable depuis la table unit au moment de rejouer l'événement.

#v(2em)
=== Rappel des actions possibles du joueur
#v(2em)
Pour mieux comprendre la nature des événements que nous devons persister, il est utile de rappeler le déroulement d'une partie et les actions qu'un joueur peut y effectuer. Une partie se compose d'une succession de rounds, chacun alternant une phase d'achat et une phase de combat. Durant la phase d'achat, le joueur peut notamment acheter, vendre ou déplacer une unité, lui attribuer un objet, relancer son shop, ou encore acheter de l'expérience.
Le diagramme de séquence suivant illustre ces différentes actions et leur enchaînement au cours d'un round :
#figure(
image("../images/DéroulementDeLaPartie.png", width: 100%),
caption: [
Schéma de séquence user input
]
) <input>
#v(2em)
C'est cette même liste d'actions qui détermine directement les types d'événements que nous persistons en base de données, chacun correspondant à l'une de ces actions.

#v(2em)
=== Modélisation des événements
#v(2em)

On distingue trois catégories d'événements, toutes héritant d'une classe commune Event.
#v(1em)

==== Événements liés aux données de base du joueur
 Une première catégorie regroupe les événements traduisant un changement dans les données fondamentales d'un joueur : ses points de vie, son expérience, ou son or. Chaque Event est associé à un Round, ce qui permet de savoir à quel round un événement donné s'est produit, ainsi qu'à un step, propre à l'équipe concernée, qui permet de connaître l'ordre relatif des événements au sein de ce round. Les combats sont quant à eux représentés par une association entre deux Rounds, traduisant le fait que deux équipes se sont affrontées lors d'un round donné. L'ensemble de ces rounds est finalement rattaché à l'équipe (Team) à laquelle ils appartiennent.

#figure(
image("../images/eventBasic.png", width: 90%),
caption: [
UML team/Event
]
) <teamEvent>
#v(2em)

==== Événements liés aux unités
Une deuxième catégorie regroupe l'ensemble des actions du joueur portant sur une unité : achat, vente, déplacement, ou modification des objets qui lui sont attribués. Ces événements sont tous représentés comme des sous-classes d'UnitEvent, elle-même sous-classe d'Event. Chaque UnitEvent référence l'InstanceUnit concernée par l'action, ce qui permet de savoir précisément quelle unité a été affectée par l'événement, tout en bénéficiant des attributs communs (round, step) hérités d'Event.

#figure(
image("../images/unitEvent.png", width: 100%),
caption: [
UML UnitEvent
]
) <unitEvent>
#v(2em)

==== Événements de changement de shop
Enfin, une troisième catégorie permet de conserver la composition du magasin proposé au joueur après chaque changement, notamment lors d'un reroll. Cette catégorie est également représentée comme une sous-classe d'Event, mais elle se distingue des deux précédentes en ce qu'elle référence non pas une seule unité, mais l'ensemble des unités présentes dans le nouveau magasin.

#figure(
image("../images/changingShopEvent.png", width: 100%),
caption: [
UML changingShopEvent
]
) <changingShopEvent>
#v(2em)

L'ensemble de ces événements et de ces rounds est finalement persisté en base de données, ce qui nous permet, comme expliqué précédemment, à la fois d'assurer la tolérance aux pannes et de proposer un système de replay des parties.



#v(2em)
== Architecture réseau
#v(2em)
L'un des principaux défis de ce projet est la gestion des connexions, afin de proposer un service à la fois qualitatif et performant.

#v(2em)
=== Authentification
#v(2em)
L'authentification a pour objectif d'identifier l'utilisateur, puis d'établir une connexion WebSocket associée à son identifiant (userId), afin de pouvoir le reconnaître tout au long de la session.
#figure(
image("../images/Connexion.png", width: 100%),
caption: [
Schéma de séquence de l'authentification
]
) <connexions>
#v(2em)
Lors de l'inscription, le backend vérifie d'abord auprès de la base de données qu'aucun utilisateur n'existe déjà avec le même nom d'utilisateur ou la même adresse e-mail, et que les données du formulaire sont valides. En cas d'erreur ou de conflit, une erreur est renvoyée au client ; sinon, les données de l'utilisateur sont enregistrées, et celui-ci est redirigé vers la page de connexion.
#v(1em)

Lors de la connexion, le backend recherche l'utilisateur correspondant à l'identifiant fourni, puis compare le mot de passe soumis à son équivalent haché à l'aide d'Argon2. Si aucun utilisateur ne correspond ou si le mot de passe est incorrect, une erreur est renvoyée. Dans le cas contraire, une connexion WebSocket est établie avec le client.

#v(2em)
=== Acteurs et moniteur
#v(2em)
On doit communiquer de manière bidirectionnelle avec nos joueurs pendant le déroulement des parties. C'est pourquoi on a choisi d'utiliser des WebSockets, implémentées à l'aide de Pekko au sein du framework Play, ce qui amène naturellement à l'utilisation du pattern acteur.
#figure(
image("../images/acteur.png", width: 100%),
caption: [
UML des acteurs
]
) <acteur>
#v(2em)
Nous disposons ainsi d'un ConnexionActor, chargé de gérer la connexion d'un joueur, de mettre en tampon (buffer) les messages échangés, et qui s'arrête automatiquement cinq minutes après une interruption de connexion. Nous disposons également d'un GameActor, responsable de la gestion et du déroulement d'une partie. L'utilisation d'acteurs permet de traiter séquentiellement les entrées des différents joueurs par le biais de messages, évitant ainsi les race conditions qui pourraient survenir dans d'autres circonstances.
#v(1em)

Afin d'éviter tout problème de concurrence lors de la création ou de la récupération de ces acteurs, on a également mis en place un moniteur, contenant une liste des ConnexionActor ainsi qu'une liste des GameActor. Ce moniteur est un singleton dont chacune des méthodes touchant à ces listes est synchronisée, ce qui garantit l'absence de toute race condition. Cette solution évite qu'il faille verrouiller l'accès à ces listes à chaque endroit du code où elles seraient utilisées. par exemple si plusieurs instances du HomeController tentaient d'accéder simultanément à la même liste de connexions ; à la place, tout accès passe systématiquement par ce moniteur central.

#v(2em)
=== Tolérance aux pannes
#v(2em)
On a conçu l'application dans le but d'être aussi résistante que possible aux pannes. Pour cela, comme vu précédemment, l'état actuel de la partie est enregistré à chaque fois qu'un événement est persisté. Ainsi, en cas de panne du serveur, l'état de la partie aura déjà été sauvegardé, et il sera possible de reprendre la partie là où elle s'était arrêtée lors du redémarrage.
#figure(
image("../images/Panne.png", width: 100%),
caption: [
Schéma de séquence des pannes
]
) <pannes>
#v(2em)
On a également mis en place des mécanismes de reconnexion. Durant les cinq premières minutes suivant la déconnexion d'un utilisateur, son ConnexionActor reste actif et met en tampon les différents événements de la partie, afin de pouvoir les lui retransmettre lors de sa reconnexion. Un tampon existe également côté client, afin que tout événement n'ayant pas été confirmé avant la déconnexion soit renvoyé lors de la reconnexion, en vue de sa validation. Si les cinq minutes sont dépassées, l'acteur est arrêté, et le client devra alors se reconnecter et redemander l'établissement d'une nouvelle connexion WebSocket.
#v(1em)

Ce délai de cinq minutes représente un compromis entre laisser suffisamment de temps à un joueur pour se reconnecter après un problème réseau temporaire, et éviter de mobiliser inutilement des ressources serveur pour un joueur qui ne reviendrait pas.

#v(2em)
== Système de simulation des combats
#v(2em)
Pour la simulation des combats, nous nous sommes inspirés d'une architecture couramment utilisée dans le jeu vidéo, qui offre à la fois une bonne modularité et une grande efficacité de traitement : l'Entity Component System (ECS). Le principe de cette architecture n'est pas d'avoir une représentation orientée objet réagissant à des événements, mais de distinguer trois types d'éléments : les entités, qui ne sont qu'un identifiant ; les composants, qui stockent l'ensemble des données propres à une entité ; et les systèmes, qui appliquent la logique métier. Chaque système est exécuté séquentiellement sur l'ensemble des entités, et n'effectue de changement que sur celles possédant les composants requis. Cette architecture est particulièrement adaptée lorsque le nombre d'entités et de systèmes est très élevé.
#v(1em)

Dans notre cas, le nombre d'unités en jeu reste restreint : nous avons donc choisi de fusionner les entités et les composants, cette distinction n'apportant pas de bénéfice significatif à notre échelle. Ce qui nous intéresse réellement, ce sont les systèmes et leur exécution. En effet, notre projet prévoyant d'implémenter un grand nombre d'effets différents, il nous fallait une architecture robuste, capable de traiter l'ensemble de ces effets de manière simple et compréhensible. D'où le choix d'associer un système dédié à chaque type de traitement.
#figure(
image("../images/ECS.png", width: 100%),
caption: [
schéma d'activité ECS
]
) <ecs>
#v(2em)
Concrètement, cela se traduit par un ComponentUnit, qui fusionne les données propres à une InstanceUnit avec celles de l'unité de base correspondante, afin de disposer de toutes les données nécessaires dès le début du combat. Trois systèmes interviennent ensuite séquentiellement : le statusSystem, qui vérifie les statuts (effets appliqués dans la durée) éventuellement infligés aux unités ; l'actionSystem, qui déclenche une attaque ou le lancement d'un sort selon le niveau de mana de l'unité ; et enfin l'endSystem, qui détermine si le combat est terminé. Notre FightingService fait alors itérer ces systèmes en boucle jusqu'à la fin du combat, moment auquel les unités encore en vie sont examinées afin de déterminer les points de vie perdus par chaque équipe.

