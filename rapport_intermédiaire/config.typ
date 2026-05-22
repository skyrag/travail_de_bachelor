/*
|              ██         
| ████▄ ▄███▄ ▀██▀▀ ▄█▀█▄ 
| ██ ██ ██ ██  ██   ██▄█▀ 
| ██ ██ ▀███▀  ██   ▀█▄▄▄ 
| 
| Ce fichier est basé sur du code précédemment écrit par @DACC4 et @samuelroland.
| Dépot original: https://github.com/DACC4/HEIG-VD-typst-template-for-TB
| 
*/

#let config = (

    global: (
      confidential: false,
      text_lang: "fr"
    ),

    information: (
      title: "Auto-battler multijoueur",
      academic_years: "2026-27",
      departement: (
        court: "TIC",
        long: "Technologies de l'information et de la communication (TIC)",
      ),
      filiere: (
        court: "ISC",
        long: "Informatique et systèmes de communication (ISC)",
      ),
      orientation: (
        court: "ISCL",
        long: "Informatique Logicielle (ISCL)",
      ),
      author: (
        name: "Nicolas Duprat",
        feminine_form: false,
      ),
      supervisor: (
        name: "Prof. Donini Pier",
        feminine_form: false,
      ),
      resume_publiable: [
        #lorem(100)\
        \
        #lorem(50)
      ]
    ),
    bibliography: (
      content: read("bibliography.yaml", encoding: none),
      style: "ieee"
    ),
  )