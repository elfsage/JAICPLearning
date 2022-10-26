require: slotfilling/slotFilling.sc
  module = sys.zb-common

require: patterns.sc
  module = sys.zb-common

require: localPatterns.sc

init:
    bind("postProcess", function($context) {
        log("%%%%%" + $context.currentState)
        $context.session.lastState = $context.currentState;
    })

theme: /
    
    state: StartIntent
        intent: /привет
        a: {{ $context.intent.answer }}

    state: Start
        q!: $regex</start>
        q: $regex</start> || fromState = /Phone/Ask
        # q: ($hi/$hello)
        script:
            $jsapi.startSession()
        random:
            a: Здравствуйте!
            a: Добрый день!
            a: Приветствую!
        a: Меня зовут {{ capitalize($injector.botName) }}
        script:
            $response.replies =  $response.replies || [];
            $response.replies.push({
                type: "image",
                imageUrl: "https://trendymen.ru/images/article1/120294/attachments/44.jpg",
                text: "Самолетик"
                })
        go!: /Service/SuggestHelp

    state: Reset
        q!: $regex</reset>
        script:
            $client = {};
            $session = {};

    state: NoMatch || noContext = true
        event!: noMatch
        a: Простите, я не понял. Переформулируйте, пожалуйста, ваш запрос.
        go!: {{ $session.lastState }}
        
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
            if: $client.phone
                go!: /Phone/Confirm
            else:
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
            $session.suggestedPhone = $parseTree._phone || $client.phone;
        a: Ваш номер {{ $session.suggestedPhone }}, верно?
        buttons:
            "Да"
            "Нет"
        
        state: Yes
            q: (да/верно)
            script:
                $client.phone = $session.suggestedPhone
            a: Отлично!
            go!: /Discount/Inform
            
        state: No
            q: (нет/не верно)
            go!: /Phone/Ask
            
theme: /Discount
    
    state: Inform
        script:
            $temp.date = $jsapi.dateForZone("Europe/Samara","dd.MM");
            
            var answer = "Вам не очень крупно повезло!"
            $reactions.answer(answer);
        a: Сегодня {{ $temp.date }} у нас скидка 5%