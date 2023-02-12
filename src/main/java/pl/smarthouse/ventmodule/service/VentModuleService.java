package pl.smarthouse.ventmodule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.ventmodule.configurations.VentModuleConfiguration;
import pl.smarthouse.ventmodule.model.core.Fan;
import pl.smarthouse.ventmodule.model.core.Fans;
import pl.smarthouse.ventmodule.model.core.Throttle;
import pl.smarthouse.ventmodule.model.dao.VentModuleDao;
import pl.smarthouse.ventmodule.model.dao.ZoneDao;
import pl.smarthouse.ventmodule.model.dto.VentModuleDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Service
@RequiredArgsConstructor
@Slf4j
public class VentModuleService {
  private final VentModuleConfiguration ventModuleConfiguration;
  private final ModelMapper modelMapper = new ModelMapper();

  public Mono<VentModuleDto> getVentModule() {
    return Mono.just(
        modelMapper.map(ventModuleConfiguration.getVentModuleDao(), VentModuleDto.class));
  }

  public Mono<VentModuleDao> getVentModuleDao() {
    return Mono.just(ventModuleConfiguration.getVentModuleDao());
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

  public Flux<Tuple2<ZoneName, ZoneDao>> getAllZonesWithZoneNames() {
    return Flux.fromIterable(
            ventModuleConfiguration.getVentModuleDao().getZoneDaoHashMap().keySet())
        .map(
            zoneName ->
                Tuples.of(
                    zoneName,
                    ventModuleConfiguration.getVentModuleDao().getZoneDaoHashMap().get(zoneName)));
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
