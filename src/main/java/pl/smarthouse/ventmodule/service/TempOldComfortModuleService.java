package pl.smarthouse.ventmodule.service;

import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import pl.smarthouse.sharedobjects.enums.Operation;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.ventmodule.enums.FunctionType;
import pl.smarthouse.ventmodule.model.core.TempComfortZone;
import reactor.core.publisher.Mono;

@Service
@EnableScheduling
public class TempOldComfortModuleService {
  private final WebClient webClient;
  private final VentModuleService ventModuleService;
  HashMap<ZoneName, TempComfortZone> comfortZoneHashMap = new HashMap<>();
  int humidityThreshold = 75;
  double forcedAirSystemThreshold = 2.0;

  public TempOldComfortModuleService(@Autowired final VentModuleService ventModuleService) {
    this.ventModuleService = ventModuleService;
    this.webClient = WebClient.create();
    comfortZoneHashMap.put(ZoneName.SALON, new TempComfortZone(true));
    comfortZoneHashMap.put(ZoneName.PRALNIA, new TempComfortZone(true));
    comfortZoneHashMap.put(ZoneName.LAZ_DOL, new TempComfortZone(true));
    comfortZoneHashMap.put(ZoneName.RODZICE, new TempComfortZone(true));
    comfortZoneHashMap.put(ZoneName.NATALIA, new TempComfortZone(true));
    comfortZoneHashMap.put(ZoneName.KAROLINA, new TempComfortZone(true));
    comfortZoneHashMap.put(ZoneName.LAZ_GORA, new TempComfortZone(true));
  }

  public HashMap<String, Object> getTempComfortZones() {
    final HashMap<String, Object> tempMap = new HashMap<>();
    tempMap.put("comfortZoneHashMap", comfortZoneHashMap);
    tempMap.put("humidityThreshold", humidityThreshold);
    tempMap.put("forcedAirSystemThreshold", forcedAirSystemThreshold);
    return tempMap;
  }

  public void setHumidityThreshold(final int threshold) {
    humidityThreshold = threshold;
  }

  public void setForcedAirSystemEnabled(final ZoneName zoneName, final boolean enabled) {
    comfortZoneHashMap.get(zoneName).setForcedAirSystemEnabled(enabled);
  }

  public void setForcedAirSystemThreshold(final double threshold) {
    forcedAirSystemThreshold = threshold;
  }

  @Scheduled(initialDelay = 10000, fixedDelay = 10000)
  public void checkZonesScheduler() {
    comfortZoneHashMap.forEach(
        (zoneName, tempComfortZone) -> {
          if (tempComfortZone.isEnabled()) {
            handleHumidity(zoneName, tempComfortZone);
            handleForcedAirSystem(zoneName, tempComfortZone);
          }
        });
  }

  private void handleHumidity(final ZoneName zoneName, final TempComfortZone tempComfortZone) {
    ventModuleService
        .getZone(zoneName)
        .filter(zoneDao -> FunctionType.INLET.equals(zoneDao.getFunctionType()))
        .flatMap(
            zoneDao -> {
              if (tempComfortZone.getHumidity() > humidityThreshold) {
                return webClient
                    .post()
                    .uri(
                        uriBuilder ->
                            uriBuilder
                                .path("localhost:9999/zones/" + zoneName + "/operation")
                                .queryParam("operation", Operation.HUMIDITY_ALERT)
                                .queryParam(
                                    "requestPower",
                                    (tempComfortZone.getHumidity() > (humidityThreshold + 10)
                                        ? 100
                                        : 75))
                                .build())
                    .exchangeToMono(this::processResponse);
              }
              return Mono.empty();
            })
        .subscribe();
  }

  private void handleForcedAirSystem(
      final ZoneName zoneName, final TempComfortZone tempComfortZone) {
    ventModuleService
        .getZone(zoneName)
        .filter(zoneDao -> FunctionType.OUTLET.equals(zoneDao.getFunctionType()))
        .flatMap(
            zoneDao -> {
              if (tempComfortZone.isForcedAirSystemEnabled()) {
                if (tempComfortZone.getTemperature()
                    >= (tempComfortZone.getRequiredTemperature() + forcedAirSystemThreshold)) {
                  return webClient
                      .post()
                      .uri(
                          uriBuilder ->
                              uriBuilder
                                  .path("localhost:9999/zones/" + zoneName + "/operation")
                                  .queryParam("operation", Operation.COOLING)
                                  .queryParam("requestPower", Integer.toString(30))
                                  .build())
                      .exchangeToMono(this::processResponse);
                }
                if (tempComfortZone.getTemperature()
                    <= (tempComfortZone.getRequiredTemperature() - forcedAirSystemThreshold)) {
                  return webClient
                      .post()
                      .uri(
                          uriBuilder ->
                              uriBuilder
                                  .path("localhost:9999/zones/" + zoneName + "/operation")
                                  .queryParam("operation", Operation.HEATING)
                                  .queryParam("requestPower", Integer.toString(30))
                                  .build())
                      .exchangeToMono(this::processResponse);
                }
              }
              return Mono.empty();
            })
        .subscribe();
  }

  private Mono<String> processResponse(final ClientResponse clientResponse) {
    if (clientResponse.statusCode().is2xxSuccessful()) {
      return Mono.empty();
    } else {
      return clientResponse.createException().flatMap(Mono::error);
    }
  }

  public void setOldComfortModuleParameters(final int[] packetData) {
    comfortZoneHashMap.get(ZoneName.SALON).setTemperature(packetData[3] + packetData[4] / 10.0);
    comfortZoneHashMap.get(ZoneName.SALON).setRequiredTemperature(packetData[5] / 2.0);
    comfortZoneHashMap.get(ZoneName.SALON).setHumidity(packetData[6]);

    comfortZoneHashMap.get(ZoneName.PRALNIA).setTemperature(packetData[7] + packetData[8] / 10.0);
    comfortZoneHashMap.get(ZoneName.PRALNIA).setRequiredTemperature(packetData[9] / 2.0);
    comfortZoneHashMap.get(ZoneName.PRALNIA).setHumidity(packetData[10]);

    comfortZoneHashMap.get(ZoneName.LAZ_DOL).setTemperature(packetData[11] + packetData[12] / 10.0);
    comfortZoneHashMap.get(ZoneName.LAZ_DOL).setRequiredTemperature(packetData[13] / 2.0);
    comfortZoneHashMap.get(ZoneName.LAZ_DOL).setHumidity(packetData[14]);

    comfortZoneHashMap.get(ZoneName.RODZICE).setTemperature(packetData[15] + packetData[16] / 10.0);
    comfortZoneHashMap.get(ZoneName.RODZICE).setRequiredTemperature(packetData[17] / 2.0);
    comfortZoneHashMap.get(ZoneName.RODZICE).setHumidity(packetData[18]);

    comfortZoneHashMap.get(ZoneName.NATALIA).setTemperature(packetData[19] + packetData[20] / 10.0);
    comfortZoneHashMap.get(ZoneName.NATALIA).setRequiredTemperature(packetData[21] / 2.0);
    comfortZoneHashMap.get(ZoneName.NATALIA).setHumidity(packetData[22]);

    comfortZoneHashMap
        .get(ZoneName.KAROLINA)
        .setTemperature(packetData[23] + packetData[24] / 10.0);
    comfortZoneHashMap.get(ZoneName.KAROLINA).setRequiredTemperature(packetData[25] / 2.0);
    comfortZoneHashMap.get(ZoneName.KAROLINA).setHumidity(packetData[26]);

    comfortZoneHashMap
        .get(ZoneName.LAZ_GORA)
        .setTemperature(packetData[27] + packetData[28] / 10.0);
    comfortZoneHashMap.get(ZoneName.LAZ_GORA).setRequiredTemperature(packetData[29] / 2.0);
    comfortZoneHashMap.get(ZoneName.LAZ_GORA).setHumidity(packetData[30]);
  }
}
