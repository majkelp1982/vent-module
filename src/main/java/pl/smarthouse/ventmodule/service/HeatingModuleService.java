package pl.smarthouse.ventmodule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import pl.smarthouse.ventmodule.configurations.HeatingModuleConfiguration;
import pl.smarthouse.ventmodule.model.dto.WaterRequirementDto;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class HeatingModuleService {
  private final HeatingModuleConfiguration heatingModuleConfiguration;

  public Mono<String> sendWaterRequirement(final WaterRequirementDto waterRequirementDto) {

    return heatingModuleConfiguration
        .getWebClient()
        .post()
        .uri("/action")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(waterRequirementDto)
        .exchangeToMono(
            clientResponse -> {
              if (clientResponse.statusCode().is2xxSuccessful()) {
                return clientResponse.bodyToMono(String.class);
              } else {
                return clientResponse.createException().flatMap(Mono::error);
              }
            })
        .onErrorResume(
            throwable -> {
              log.warn("Heating module connection problem. Message: {}", throwable.getMessage());
              return Mono.empty();
            });
  }
}
