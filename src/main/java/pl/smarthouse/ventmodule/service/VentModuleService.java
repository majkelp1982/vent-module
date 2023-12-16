package pl.smarthouse.ventmodule.service;

import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.smarthouse.sharedobjects.dto.ventilation.VentModuleDto;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.ventmodule.configurations.VentModuleConfiguration;
import pl.smarthouse.ventmodule.model.core.*;
import pl.smarthouse.ventmodule.model.dao.VentModuleDao;
import pl.smarthouse.ventmodule.model.dao.ZoneDao;
import pl.smarthouse.ventmodule.utils.ModelMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class VentModuleService {
  private final VentModuleConfiguration ventModuleConfiguration;

  public VentModuleDto getVentModule() {
    return ModelMapper.toVentModuleDto(ventModuleConfiguration.getVentModuleDao());
  }

  public VentModuleDao getVentModuleDao() {
    return ventModuleConfiguration.getVentModuleDao();
  }

  public AirExchanger getAirExchanger() {
    return ventModuleConfiguration.getVentModuleDao().getAirExchanger();
  }

  public ForcedAirSystemExchanger getForcedAirSystemExchanger() {
    return ventModuleConfiguration.getVentModuleDao().getForcedAirSystemExchanger();
  }

  public Mono<String> getModuleName() {
    return Mono.just(ventModuleConfiguration.getVentModuleDao().getModuleName());
  }

  public Mono<ZoneDao> getZone(final ZoneName zoneName) {
    return Mono.justOrEmpty(
        ventModuleConfiguration.getVentModuleDao().getZoneDaoHashMap().get(zoneName));
  }

  public Flux<ZoneDao> getAllZones() {
    return Flux.fromIterable(
            ventModuleConfiguration.getVentModuleDao().getZoneDaoHashMap().keySet())
        .map(
            zoneName ->
                ventModuleConfiguration.getVentModuleDao().getZoneDaoHashMap().get(zoneName));
  }

  public Mono<Throttle> getIntakeThrottle() {
    return Mono.just(ventModuleConfiguration.getVentModuleDao().getIntakeThrottle());
  }

  public Mono<HashMap<ZoneName, ZoneDao>> getZonesFullData() {
    return Mono.just(ventModuleConfiguration.getVentModuleDao().getZoneDaoHashMap());
  }

  public Mono<Fans> getFans() {
    return Mono.just(ventModuleConfiguration.getVentModuleDao().getFans());
  }

  public Mono<Fan> getInletFan() {
    return Mono.just(ventModuleConfiguration.getVentModuleDao().getFans().getInlet());
  }

  public Mono<Fan> getOutletFan() {
    return Mono.just(ventModuleConfiguration.getVentModuleDao().getFans().getOutlet());
  }
}
