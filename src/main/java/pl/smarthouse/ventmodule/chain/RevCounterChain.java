package pl.smarthouse.ventmodule.chain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.smarthouse.smartchain.model.core.Chain;
import pl.smarthouse.smartchain.model.core.Step;
import pl.smarthouse.smartchain.service.ChainService;
import pl.smarthouse.smartchain.utils.PredicateUtils;
import pl.smarthouse.smartmodule.model.actors.type.pin.Pin;
import pl.smarthouse.smartmodule.model.actors.type.pin.PinCommandType;
import pl.smarthouse.ventmodule.configurations.Esp32ModuleConfig;
import pl.smarthouse.ventmodule.model.core.Fan;
import pl.smarthouse.ventmodule.service.VentModuleService;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import static pl.smarthouse.ventmodule.properties.FanProperties.FAN_INLET_REV_COUNTER;
import static pl.smarthouse.ventmodule.properties.FanProperties.FAN_OUTLET_REV_COUNTER;

@Service
@Slf4j
public class RevCounterChain {

  private final VentModuleService ventModuleService;
  private final Pin fanOutletRevCounter;
  private final Pin fanInletRevCounter;
  private Fan inletFan;
  private Fan outletFan;

  public RevCounterChain(
      @Autowired final ChainService chainService,
      @Autowired final Esp32ModuleConfig esp32ModuleConfig,
      @Autowired final VentModuleService ventModuleService) {
    this.fanOutletRevCounter =
        (Pin) esp32ModuleConfig.getConfiguration().getActorMap().getActor(FAN_OUTLET_REV_COUNTER);
    this.fanInletRevCounter =
        (Pin) esp32ModuleConfig.getConfiguration().getActorMap().getActor(FAN_INLET_REV_COUNTER);
    chainService.addChain(createChain());
    this.ventModuleService = ventModuleService;
  }

  private Chain createChain() {
    final Chain chain = new Chain("Fans revolution counter");
    // Wait for fun active or 1 minute and measure revolution
    chain.addStep(waitForFanRotatingAndMeasureFansRevolution());
    // Wait for response and after set NO_ACTION
    chain.addStep(waitForResponseAndSetNoActionStep());
    return chain;
  }

  private Step waitForFanRotatingAndMeasureFansRevolution() {
    return Step.builder()
        .conditionDescription("Wait for fun active or 1 minute")
        .condition(
            checkIfRequestStateChange()
                .and(PredicateUtils.delaySeconds(10))
                .or(PredicateUtils.delaySeconds(50)))
        .stepDescription("Read fans revolution")
        .action(readRevolutions())
        .build();
  }

  private Predicate<Step> checkIfRequestStateChange() {
    return step -> {
      final AtomicBoolean result = new AtomicBoolean(false);
      ventModuleService
          .getInletFan()
          .doOnNext(
              fan -> {
                inletFan = fan;
                if (fan.getCurrentSpeed() > 0) {
                  result.set(true);
                }
              })
          .flatMap(fan -> ventModuleService.getOutletFan())
          .doOnNext(
              fan -> {
                outletFan = fan;
                if (fan.getCurrentSpeed() > 0) {
                  result.set(true);
                }
              })
          .subscribe();
      return result.get();
    };
  }

  private Runnable readRevolutions() {
    return () -> {
      fanInletRevCounter.getCommandSet().setCommandType(PinCommandType.READ);
      fanOutletRevCounter.getCommandSet().setCommandType(PinCommandType.READ);
    };
  }

  private Step waitForResponseAndSetNoActionStep() {
    return Step.builder()
        .conditionDescription("Wait for response")
        .condition(waitForResponse())
        .stepDescription("Set NO_ACTION")
        .action(setNoAction())
        .build();
  }

  private Predicate<Step> waitForResponse() {
    return PredicateUtils.isResponseUpdated(fanInletRevCounter)
        .and(PredicateUtils.isResponseUpdated(fanOutletRevCounter));
  }

  private Runnable setNoAction() {
    return () -> {
      inletFan.setRevolution(fanInletRevCounter.getResponse().getCounter());
      outletFan.setRevolution(fanOutletRevCounter.getResponse().getCounter());
      fanInletRevCounter.getCommandSet().setCommandType(PinCommandType.NO_ACTION);
      fanOutletRevCounter.getCommandSet().setCommandType(PinCommandType.NO_ACTION);
    };
  }
}
