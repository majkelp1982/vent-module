package pl.smarthouse.ventmodule.chain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.smarthouse.smartchain.model.core.Chain;
import pl.smarthouse.smartchain.model.core.Step;
import pl.smarthouse.smartchain.service.ChainService;
import pl.smarthouse.smartchain.utils.PredicateUtils;
import pl.smarthouse.smartmodule.model.actors.type.ds18b20.Ds18b20;
import pl.smarthouse.smartmodule.model.actors.type.ds18b20.Ds18b20CommandType;
import pl.smarthouse.smartmodule.model.actors.type.ds18b20.Ds18b20Utils;
import pl.smarthouse.ventmodule.configurations.Esp32ModuleConfig;
import pl.smarthouse.ventmodule.service.VentModuleService;

import static org.springframework.data.util.Predicates.negate;
import static pl.smarthouse.ventmodule.properties.ActiveHeatingCoolingExchangerProperties.*;

@Service
public class Ds18b20Chain {
  private final VentModuleService ventModuleService;
  private final Ds18b20 airExchanger;

  public Ds18b20Chain(
      @Autowired final VentModuleService ventModuleService,
      @Autowired final ChainService chainService,
      @Autowired final Esp32ModuleConfig esp32ModuleConfig) {
    this.ventModuleService = ventModuleService;
    airExchanger = (Ds18b20) esp32ModuleConfig.getConfiguration().getActorMap().getActor(EXCHANGER);
    final Chain chain = createChain();
    chainService.addChain(chain);
  }

  private Chain createChain() {
    final Chain chain = new Chain("Read DS18B20 sensors");
    // Wait 10 seconds and read values from each sensor type DS18B20
    chain.addStep(createStep1());
    // Wait until command read successful and set command to NO_ACTION for all
    chain.addStep(createStep2());
    return chain;
  }

  private Step createStep1() {

    return Step.builder()
        .stepDescription("Read values from each sensor type DS18B20")
        .conditionDescription("Waiting 30 seconds")
        .condition(PredicateUtils.delaySeconds(30))
        .action(createActionStep1())
        .build();
  }

  private Runnable createActionStep1() {
    return () -> {
      airExchanger.getCommandSet().setCommandType(Ds18b20CommandType.READ);
      airExchanger
          .getCommandSet()
          .setValue(
              Ds18b20Utils.getDs18b20Command(
                  EXCHANGER_WATTER_IN, EXCHANGER_WATTER_OUT, EXCHANGER_AIR_IN, EXCHANGER_AIR_OUT));
    };
  }

  private Step createStep2() {
    return Step.builder()
        .stepDescription("Set DS18B20 commands to NO_ACTION")
        .conditionDescription("Wait until command read successful")
        .condition(
            PredicateUtils.isResponseUpdated(airExchanger)
                .and(negate(PredicateUtils.isErrorOnDs18b20Group(airExchanger.getResponse()))))
        .action(createActionStep2())
        .build();
  }

  private Runnable createActionStep2() {
    return () -> {
      ventModuleService
          .getVentModuleDao()
          .map(
              ventModuleDao -> {
                ventModuleDao
                    .getActiveHeatingCoolingExchanger()
                    .setWatterIn(airExchanger.getResponse().getSensorResult(EXCHANGER_WATTER_IN));
                ventModuleDao
                    .getActiveHeatingCoolingExchanger()
                    .setWatterOut(airExchanger.getResponse().getSensorResult(EXCHANGER_WATTER_OUT));
                ventModuleDao
                    .getActiveHeatingCoolingExchanger()
                    .setAirIn(airExchanger.getResponse().getSensorResult(EXCHANGER_AIR_IN));
                ventModuleDao
                    .getActiveHeatingCoolingExchanger()
                    .setAirOut(airExchanger.getResponse().getSensorResult(EXCHANGER_AIR_OUT));
                return ventModuleDao;
              })
          .subscribe();

      airExchanger.getCommandSet().setCommandType(Ds18b20CommandType.NO_ACTION);
    };
  }
}
