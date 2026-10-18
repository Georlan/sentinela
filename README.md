# Sentinela

Aplicativo **pessoal** Android para registrar localizações offline e testar a retomada após reiniciar um Samsung Galaxy A17 5G.

## Estado do MVP 0.1

- App Kotlin nativo, sem backend, login ou conectividade de rede.
- Serviço de localização em primeiro plano **visível por notificação**, iniciado voluntariamente no app.
- Captura econômica via Google Play Services (prioridade balanceada, intervalo desejado de 2 minutos, distância mínima de 50 m, agrupamento permitido).
- SQLite privado com limite de 2.000 posições; tela mostra última leitura, horário e precisão.
- Opção explícita de retomada após reiniciar **somente com permissão "Permitir o tempo todo"**. O Android/One UI pode impedir ou atrasar a execução; requer validação real.
- CI público gera APK **debug** para teste no aparelho. Nenhum dado sai do celular nesta etapa.

**Não é um rastreador remoto ainda.** Quando o telefone está desligado não há localização; após formatação o app é removido. O projeto não contorna bloqueios de sistema, não oculta notificação de rastreamento nem garante rastreamento após boot.

## Compilação

O GitHub Actions publica `app-debug.apk` como artefato do workflow Android CI (aba Actions). Para compilar localmente, instale Android SDK plataforma 36, Java 17 e Gradle 8.13 e execute:

```bash
cd android
gradle :app:testDebugUnitTest :app:assembleDebug
```

O repositório inicial não contém Gradle Wrapper binário. O CI instala a distribuição declarada de Gradle. **Não** faça commit de `local.properties`, APK, keystore, tokens ou amostras reais.

## Testes no A17

Leia [docs/validacao-a17.md](docs/validacao-a17.md). Instale o APK, conceda localização precisa, inicie pelo botão e confirme a notificação persistente. A opção de boot exige habilitar localização em segundo plano nas configurações do Android.

## Próximas etapas

1. Medir consumo e confiabilidade em modo Economia de energia/One UI.
2. Adicionar fila e envio seguro de coordenadas para backend autenticado.
3. Construir painel privado de última posição e histórico no notebook.
4. Implementar comandos autenticados, check-in e alertas com proteção contra acesso indevido.

O repositório é **público**; não publique logs de localização, capturas com coordenadas, chaves de assinatura ou credenciais.