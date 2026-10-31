# Site do CuidarApp

Site estático (HTML, CSS e JS puros), pronto para a Vercel. Não precisa de build nem de dependências.

## Publicar na Vercel

**Pelo painel**
1. Em vercel.com, clique em **Add New → Project** e importe o repositório `InBradleydesign/1-Download-and-Export`.
2. Em **Root Directory**, escolha `site`.
3. Em **Framework Preset**, deixe **Other**. Deixe Build Command e Output Directory vazios.
4. Clique em **Deploy**.

**Pela linha de comando**
```bash
cd site
npx vercel        # prévia
npx vercel --prod # produção
```

## O que já está configurado (`vercel.json`)
- `/baixar` e `/download` levam direto ao APK da release mais recente.
- URLs sem `.html` e página `404.html` própria.
- Cabeçalhos de segurança e cache das imagens.

## Atualizar
O link de download (botões, QR code, `/baixar` e `/download`) aponta para
`releases/latest/download/CuidarApp.apk`, então não precisa mudar o site a cada
nova versão do app. Basta publicar a release com uma tag `v*`: o workflow
`release.yml` gera o `CuidarApp.apk` assinado com a chave fixa, o que permite
instalar por cima da versão anterior sem perder os dados.

A cada versão nova, acrescente um cartão na seção **Novidades** (`#novidades`
em `index.html`) e ajuste o número da versão no bloco de download.
