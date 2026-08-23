# BG Remover Android

Aplicativo Android para remover fundo de imagens com IA, com pré-visualização lado a lado (antes / depois).

Usa **Google ML Kit Subject Segmentation** — processamento 100% no aparelho, sem enviar fotos para a internet.

## Funcionalidades

- Escolher imagem da galeria
- Remover fundo com um toque
- Pré-visualização Antes / Depois
- Salvar PNG com transparência na galeria (`Pictures/BG Remover`)
- Barra de progresso durante o processamento

## Requisitos

- Android 8.0 (API 26) ou superior
- Android Studio Ladybug (2024.2+) ou mais recente
- Conexão na **primeira** execução para o Google Play Services baixar o modelo de IA (depois funciona offline)

## Como abrir o projeto

1. Clone o repositório:
   ```bash
   git clone https://github.com/assumcaonerd/bg-remover-android.git
   ```
2. Abra a pasta no **Android Studio**
3. Aguarde o Gradle sincronizar
4. Conecte um celular ou inicie um emulador
5. Clique em **Run** (▶)

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

## App desktop relacionado

Versão para Windows/Linux em Python:  
https://github.com/assumcaonerd/bg-remover
