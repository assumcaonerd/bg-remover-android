# BG Remover Android

Aplicativo Android para remover fundo de imagens com IA, com pré-visualização lado a lado (antes / depois).

Usa **Google ML Kit Subject Segmentation** — processamento 100% no aparelho, sem enviar fotos para a internet.

## Baixar o APK (pronto para instalar)

1. Abra a aba **[Actions](https://github.com/assumcaonerd/bg-remover-android/actions)**
2. Clique no workflow **Build APK** (última execução com sucesso)
3. Em **Artifacts**, baixe **BG-Remover-Android-Debug**
4. Extraia o `.zip` e copie o arquivo `.apk` para o celular
5. No Android, permita instalar de fontes desconhecidas e abra o APK

Também é possível disparar um novo build manualmente:
**Actions → Build APK → Run workflow**

Para publicar uma versão oficial:
```bash
git tag v1.0.0
git push origin v1.0.0
```
Isso cria um **Release** com o APK anexado.

## Funcionalidades

- Escolher imagem da galeria
- Remover fundo com um toque
- Pré-visualização Antes / Depois
- Salvar PNG com transparência na galeria (`Pictures/BG Remover`)
- Barra de progresso durante o processamento

## Requisitos

- Android 8.0 (API 26) ou superior
- Conexão na **primeira** execução para o Google Play Services baixar o modelo de IA (depois funciona offline)

## Abrir no Android Studio

```bash
git clone https://github.com/assumcaonerd/bg-remover-android.git
```

1. Abra a pasta no **Android Studio**
2. Aguarde o Gradle sincronizar
3. Conecte um celular ou inicie um emulador
4. Clique em **Run** (▶)

## Estrutura principal

```
app/src/main/java/br/com/bgremover/
  MainActivity.kt          → Interface (Jetpack Compose)
  BackgroundRemover.kt     → Lógica de remoção com ML Kit
```

## Observações

- O modelo do ML Kit é baixado pelo Google Play Services na primeira vez.
- Funciona melhor com pessoas, animais e objetos em destaque no primeiro plano.
- Não é o mesmo motor do app desktop (`rembg`). A qualidade pode variar; no celular o foco é velocidade e privacidade.
- O APK gerado pelo CI é **debug** (assinado com chave de debug), ideal para testes.

## App desktop relacionado

Versão para Windows/Linux em Python:  
https://github.com/assumcaonerd/bg-remover
