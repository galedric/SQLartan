# PRO
Projet de semestre

## Documentation

https://docs.google.com/document/d/1FOK5C_Gk2t1mvBKGPfbzqO_D1RPlWqBSVDbWO8uzIHo/edit?usp=sharing

## Coding Style

https://google.github.io/styleguide/javaguide.html

### Exceptions:

 -  **4.1.1** : les "guards" n'utilisent pas d'acolades et s'écrivent en ligne. (`if (!isValid()) return false;`)
 -  **4.2** : Indentation avec 1 tab, alignement avec des espaces. (http://dmitryfrank.com/articles/indent_with_tabs_align_with_spaces)
 -  **4.4** : Column limit -> 120
 -  **5.2.6** et **5.2.7** : snake_case. Permet de distinguer facilement variables locales et attribut de la classe. Les variables locales sont généralement 1 mot, donc sans underscore. (https://en.wikipedia.org/wiki/Snake_case)

## IMPORTANT
Pour éviter de créer des branches useless lors d'un pull :
`git config --global pull.rebase true`
