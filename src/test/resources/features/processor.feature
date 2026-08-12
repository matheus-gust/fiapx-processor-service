Feature: Processamento de videos

  Scenario: Processamento bem-sucedido de um video
    Given que existe uma mensagem de processamento para o video com email "user@test.com"
    When o processador consome a mensagem
    Then o status do video e atualizado para "DONE"
    And nenhuma notificacao de erro e enviada

  Scenario: Falha no processamento notifica o usuario
    Given que existe uma mensagem de processamento com video invalido
    When o processador tenta processar e falha
    Then o status do video e atualizado para "ERROR"
    And uma notificacao de erro e publicada para "user@test.com"
