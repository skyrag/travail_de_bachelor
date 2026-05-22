On va reprendre les besoins a l'aide des must have que l'on doit faire :

Must have
-	Un système de login, où chaque utilisateur pourra se connecter et regarder ses statistiques, soit le nombre de victoires dans un premier temps
-	Un système de connexion multijoueur, où l'on pourra connecter 8 personnes à la même partie afin qu'ils s'affrontent
-	Un système de stockage des données utilisateurs
-	Un système de partie opérationnel : donc où un joueur pourrait lancer une partie pour jouer contre d'autres joueurs ou des bots. Donc cela inclut :
o	Un système de boutique pour acheter les unités
o	Un système de combat qui fera s'affronter les unités de deux joueurs à intervalles réguliers
o	Un système de gestion de son équipe afin que l'on puisse changer soit la formation de son équipe ou bien les unités constituant mon équipe.
Should have
-	Un système de classe et de famille pour les unités : au-delà d'avoir une unité, en avoir plusieurs différentes de la même classe ou famille devrait accorder un bonus (cf. GDD)
-	Un roster d'unités suffisamment grand : même si cela est assez subjectif, on va partir sur une base de 40 unités différentes qui seraient jouables
-	Une interaction spécifique lors de l'utilisation de chaque famille : s'il y a suffisamment d'unités différentes d'une même famille dans votre équipe, cela devrait apporter une mécanique nouvelle lors des combats
Could have
-	Un système d'augment pendant la partie : durant la partie plusieurs choix aléatoires sont proposés aux joueurs afin d'améliorer leur équipe ou de changer leur partie actuelle
-	Un pouvoir spécifique aux joueurs, et où chaque joueur pourrait choisir entre certains pouvoirs afin de changer la dynamique de sa partie
-	Une IA complexe et challengeante, qui pourrait poser un vrai danger à des joueurs connaissant le jeu ou les jeux de ce genre

faire un proof of concept ??? sur la connections réseau et l'affichage en temps réel pour que l'on voit si l'on arrive a connecter 8 personne en même temps et que ils ait tout un bon affichage.

les besoins: 
- le système de login avec Oauth2 ? 
- système de connexion multijoueur: les websockets de play 
- système de stockage des données : postgreSQL 
- et donc faire des requêtes sur la base de données.
- pouvoir faire une interface graphique : canvas 2D, ou phaser 2D ? ou pixi plus rapide ?. rendering juste avec pixi et la logique métier serait sur le backend donc on rendererais que les changement obtenu par la websocket.

identification des risques du projets :
- risque de latence et la connesion de 8 personnes a la fois au serveur

proof of concept pour montrer qu'il y a pas de risques ?
réaliser un proof of concept qui permet a 8 personne de se connecter et de chacun faire bouger un carré tout en voyant celui des autre bouger et cela sans trop de délai?