# CuidarApp

Aplicativo Android para acompanhamento de idosos por cuidadores. Permite gerenciar dieta semanal, medicamentos, exercícios físicos, visitas e emergências.

## Funcionalidades

### Perfil Cuidador
- Cadastrar e gerenciar idosos
- Acompanhar progresso (refeições, medicamentos, exercícios, check-ins)
- Definir e gerenciar dieta semanal
- Gerenciar medicamentos
- Agendar e gerenciar visitas
- Definir exercícios físicos

### Perfil Idoso
- Consultar dieta semanal
- Registrar o que comeu
- Registrar como está hoje (humor e físico)
- Confirmar medicamentos tomados
- Fazer check-in diário dos exercícios
- Consultar calendário de visitas
- Acionar botão de emergência

## Arquitetura

- **minSdk**: 26 (Android 8.0)
- **Linguagem**: Java
- **Banco de Dados**: Room (SQLite)
- **Interface**: WebView com HTML/CSS/JS local
- **Sem permissão INTERNET**: App funciona 100% offline

### Estrutura de Pacotes

```
com.cuidarapp/
├── model/          # Entidades e enums
├── data/
│   └── dao/        # DAOs do Room
├── repository/     # Camada de repositório
├── service/        # Lógica de negócio
└── ui/
    └── bridge/     # Ponte JavaScript
```

## Credenciais de Demonstração (Build Debug)

No build de debug, o app inicializa automaticamente com dados de demonstração:

### Cuidador
- **E-mail**: `cuidador@demo.com`
- **Senha**: `cuidador123`

### Idoso
- **E-mail**: `idoso@demo.com`
- **Senha**: `idoso123`

> **Nota**: As senhas são armazenadas usando PBKDF2 com salt aleatório. Não são salvas em texto puro.

## Como Executar

### Pré-requisitos
- Android Studio Hedgehog (2023.1.1) ou superior
- JDK 17
- Android SDK 34

### Passo a Passo

1. **Abrir no Android Studio**
   ```
   File > Open > Selecione a pasta CuidarApp
   ```

2. **Sincronizar Gradle**
   - O Android Studio deve sincronizar automaticamente
   - Se não sincronizar, clique em `Sync Project with Gradle Files` na barra de ferramentas

3. **Executar no Emulador**
   - Crie um AVD (Android Virtual Device) com API 26 ou superior
   - Selecione o dispositivo no menu dropdown
   - Clique em `Run 'app'` (Shift+F10)

4. **Gerar APK Debug**
   ```
   Build > Build Bundle(s) / APK(s) > Build APK(s)
   ```
   O APK será gerado em: `app/build/outputs/apk/debug/app-debug.apk`

5. **Instalar no Celular via USB**
   - Ative o modo desenvolvedor no celular
   - Conecte via USB
   - Execute:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## Testes

### Executar Testes Unitários
```bash
./gradlew test
```

### Testes Incluídos
- Validação do nome (nulo, vazio, espaços)
- Ajuste dos limites de fonte (0.8 a 2.0)
- Autenticação inválida (PBKDF2)
- Vínculo cuidador-idoso
- Confirmação de medicamento
- Conclusão de exercício
- Cálculo de progresso

## Acessibilidade

- Font-size base de 20px para tela do idoso
- Contraste mínimo de 4.5:1
- Foco visível em todos os elementos interativos
- Botões com altura mínima de 56px
- Ícones de pelo menos 32px com texto descritivo
- Botões A- e A+ no cabeçalho para ajuste de fonte
- Escala de fonte de 0.8 a 2.0, persistida por usuário
- Interface responsiva sem rolagem horizontal quando ampliada

## Segurança

- WebView configurada para carregar apenas conteúdo local
- Bloqueio de acesso a arquivos externos
- Sem permissão INTERNET
- Bridge JavaScript com métodos estritamente necessários
- Validação e sanitização de dados em Java
- Senhas derivadas com PBKDF2 + salt

## Dependências

```gradle
// Core AndroidX
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'androidx.webkit:webkit:1.9.0'

// Room Database
implementation 'androidx.room:room-runtime:2.6.1'
annotationProcessor 'androidx.room:room-compiler:2.6.1'

// JSON
implementation 'com.google.code.gson:gson:2.10.1'

// Core library desugaring
coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:2.0.4'

// Testing
testImplementation 'junit:junit:4.13.2'
testImplementation 'org.mockito:mockito-core:5.8.0'
```

## Notas de Desenvolvimento

1. **Room com classes abstratas**: A classe `Usuario` é abstrata e não pode ser instanciada diretamente pelo Room. As entidades concretas são `Cuidador` e `Idoso`.

2. **LocalDate/LocalTime**: Utilizamos core library desugaring para suportar `java.time` em APIs abaixo de 26.

3. **WebViewAssetLoader**: Carrega assets locais com origem `https://appassets.androidplatform.net` para maior segurança.

4. **Dados persistentes**: Todos os dados são salvos no SQLite via Room e persistem após fechar/abrir o app.

## Licença

Este projeto é um protótipo educacional. Não substitui serviços de emergência reais ou acompanhamento médico profissional.
