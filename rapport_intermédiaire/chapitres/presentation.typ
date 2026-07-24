= Présentation du jeu
#v(2em)
Maintenant que nous disposons d'une meilleure vue d'ensemble du projet, nous allons rappeler les principes fondamentaux d'un auto-battler avant de présenter les règles du jeu.
#v(2em)

== Présentation d'un auto-battler
#v(2em)

Un auto-battler est un genre de jeu dans lequel chaque joueur constitue progressivement une équipe de personnages afin d'affronter les autres joueurs. Les combats se déroulent automatiquement, sans intervention du joueur, d'où le terme _auto-battler_. Le joueur doit donc adapter et renforcer son équipes pour maximiser ses chances de victoire.

 À l'issue de chaque combat, le perdant perd une partie de ses points de vie. La partie se termine lorsqu'il ne reste plus qu'un seul joueur en vie.
#v(1em)

Voyons maintenant plus en détail les mécaniques du jeu.
#v(2em)

== Mécanique principale de notre jeu
#v(2em)

Chaque joueur dispose d'un terrain composé d'hexagones et d'un banc. Le terrain est séparé en 2 partie, une première partie où l'utilisateur va pouvoir placer son équipe, et une deuxième partie qui servira à acceuillir l'équipe adverse lors des combats. Le banc est un espace de stockages des unités qui ne sont utilisés pour l'équipe actuelles.

Chaque joueur dispose d'une réserve d'or ainsi que l'accès à un magasin lui permettant d'acheter des unités parmi les cinq unités actuellement proposées. Les unités achetées sont alors immédiatement placées sur son banc.
#v(1em)

Les joueurs possèdent également un niveau qui détermine le nombre maximal d'unités pouvant être déployées sur le terrain. Ils peuvent dépenser de l'or pour gagner de l'expérience, acheter des unités ou changer les unités présentent dans le magasin.
#v(1em)

Enfin, chaque joueur possède un inventaire contenant des objets pouvant être équipés sur les unités afin d'améliorer leurs statistiques. Chaque joueur commencerait la partie avec trois objets aléatoires et en gagnerait trois autres après cinq combats.
#v(2em)

Les unités sont classées selon 5 niveaux de rareté : Commune, Peu commune, Rare, Épique et Légendaire. Plus une unité est rare, plus son coût est élevé et plus le joueur doit avoir d'un niveau élevé pour avoir des chances de la voir apparaître dans le magasin.
#v(1em)

Les unités possèdent également un niveau, où chaque passage au niveau supérieur les rend plus puissantes. Lorsqu'un joueur possède trois exemplaires d'une même unité, celles-ci fusionnent automatiquement pour former une unité de niveau supérieur.
#v(1em)

Chaque unité a ses propres statistiques, qui déterminent son efficacité au combat.
- *Points de vie* : quantité de dégâts que l'unité peut subir avant de mourir.
- *Mana initial* : quantité de mana avec laquelle l'unité commence le combat
- *Mana maximal* : la quantité de mana nécessaire pour lancer sa compétence
- *Attaque de base* : les dégâts que l'unité inflige avant application de multiplicateur ou de mitigation des dégâts
- *Dégâts d'attaque* : un multiplicateur appliqué à l'attaque de base pour déterminer les dégâts totaux
- *Puissance*: utilisé principalement pour déterminer l'efficacité de la compétence
- *Vitesse d'attaque* : fréquence des attaques
- *Armure* : mitige les dégâts d'attaque
- *Résistance magique* : mitige les dégâts de compétences
- *Portée* : distance maximale d'attaque

Enfin, au-delà de leurs statistiques, chaque unité dispose également d’une capacité unique qui leur confère un rôle particulier sur le champ de bataille.

Au cours d’un combat, les unités gagnent du mana à chaque attaque effectuée. Lorsque leur réserve de mana atteint sa valeur maximale, elles lancent automatiquement leur compétence.

Selon l’unité concernée, cette capacité peut infliger des dégâts, renforcer des alliés, affaiblir des ennemis ou produire des effets spéciaux modifiant le déroulement du combat.
#v(2em)

Les objets permettent d'améliorer les statistiques des unités qui les équipent. Une même unité peut porter jusqu'à trois objets simultanément.

L'objectif du joueur est donc de gérer son économie, améliorer ses unités, composer une équipe efficace et survivre jusqu'à être le dernier joueur encore en vie.
#v(2em)

== Déroulement de la partie
#v(2em)

Une partie oppose 8 joueurs. Au début de la partie, chaque joueur :
- Commence au niveau 1
- Possède une unité commune aléatoire de niveau 1
- Dispose de 30 pièces d'or

La partie est ensuite découpée en une succession de manches. Chaque manche comporte deux phases :
#v(2em)

*Phase de préparation*:

Pendant 90 secondes, chaque joueur peut:
	- Déplacer une unité du banc à son équipe ou inversement.
	- Acheter une unité présente dans le magasin. Son coût varie entre 1 et 5 pièces d'or selon sa rareté.
	- Vendre une unité qui est dans son équipe ou sur son banc, afin de récupérer son coût d'achat ainsi que les objets dont elle était équipée.
	- Équiper un objet sur une unité
	- Rafraîchir son magasin pour 2 pièces d'or
	- Acheter 4 point d'expérience pour 4 pièces d'or.
#v(2em)

*Phase de combat* :
Chaque joueur affronte automatiquement l'équipe d'un autre joueur choisi aléatoirement.

Durant cette phase, les joueurs ne peuvent plus modifier leur équipe. En revanche, ils peuvent toujours effectuer des actions n'ayant aucun impact sur le combat en cours, comme gérer leur banc ou acheter des unités dans le magasin.
À la fin du combat, le joueur vaincu perd un nombre de point de vie calculé selon la formule suivante :
Points de vies perdus = nombre d'unités en vie chez l'adversaire + le niveau de l'adversaire.
#v(2em)

À la fin de chaque combat, tous les joueurs gagnent 5 pièces d'or et 2 points d'expérience. Le vainqueur reçoit en plus un bonus de 2 pièces d'or.

La partie prend fin lorsqu'il ne reste plus qu'un seul joueur ayant encore des points de vie.
#v(2em)


