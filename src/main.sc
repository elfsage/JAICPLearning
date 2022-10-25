require: slotfilling/slotFilling.sc
  module = sys.zb-common

require: patterns.sc
  module = sys.zb-common

require: localPatterns.sc

theme: /

    state: Start
        q!: $regex</start>
        q: ($hi/$hello)
        random:
            a: Здравствуйте!
            a: Добрый день!
            a: Приветствую!
        go!: /Service/SuggestHelp

    state: NoMatch
        event!: noMatch
        a: Простите, я не понял. Переформулируйте, пожалуйста, ваш запрос.
        
theme: /Service
    
    state: SuggestHelp
        a: Давайте я помогу вам купить билеты на самолет, хорошо?
        buttons:
            "Да"
            "Нет"
        
        state: Accepted
            q: (да/давай/хорошо)
            a: Отлично!
            
        state: Rejected
            q: (нет/не надо)
            a: Боюсь, что ничего другого я пока предложить не могу.