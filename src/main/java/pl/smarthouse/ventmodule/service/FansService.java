package pl.smarthouse.ventmodule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.smarthouse.ventmodule.enums.FunctionType;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class FansService {

  private final VentModuleService ventModuleService;

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
                  if (FunctionType.INLET.equals(zoneDao.getFunctionType())) {
                    addAndValidateRequiredPower(inletRequiredGoalPower, requiredPower);
                  } else {

                    addAndValidateRequiredPower(outletRequiredGoalPower, requiredPower);
                  }
                  break;
                case HUMIDITY_ALERT:
                  addAndValidateRequiredPower(outletRequiredGoalPower, requiredPower);
                  break;
                case COOLING:
                case HEATING:
                  addAndValidateRequiredPower(inletRequiredGoalPower, requiredPower);
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
        .getInletFan()
        .map(
            inletFan -> {
              inletFan.setGoalSpeed(inletRequiredGoalPower);
              return inletFan;
            })
        .flatMap(ignoreFan -> ventModuleService.getOutletFan())
        .map(
            outletFan -> {
              outletFan.setGoalSpeed(outletRequiredGoalPower);
              return outletFan;
            })
        .flatMap(ignoreFan -> Mono.empty());
  }

  private void addAndValidateRequiredPower(final AtomicInteger goalPower, final int value) {
    goalPower.addAndGet(value);
    if (goalPower.get() > 100) {
      goalPower.set(100);
    } else if (goalPower.get() < 0) {
      goalPower.set(0);
    }
  }
}
