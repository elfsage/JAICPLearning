require: slotfilling/slotFilling.sc
  module = sys.zb-common

require: patterns.sc
  module = sys.zb-common

require: localPatterns.sc

theme: /

    state: Start
        q!: $regex</start>
        q: $regex</start> || fromState = /Phone/Ask
        q: ($hi/$hello)
        random:
            a: Здравствуйте!
            a: Добрый день!
            a: Приветствую!
        script:
            $response.replies =  $response.replies || [];
            $response.replies.push({
                type: "image",
                imageUrl: "https://trendymen.ru/images/article1/120294/attachments/44.jpg",
                text: "Самолетик"
                })
        go!: /Service/SuggestHelp

    state: NoMatch || noContext = true
        event!: noMatch
        a: Простите, я не понял. Переформулируйте, пожалуйста, ваш запрос.
        
theme: /Service
    
    state: SuggestHelp
        q: Передумал || fromState = /Phone/Ask
        a: Давайте я помогу вам купить билеты на самолет, хорошо?
        buttons:
            "Да"
            "Нет"
        
        state: Accepted
            q: (да/давай/хорошо)
            a: Отлично!
            go!: /Phone/Ask
            
        state: Rejected
            q: (нет/не надо)
            a: Боюсь, что ничего другого я пока предложить не могу.
            
theme: /Phone
    
    state: Ask || modal = true
        a: Пожалуйста, укажите номер телефона в формате +790000000000
        buttons:
            "79021234567"
        
        state: GetPhone
            q: $phone
            a: Спасибо
            script:
                log("@@@@" + toPrettyString($parseTree))
            go!: /Phone/Confirm
            
        state: LocalCatchAll
            event: noMatch
            a: Пожалуйста, укажите номер телефона
            buttons:
                "Передумал"
            
    state: Confirm
        script:
            $temp.phone = $parseTree._phone;
        a: Ваш номер {{ $temp.phone }}, верно?
        buttons:
            "Да"
            "Нет"
        
        state: Yes
            q: (да/верно)
            a: Отлично!
            
        state: No
            q: (нет/не верно)
            a: ...