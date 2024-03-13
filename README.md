# Sfolder

Sfolder est une application de coffre-fort.
Elle procède par isolation & chiffrement des fichiers.

Utilisez le bouton "**+**" pour rajouter des fichiers à protéger.
Leur nom d'origine apparait dans la liste.
La copie originale est supprimée.
Cliquez sur un élément de la liste pour dérouler un menu contextuel. Il vous permet de soit "*Débloquer*", soit "*Supprimer*" définitivement le fichier.
-> "**Débloquer**" un fichier restaure la version déchiffrée. Une notif vous dit où récupérer cette dernière.

Vous pouvez changer votre mot de passe grâce au bouton "**Cadenas**"

Une clé de chiffrement est aléatoirement générée à l'installation. Elle n'est pas associée à votre mot de passe.
L'algorithme de chiffrement est l'AES-256. Chaque couple de chiffré-déchiffré possède son vecteur d'initialisation.

Le code est disponible sur la branche master




