patterns:
    $hi = (привет*/здравствуй*/добр* (день/утр*/вечер*/ноч*)/хай/hi/hello)
    $phone = $regex<79\d{9}>
    $City = $entity<Cities> || converter = $converters.cityConverters
