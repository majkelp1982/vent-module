package pl.smarthouse.ventmodule.service;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.smarthouse.sharedobjects.dto.ventilation.enums.FunctionType;
import pl.smarthouse.ventmodule.utils.FanUtils;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class FansService {

  private final VentModuleService ventModuleService;
  private final VentModuleParamsService ventModuleParamsService;

  public Mono<Void> setFansRequiredPower() {
    final AtomicInteger inletRequiredGoalPower = new AtomicInteger(0);
    final AtomicInteger outletRequiredGoalPower = new AtomicInteger(0);
    return ventModuleService
        .getAllZones()
        .map(
            zoneDao -> {
              final int requiredPower = zoneDao.getRequiredPower();
              switch (zoneDao.getOperation()) {
                case AIR_EXCHANGE:
                  if (FunctionType.AIR_EXTRACT.equals(zoneDao.getFunctionType())) {
                    FanUtils.addAndValidateRequiredPower(outletRequiredGoalPower, requiredPower);
                  } else {

                    FanUtils.addAndValidateRequiredPower(inletRequiredGoalPower, requiredPower);
                  }
                  break;
                case HUMIDITY_ALERT:
                  FanUtils.addAndValidateRequiredPower(outletRequiredGoalPower, requiredPower);
                  break;
                case AIR_COOLING:
                case AIR_HEATING:
                case AIR_CONDITION:
                  FanUtils.addAndValidateRequiredPower(inletRequiredGoalPower, requiredPower);
                  break;
              }
              return zoneDao;
            })
        .collectList()
        .flatMap(
            zoneDaoList ->
                setRequiredPower(inletRequiredGoalPower.get(), outletRequiredGoalPower.get()));
  }

  private Mono<Void> setRequiredPower(
      final int inletRequiredGoalPower, final int outletRequiredGoalPower) {
    return ventModuleService
        .getFans()
        .flatMap(
            fans ->
                ventModuleParamsService
                    .getParams()
                    .map(
                        ventModuleParamsDto -> {
                          fans.getInlet()
                              .setGoalSpeed(
                                  FanUtils.validateRequiredGoalPower(
                                      ventModuleParamsDto,
                                      inletRequiredGoalPower,
                                      ventModuleParamsDto.getInletFanNightHoursMaxPower()));

                          fans.getOutlet()
                              .setGoalSpeed(
                                  FanUtils.validateRequiredGoalPower(
                                      ventModuleParamsDto,
                                      outletRequiredGoalPower,
                                      ventModuleParamsDto.getOutletFanNightHoursMaxPower()));
                          return fans;
                        })
                    .then());
  }
}
