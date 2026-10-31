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

### Diário de Saúde (v1.5)
- **Perfil de saúde**: condições, medições acompanhadas, observações do profissional, orientações do profissional (valor + mensagem) e autorização para o cuidador anotar pelo idoso.
- **Registros em casa**: pressão (sistólica, diastólica e pulso opcional), glicemia com o momento da medição, peso, temperatura, saturação, frequência cardíaca, volume de urina e sintomas (tipo, intensidade, horário, duração opcional e observação). Editar e excluir com confirmação; cada registro guarda quem anotou e quando, e quem editou.
- **Remédios**: tomado, tomado com atraso (confirmado mais de 60 min depois do horário) ou não tomado. O app nunca marca uma dose sozinho e não sugere doses.
- **Histórico e gráficos**: hoje, 7 dias, 30 dias ou período escolhido; pressão em duas séries; média/menor/maior só com 3 registros ou mais (glicemia separada por momento).
- **Lembretes**: medições (horários e dias definidos pelo cuidador, sem horário padrão), remédios (no horário do cadastro) e consultas. Situação pendente, feito ou ignorado. Reagendados após reiniciar o celular, mudar hora/fuso ou atualizar o app.
- **Relatório PDF**: identificação, período, quantidade de registros, tabelas com unidades, gráficos, sintomas, observações, histórico de remédios, data de emissão e aviso de que não substitui avaliação profissional. Pré-visualização e compartilhamento pelo Android; nada é enviado automaticamente.
- **Sintomas de alerta** (dor no peito, falta de ar, confusão, fraqueza de um lado, desmaio): o app orienta procurar atendimento imediato.

O app não diagnostica, não classifica valores como normais ou alterados e não tem limites clínicos próprios: as únicas orientações automáticas são as cadastradas a partir do profissional de saúde. As faixas de validação servem só para barrar erros de digitação.

#### Privacidade e limitações
- Os dados ficam no banco local (Room, versão 2) e ficam **fora do backup na nuvem e da transferência entre aparelhos** (`backup_rules.xml` / `data_extraction_rules.xml`).
- O diário exige consentimento (LGPD) e pode ser apagado a qualquer momento (Cuidador › Saúde › Perfil).
- **Não há sincronização entre aparelhos**: cada celular tem o seu diário. O cuidador acompanha pelo mesmo celular (área do cuidador, protegida por senha). Não existe backend.
- O PDF é gerado no cache interno do app; só sai do aparelho se a pessoa tocar em Compartilhar.
- Nenhum dado de saúde é escrito no log.
- Em Android 12+, sem a permissão de alarme exato o Android pode atrasar o aviso alguns minutos; alguns fabricantes bloqueiam avisos por economia de bateria (o app mostra o estado e leva às configurações).

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
├── health/         # Diário de Saúde
│   ├── model/      # Entidades Room (medições, sintomas, doses, lembretes, perfil)
│   ├── data/       # DAOs e migração 1 -> 2
│   ├── domain/     # Validação, estatísticas, orientações, cálculo de lembretes (Java puro)
│   ├── reminders/  # AlarmManager, notificações e receptores
│   ├── report/     # Montagem e desenho do PDF
│   ├── HealthRepository.java  # Consentimento, permissões e auditoria
│   └── HealthBridge.java      # window.CuidarHealth
└── ui/
    └── bridge/     # Ponte JavaScript
```

A interface do diário está em `assets/web/health.js`, integrada ao `app.js`.

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

## Versão de distribuição e atualizações

O Android só instala uma versão nova **por cima** da antiga (mantendo o banco Room
e as configurações) quando o pacote (`com.cuidarapp`) e a **chave de assinatura**
são os mesmos e o `versionCode` é maior. Por isso o APK distribuído é o `release`,
sempre assinado com a mesma chave. As migrações do Room não apagam dados
(não há `fallbackToDestructiveMigration`).

A chave **nunca** vai para o repositório. O build lê a assinatura de variáveis de
ambiente (`CUIDARAPP_KEYSTORE_FILE`, `CUIDARAPP_KEYSTORE_PASSWORD`,
`CUIDARAPP_KEY_ALIAS`, `CUIDARAPP_KEY_PASSWORD`) ou de um `keystore.properties`
local (ignorado pelo git), com `storeFile`, `storePassword`, `keyAlias` e `keyPassword`.
Sem chave configurada, `assembleRelease` gera um APK sem assinatura.

Publicar uma versão:
1. Aumentar `versionCode` e `versionName` em `app/build.gradle` e atualizar `docs/RELEASE_NOTES.md`.
2. Enviar uma tag `vX.Y.Z`. O workflow `release.yml` monta o keystore a partir dos
   secrets `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`
   e `ANDROID_KEY_PASSWORD`, roda os testes, confere a assinatura e publica `CuidarApp.apk`.

Guarde uma cópia do keystore e das senhas em local seguro: se a chave for perdida,
nenhuma versão futura poderá atualizar a instalada sem desinstalar.

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
- Diário de Saúde: validação das leituras, cálculo dos lembretes (fusos e horário de verão), estatísticas, orientações do profissional, situação das doses, montagem do relatório, repositório com Room em memória (salvar, editar, excluir, histórico, consentimento, permissão do cuidador, isolamento por paciente, exclusão LGPD), migração 1 -> 2 a partir do esquema exportado, agendamento de alarmes, notificações e respostas pela notificação (Robolectric)

### Teste instrumentado (aparelho ou emulador)
```bash
./gradlew connectedDebugAndroidTest
```
Gera o PDF real com `PdfDocument`/`PdfRenderer` e confere a pré-visualização e o compartilhamento via `FileProvider`.

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

