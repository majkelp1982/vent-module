package pl.smarthouse.ventmodule.service;

import java.util.HashMap;
import java.util.List;
import lombok.Setter;
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
import pl.smarthouse.ventmodule.model.dto.ZoneDto;
import reactor.core.publisher.Mono;

@Service
@Setter
@EnableScheduling
public class TempOldComfortModuleService {
  private final WebClient webClient;
  private final VentModuleService ventModuleService;
  HashMap<ZoneName, TempComfortZone> comfortZoneHashMap = new HashMap<>();
  private int humidityThresholdLow = 70;
  private int humidityThresholdHigh = 75;
  private double heatingThresholdLow = -2.5;
  private double heatingThresholdHigh = -1.5;
  private double coolingThresholdHigh = 1.5;
  private double coolingThresholdLow = 1.0;
  private double airConditionThresholdHigh = 2.0;
  private double airConditionThresholdLow = 1.0;
  final TempHysteresisService tempHysteresisService =
      new TempHysteresisService(
          humidityThresholdLow,
          humidityThresholdHigh,
          heatingThresholdLow,
          heatingThresholdHigh,
          coolingThresholdHigh,
          coolingThresholdLow,
          airConditionThresholdHigh,
          airConditionThresholdLow);

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
    tempMap.put("humidityThresholdLow", humidityThresholdLow);
    tempMap.put("humidityThresholdHigh", humidityThresholdHigh);
    tempMap.put("heatingThresholdLow", heatingThresholdLow);
    tempMap.put("heatingThresholdHigh", heatingThresholdHigh);
    tempMap.put("coolingThresholdHigh", coolingThresholdHigh);
    tempMap.put("coolingThresholdLow", coolingThresholdLow);
    tempMap.put("airConditionThresholdHigh", airConditionThresholdHigh);
    tempMap.put("airConditionThresholdLow", airConditionThresholdLow);
    return tempMap;
  }

  public void setForcedAirSystemEnabled(final ZoneName zoneName, final boolean enabled) {
    comfortZoneHashMap.get(zoneName).setForcedAirSystemEnabled(enabled);
  }

  @Scheduled(initialDelay = 10000, fixedDelay = 10000)
  public void checkZonesScheduler() {
    comfortZoneHashMap.forEach(
        (zoneName, tempComfortZone) -> {
          if (tempComfortZone.isEnabled()) {
            handleOperations(zoneName, tempComfortZone);
          }
        });
  }

  private void handleOperations(final ZoneName zoneName, final TempComfortZone tempComfortZone) {
    if (!tempComfortZone.isForcedAirSystemEnabled()) {
      return;
    }

    // + -> to hot  - -> to cold
    final double deltaTemp =
        tempComfortZone.getTemperature() - tempComfortZone.getRequiredTemperature();

    final Operation calculatedOperation =
        tempHysteresisService.update(
            tempComfortZone.getCurrentOperation(), tempComfortZone.getHumidity(), deltaTemp);

    ventModuleService
        .getZone(zoneName)
        .flatMap(
            zoneDao -> {
              if (!FunctionType.INLET.equals(zoneDao.getFunctionType())
                  && Operation.HUMIDITY_ALERT.equals(calculatedOperation)) {
                return Mono.just(Operation.STANDBY);
              }

              final List<Operation> operationList =
                  List.of(Operation.COOLING, Operation.HEATING, Operation.AIR_CONDITION);
              if (!FunctionType.OUTLET.equals(zoneDao.getFunctionType())
                  && operationList.contains(calculatedOperation)) {
                return Mono.just(Operation.STANDBY);
              }
              return Mono.just(calculatedOperation);
            })
        .flatMap(
            resultOperation -> sendComfortZoneOperation(zoneName, resultOperation, tempComfortZone))
        .subscribe();
  }

  private Mono<ZoneDto> sendComfortZoneOperation(
      final ZoneName zoneName, final Operation operation, final TempComfortZone tempComfortZone) {
    final int requestPower = calculatePowerToRequest(operation, tempComfortZone);
    return webClient
        .post()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("localhost:9999/zones/" + zoneName + "/operation")
                    .queryParam("operation", operation)
                    .queryParam("requestPower", requestPower)
                    .build())
        .exchangeToMono(this::processResponse);
  }

  private int calculatePowerToRequest(
      final Operation operation, final TempComfortZone tempComfortZone) {
    if (Operation.HUMIDITY_ALERT.equals(operation)) {
      if (tempComfortZone.getHumidity() >= (humidityThresholdHigh + 10)) {
        return 100;
      } else {
        return 75;
      }
    }
    final List operationList = List.of(Operation.HEATING, Operation.COOLING);
    if (operationList.contains(operation)) {
      return 30;
    }
    if (Operation.AIR_CONDITION.equals(operation)) {
      return 50;
    }
    return 0;
  }

  private Mono<ZoneDto> processResponse(final ClientResponse clientResponse) {
    if (clientResponse.statusCode().is2xxSuccessful()) {
      return clientResponse.bodyToMono(ZoneDto.class);
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
