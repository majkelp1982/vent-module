package pl.smarthouse.ventmodule.error;

import static pl.smarthouse.ventmodule.properties.ForcedAirSystemExchangerProperties.*;

import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import pl.smarthouse.smartmodule.utils.errorpredictions.Ds18b20ErrorPredictionsUtils;
import pl.smarthouse.smartmonitoring.service.ErrorHandlingService;
import pl.smarthouse.ventmodule.service.VentModuleService;

@Configuration
@RequiredArgsConstructor
public class Ds18b20ErrorPredictions {

  private final VentModuleService ventModuleService;
  private final ErrorHandlingService errorHandlingService;

  @PostConstruct
  public void postConstructor() {
    Ds18b20ErrorPredictionsUtils.setDs180b20SensorsErrorPredictions(
        errorHandlingService,
        EXCHANGER_WATTER_IN,
        ventModuleService.getForcedAirSystemExchanger()::getWatterIn);
    Ds18b20ErrorPredictionsUtils.setDs180b20SensorsErrorPredictions(
        errorHandlingService,
        EXCHANGER_WATTER_OUT,
        ventModuleService.getForcedAirSystemExchanger()::getWatterOut);
    Ds18b20ErrorPredictionsUtils.setDs180b20SensorsErrorPredictions(
        errorHandlingService,
        EXCHANGER_AIR_IN,
        ventModuleService.getForcedAirSystemExchanger()::getAirIn);
    Ds18b20ErrorPredictionsUtils.setDs180b20SensorsErrorPredictions(
        errorHandlingService,
        EXCHANGER_AIR_OUT,
        ventModuleService.getForcedAirSystemExchanger()::getAirOut);
  }
}
