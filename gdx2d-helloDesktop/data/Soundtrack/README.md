# Mystis - Audio Assets Sources

Ce dossier contient tous les assets audio utilisés dans le jeu Mystis.

## Structure des dossiers

- **OST/** - Musiques de fond du jeu
- **SFX/** - Effets sonores du jeu

## Sources et Crédits

### Musiques (OST)

**Ori and the Will of the Wisps - Original Soundtrack**

Coker, G. (2020, 9 mars). *Ori and the Will of the Wisps - Main Theme* [Fichier audio]. YouTube. https://www.youtube.com/watch?v=Qh3NnE7Ye2M

Coker, G. (2020, 9 mars). *Ori and the Will of the Wisps - A Yearning for the Sky* [Fichier audio]. YouTube. https://youtu.be/rE3Qdx1nW9s?si=TvQO5GxSz04f4E0R

Coker, G. (2020, 9 mars). *Ori and the Will of the Wisps - Escaping a Foul Presence* [Fichier audio]. YouTube. https://youtu.be/Ddhdi-kIr_c?si=wKyax5jbPKFWmQom

Coker, G. (2020, 9 mars). *Ori and the Will of the Wisps - Shriek and Ori* [Fichier audio]. YouTube. https://youtu.be/ClCW2A1355A?si=gkAQIool5GfnnluF

**Site officiel du jeu :** https://www.orithegame.com

**Utilisation dans le projet :**
- Musique de menu principal (Main Theme + A Yearning for the Sky)
- Musique de gameplay (Escaping a Foul Presence + Shriek and Ori)

### Effets Sonores (SFX)

**Epic Stock Media & Bosnow - Via Uppbeat**

Epic Stock Media. (s.d.). *Magic spell - light magic* [Fichier audio]. Uppbeat. https://uppbeat.io/sfx/magic-spell-light-magic/8681/22646

Epic Stock Media. (s.d.). *Magic spell - sparkle blast* [Fichier audio]. Uppbeat. https://uppbeat.io/sfx/magic-spell-sparkle-blast/8696/22666

Bosnow. (s.d.). *Magic impact* [Fichier audio]. Uppbeat. https://uppbeat.io/sfx/magic-impact/163004/43275

**Utilisation dans le projet :**
- `[SHOOT]` - Magic spell sparkle blast : Son de tir de projectile magique
- `[KILL]` - Magic spell light magic : Son d'élimination d'ennemi
- `[ULT]` - Magic impact : Son d'activation de l'ultimate

## Notes d'Utilisation

- **Utilisation académique**: Ces assets sont utilisés uniquement dans le cadre du projet étudiant Mystis pour le cours 101.2 - Programmation Orientée Objets (POO) en Informatique et Système de Communication (ISC) de la HES-SO Valais-Wallis.
- **Pas de distribution commerciale**: Ce projet n'est pas destiné à la commercialisation.
- **Crédits appropriés**: Tous les créateurs originaux sont crédités ci-dessus.

## Intégration Technique

Les fichiers audio sont chargés par la classe `AudioManager` dans `Music_and_sfx.scala`:
- **Musiques**: Utilisent `MusicPlayer` pour les pistes longues en boucle
- **SFX**: Utilisent `Sound` pour les effets courts et répétitifs
- **Volume**: Ajusté pour un équilibre optimal (SFX à 30-60% du volume maximum)

---

**Projet Mystis** - Développé par Dino Bijelic et Vadym Chobu  
HES-SO Valais-Wallis, Informatique et Système de Communication  
Cours 101.2 - Programmation Orientée Objet (POO), JUIN 2025