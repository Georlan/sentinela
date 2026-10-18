# Plano de homologacao no Samsung Galaxy A17 5G

## Preparacao

- Baixe o APK na aba Actions do GitHub (artefato do Android CI).
- Instale em aparelho proprio, confiavel, preferencialmente sem expor coordenadas em capturas de tela.
- Habilite localizacao e autorize "Durante o uso" + "Localizacao precisa" no Android.
- Abra o app e toque em **Iniciar monitoramento**; confirme a notificacao persistente.
- Aguarde alguns minutos **ao ar livre** e clique em "Atualizar ultima posicao". Coordenadas e horarios devem aparecer.
- Toque em "Parar monitoramento"; confirme que a notificacao desaparece.

## Testes de resistencia

1. Com monitoramento ativo, bloqueie a tela por 30 min e confirme se surgiram pontos novos.
2. Repita com Economia de energia ativa. Meça consumo em 1 h e 6 h; registre apenas porcentagem e contagem de pontos, **sem publicar coordenadas**.
3. Abra configuracoes do app > Permissoes > Localizacao > **Permitir o tempo todo**; depois ative "Retomar apos reiniciar".
4. Reinicie o aparelho. Espere o desbloqueio inicial e verifique se aparece notificacao e uma nova posicao.
5. Repita apos deixar o aparelho parado e em movimento. Observe eventuais restricoes da One UI.
6. Revogue a permissao em segundo plano, reinicie e confirme que o app **nao** tenta rastrear silenciosamente.
7. Teste com GPS desligado e sem internet: neste MVP nao existe backend e nenhum ponto sera enviado remotamente.

## Aceitacao

- Build + testes verdes no GitHub.
- APK instalavel, permissoes solicitadas, notificacao visivel e botao Parar funcional.
- Pontos locais com timestamp e precisao; sem rede.
- Retomada apos boot **medida**, e nao presumida.
- Comparar bateria contra uma linha de base sem Sentinela. Se consumo for alto, ajustar politica de coleta.

## Limites conhecidos

- O sistema pode interromper, atrasar ou impedir atualizacoes mesmo com o app autorizado.
- Retomada de service do BOOT_COMPLETED depende da versao Android e permissoes de localizacao sempre ativa.
- Nada inicia antes do primeiro desbloqueio apos reiniciar nesta fase (armazenamento criptografado do usuario).
- O app nao sobrevive a restauracao de fabrica, desinstalacao ou desligamento.
- O modo economico utiliza Google Play Services; o aparelho precisa deles disponiveis.
- Nao ofereca estes dados como prova de precisao sem considerar a margem de erro.
