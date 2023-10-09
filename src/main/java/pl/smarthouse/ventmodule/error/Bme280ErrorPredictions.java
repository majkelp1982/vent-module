package pl.smarthouse.ventmodule.error;

import static pl.smarthouse.ventmodule.properties.AirExchangerProperties.*;

import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import pl.smarthouse.smartmodule.utils.errorpredictions.Bme280ErrorPredictionsUtils;
import pl.smarthouse.smartmonitoring.service.ErrorHandlingService;
import pl.smarthouse.ventmodule.service.VentModuleService;

@Configuration
@RequiredArgsConstructor
public class Bme280ErrorPredictions {

  private final VentModuleService ventModuleService;
  private final ErrorHandlingService errorHandlingService;

  @PostConstruct
  public void postConstructor() {
    Bme280ErrorPredictionsUtils.setBme280SensorErrorPredictions(
        errorHandlingService, BME280_INLET, ventModuleService.getAirExchanger()::getInlet);
    Bme280ErrorPredictionsUtils.setBme280SensorErrorPredictions(
        errorHandlingService, BME280_OUTLET, ventModuleService.getAirExchanger()::getOutlet);
    Bme280ErrorPredictionsUtils.setBme280SensorErrorPredictions(
        errorHandlingService, BME280_FRESH_AIR, ventModuleService.getAirExchanger()::getFreshAir);
    Bme280ErrorPredictionsUtils.setBme280SensorErrorPredictions(
        errorHandlingService, BME280_USED_AIR, ventModuleService.getAirExchanger()::getUserAir);
  }
}
