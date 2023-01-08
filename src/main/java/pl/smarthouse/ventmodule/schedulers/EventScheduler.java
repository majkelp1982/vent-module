package pl.smarthouse.ventmodule.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.smarthouse.smartmodule.services.ModuleService;
import pl.smarthouse.ventmodule.configurations.ModuleConfig;

@EnableScheduling
@RequiredArgsConstructor
@Service
@Slf4j
public class EventScheduler {
  private final ModuleService moduleService;
  private final ModuleConfig moduleConfig;

  @Scheduled(fixedDelay = 10000)
  public void eventScheduler() {
    moduleService.exchange().subscribe();
    //    final BME280 bme280Intake = (BME280) moduleConfig.getActor(ModuleConfig.BME280_INTAKE);
    //    bme280Intake.setCommandSet(new BME280CommandSet(BME280CommandType.READ));

    //    final BME280 bme280Outlet = (BME280) moduleConfig.getActor(ModuleConfig.BME280_OUTLET);
    //    bme280Outlet.setCommandSet(new BME280CommandSet(BME280CommandType.READ));

    //    final BME280 bme280FreshAir = (BME280)
    // moduleConfig.getActor(ModuleConfig.BME280_FRESH_AIR);
    //    bme280FreshAir.setCommandSet(new BME280CommandSet(BME280CommandType.READ));

    //    final BME280 bme280UsedAir = (BME280) moduleConfig.getActor(ModuleConfig.BME280_USED_AIR);
    //    bme280UsedAir.setCommandSet(new BME280CommandSet(BME280CommandType.READ));
  }
}
