package com.springapps.weatherapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.springapps.weatherapp.dto.CurrentWeatherDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

@Service
public class WeatherService {

    private RestTemplate restTemplate;

    private ObjectMapper objectMapper;

    private static final String BASE_URL = "https://api.tomorrow.io/v4/weather";


    @Value("${weather.api}")
    private String apiKey;


    @Autowired
    public WeatherService(RestTemplate restTemplate, ObjectMapper mapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = mapper;
    }

    public CurrentWeatherDTO getCurrentWeather (double lat, double lon) throws JsonProcessingException {
        String url = UriComponentsBuilder
                .fromUriString(BASE_URL+"/realtime")
                .queryParam("location", lat+","+lon)
                .queryParam("apikey", apiKey)
                .toUriString();


        String response = restTemplate.getForObject(url,String.class);
        JsonNode root = objectMapper.readTree(response);
        return mapFromJsonToCurrentWeatherDTO(root);
    }

    public List<CurrentWeatherDTO> getForecastWeather (double lat, double lon, String timesteps) throws JsonProcessingException {
        String url = UriComponentsBuilder
                .fromUriString(BASE_URL+"/forecast")
                .queryParam("location", lat+","+lon)
                .queryParam("timesteps", timesteps)
                .queryParam("apikey", apiKey)
                .queryParam("untis", "metric")
                .toUriString();


        String response = restTemplate.getForObject(url,String.class);
        JsonNode root = objectMapper.readTree(response);
        return mapFromJsonToForecastWeather(root);
    }

    public List<CurrentWeatherDTO> mapFromJsonToForecastWeather(JsonNode root){
        ArrayNode dailyForecasts = (ArrayNode) root.path("timelines").path("daily");
        List<CurrentWeatherDTO > result = new ArrayList<>();
        for (JsonNode jsonNode: dailyForecasts){
            result.add(mapFromFOrecastNodeToCurrentWreatherDTO(jsonNode));
        }
        return result;
    }

    public CurrentWeatherDTO mapFromFOrecastNodeToCurrentWreatherDTO(JsonNode jsonNode){
        Double humidity = jsonNode.path("values").path("humidityAvg").asDouble();
        Double temperature = jsonNode.path("values").path("temperatureAvg").asDouble();
        Double feelsLikeTemperature = jsonNode.path("values").path("temperatureApparentAvg").asDouble();
        String dateString = jsonNode.path("time").asText();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        LocalDateTime dateTime = LocalDateTime.parse(dateString, dtf);
        return new CurrentWeatherDTO(temperature, feelsLikeTemperature, humidity, dateTime);
    }
    public CurrentWeatherDTO mapFromJsonToCurrentWeatherDTO (JsonNode root){
        Double humidity = root.path("data").path("values").path("humidity").asDouble();
        Double temperature = root.path("data").path("values").path("temperature").asDouble();
        Double feelsLikeTemperature = root.path("data").path("values").path("temperatureApparent").asDouble();
        String dateString = root.path("data").path("time").asText();
        LocalDateTime dateTime = LocalDateTime.parse(dateString);
        return new CurrentWeatherDTO(temperature, feelsLikeTemperature, humidity, dateTime);
    }

}
