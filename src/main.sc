require: slotfilling/slotFilling.sc
  module = sys.zb-common

require: patterns.sc
  module = sys.zb-common

require: localPatterns.sc

require: city/cities-ru.csv
    module = sys.zb-common
    name = Cities
    var = Cities

require: dict/discount.yaml
    var = discountInfo
    
require: scripts/functions.js

init:
    bind("postProcess", function($context){
        log("+++++++" + $context.currentState);
        $context.session.lastState = $context.currentState;
    });
    
    $global.$converters = $global.$converters || {};
    
    $global.$converters.cityConverters = function (parseTree){
        var id = parseTree.Cities[0].value;
        return Cities[id].value;    
    }
            
theme: /
    
    state: Start
        q!: $regex</start>
        q: ($hi/$hello)
        script:
            $jsapi.startSession()
        random:
            a: Здравствуйте!
            a: Добрый день!
            a: Приветствую!
        a: Меня зовут {{ capitalize($injector.botName) }}.
        script:
            $response.replies = $response.replies || [];
            $response.replies.push({
                type: "image",
                imageUrl: "https://st.depositphotos.com/1144687/3421/i/600/depositphotos_34217943-stock-photo-airport-interior.jpg",
                text: "Аэропорт"})
        go!: /Service/SuggestHelp

    state: Reset
        q!: $regex</reset>
        script: 
            $client = {};
            $session = {};
    
    state: NoMatch || noContext = true
        event!: noMatch
        a: Простите, я не понял. Переформулируйте, пожалуйста, ваш запрос.
        go!: {{$session.lastState}}
        
theme: /Service
    
    state: SuggestHelp
        q: отмена || fromState = /Phone/Ask
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
        a: Для продолжения напишите, пожалуйста, ваш номер телефона в формате 79000000000
        buttons:
            "Отмена"
        
        state: GetPhone
            q: $phone 
            a: Спасибо.
            script:
                log("@@@@" + toPrettyString($parseTree));
            #a: {{$parseTree._phone}}
            go!: /Phone/Confirm
                
        state: LocalCatchAll
            event!: noMatch
            a: Напишите пожалйста номер телефона.
            
    state: Confirm
        script:
            $session.probablyPhone = $parseTree._phone || $client.phone;
        a: Ваш номер телефона {{$session.probablyPhone}}, верно?
        buttons:
            "Да"
            "Нет"
        
        state: Yes
            q: (да/давай/хорошо)
            script:
                $client.phone = $session.probablyPhone;
            a: Хорошо
            go!: /Discount/Inform
            
        state: No
            q: (нет/не надо)
            go: /Phone/Ask
            
theme: /Discount
    
    state: Inform
        script:
        
            var nowDayOfWeek = $jsapi.dateForZone("Europe/Moscow","EEEE");
            var discount = discountInfo[nowDayOfWeek];
            if (discount){
                $temp.date = $jsapi.dateForZone("Europe/Moscow","dd.MM");
                var answer = "Вам крупно повезло. Сегодня " + $temp.date + " действует скидка"
                $reactions.answer(answer);
                $reactions.answer(discount);
            }
            
        go!: /Travel/Departure
        
theme: /Travel
    
    state: Departure
        a:Назовите пожалуйста город отправления
            
        state: Get
            q: * $City *
            script:
                log("///////////////////" + toPrettyString($parseTree.City));
                $session.departureCity = $parseTree._City;
            a: итак, город отправления: {{$session.departureCity.name}}
            go!: /Travel/Destination
            
    state: Destination
        a:Назовите пожалуйста город прибытия
            
        state: Get
            q: * $City *
            script:
                log("///////////////////" + toPrettyString($parseTree.City));
                $session.destinationCity = $parseTree._City;
            a: итак, город прибытия: {{$session.destinationCity.name}} 
            go!: /Weather/Current
            
    
    state: Ticket
        intent!: /Ticket
        script:
            log("ssssssssssssssssss" + toPrettyString($parseTree));
            $session.departureCity = capitalize($nlp.inflect($parseTree._departure, "gent"));
            $session.destinationCity = capitalize($nlp.inflect($parseTree._destination, "accs"));
            $session.time = $parseTree._time.day + "/" + $parseTree._time.month + "/" + $parseTree._time.year;
        a: Вы хотите купить билет из {{$session.departureCity}} в {{$session.destinationCity}} {{$session.time}}.
        
    state: Match
        event!: match
        a: Знаю ответ:{{$context.intent.answer}}   
        
theme: /Weather
    
    state: Current
        script:
            $temp.weather = getCurrentWeather($session.destinationCity.lat, $session.destinationCity.lon);
        if: $temp.weather
            a: Сейчас в городе {{$session.destinationCity.name}} {{$temp.weather.descr}}, {{$temp.weather.temp}}C