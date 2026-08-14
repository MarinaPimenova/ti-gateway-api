Run your GitHub Actions locally - use ACT

## Installation

```curl --proto '=https' --tlsv1.2 -sSf https://raw.githubusercontent.com/nektos/act/master/install.sh | sudo bash```
GitHub repo - https://github.com/nektos/act

## Use in VScode
Add extension to VSCode:
https://marketplace.visualstudio.com/items?itemName=SanjulaGanepola.github-local-actions

## Use secrets with ACT
```act --secret-file my.secrets``` - load secrets values from my.secrets file.
secrets file format is the same as .env format
https://nektosact.com/usage/index.html?highlight=secret#secrets
