package pl.smarthouse.ventmodule.scheduler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.smarthouse.sharedobjects.enums.Operation;
import pl.smarthouse.ventmodule.model.dto.WaterRequirementDto;
import pl.smarthouse.ventmodule.service.HeatingModuleService;
import pl.smarthouse.ventmodule.service.ZoneService;
import reactor.core.publisher.Mono;

@EnableScheduling
@RequiredArgsConstructor
@Service
public class ExternalModuleScheduler {

  private final HeatingModuleService heatingModuleService;
  private final ZoneService zoneService;

  @Scheduled(initialDelay = 10000, fixedDelay = 10000)
  public void checkIfSendRequestToHeatingServiceRequired() {
    zoneService
        .getActiveZones()
        .flatMap(
            zoneNameZoneDtoHashMap -> {
              final List<Operation> coolingList =
                  List.of(Operation.AIR_COOLING, Operation.AIR_CONDITION);
              final List<Operation> heatingList = List.of(Operation.AIR_HEATING);

              if (zoneNameZoneDtoHashMap.values().stream()
                  .anyMatch(zoneDto -> coolingList.contains(zoneDto.getOperation()))) {
                return heatingModuleService.sendWaterRequirement(
                    WaterRequirementDto.builder().requiredColdWater(true).build());
              } else if (zoneNameZoneDtoHashMap.values().stream()
                  .anyMatch(zoneDto -> heatingList.contains(zoneDto.getOperation()))) {
                return heatingModuleService.sendWaterRequirement(
                    WaterRequirementDto.builder().requiredWarmWater(true).build());
              }
              return Mono.empty();
            })
        .subscribe();
  }
}
