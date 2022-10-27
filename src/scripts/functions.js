function getCurrentWeather(lat, lon){
    var apiKey = $secrets.get("weatherKey");
    var response = $http.get("http://api.openweathermap.org/data/2.5/weather?appid=${appid}&lang=${lang}&units=${units}&lat=${lat}&lon=${lon}",{
        timeout: 10000,
        query: {
            appid: apiKey,
            lang: "ru",
            units: "metric",
            lat: lat,
            lon: lon,
        }
    });
    
    if(!response.data && !response.isOk){
        return false;
    }
    
    var weather = {};
    weather.temp = response.data.main.temp;
    weather.descr = response.data.weather[0].description;
    return weather;
}