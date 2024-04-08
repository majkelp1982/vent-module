package pl.smarthouse.ventmodule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import pl.smarthouse.sharedobjects.dto.weather.WeatherModuleDto;
import pl.smarthouse.ventmodule.configurations.WeatherModuleConfiguration;
import pl.smarthouse.ventmodule.exceptions.WeatherModuleServiceResponseException;
import reactor.core.publisher.Mono;

@EnableScheduling
@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherModuleService {
  private static final String WEATHER_MODULE_TYPE = "WEATHER";
  private final WeatherModuleConfiguration weatherModuleConfiguration;
  private final ModuleManagerService moduleManagerService;
  private final String SERVICE_ADDRESS_REGEX =
      "^(?:http:\\/\\/)?\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{1,5}$";
  private WeatherModuleDto weatherModuleMetaData;

  @Scheduled(fixedDelay = 30000)
  private void refreshWeatherModuleDataScheduler() {
    getWeatherModuleMetaData()
        .doOnNext(weatherModuleDto -> weatherModuleMetaData = weatherModuleDto)
        .doOnNext(weatherModuleDto -> log.info("Retrieved module data: {}", weatherModuleDto))
        .block();
  }

  private Mono<String> retrieveWeatherServiceBaseUrl() {
    return Mono.justOrEmpty(weatherModuleConfiguration.getBaseUrl())
        .switchIfEmpty(
            Mono.defer(() -> moduleManagerService.getServiceAddress(WEATHER_MODULE_TYPE)))
        .flatMap(
            baseUrl -> {
              if (!baseUrl.matches(SERVICE_ADDRESS_REGEX)) {
                Mono.error(
                    new IllegalArgumentException(
                        String.format(
                            "Base url have to contain http address. Current: %s", baseUrl)));
              }
              weatherModuleConfiguration.setBaseUrl(baseUrl);
              return Mono.just(baseUrl);
            });
  }

  private Mono<WeatherModuleDto> getWeatherModuleMetaData() {
    return retrieveWeatherServiceBaseUrl()
        .flatMap(signal -> Mono.just(weatherModuleConfiguration.getWebClient()))
        .flatMap(
            webClient ->
                webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder.path("/weather").build())
                    .exchangeToMono(this::processResponse))
        .doOnError(
            throwable -> {
              weatherModuleConfiguration.resetBaseUrl();
              log.error(
                  "Error occurred on getWeatherModuleMetaData. Reason: {}",
                  throwable.getMessage(),
                  throwable);
            });
  }

  private Mono<WeatherModuleDto> processResponse(final ClientResponse clientResponse) {
    if (clientResponse.statusCode().is2xxSuccessful()) {
      return clientResponse.bodyToMono(WeatherModuleDto.class);
    } else {
      return clientResponse
          .bodyToMono(String.class)
          .flatMap(
              response ->
                  Mono.error(
                      new WeatherModuleServiceResponseException(
                          clientResponse.statusCode(), response)));
    }
  }

  public WeatherModuleDto getWeatherMetadata() {
    return weatherModuleMetaData;
  }
}
