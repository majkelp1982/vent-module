package pl.smarthouse.ventmodule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.ventmodule.model.core.Fan;
import pl.smarthouse.ventmodule.model.dao.ZoneDao;
import pl.smarthouse.ventmodule.service.VentModuleService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@RestController
@RequestMapping("/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {
  private final VentModuleService ventModuleService;

  @GetMapping(value = "/getAllZonesWithNames")
  public Flux<Tuple2<ZoneName, ZoneDao>> getAllZonesWithNames() {
    return ventModuleService.getAllZonesWithZoneNames();
  }

  @GetMapping(value = "/fans/inlet")
  public Mono<Fan> getInletFan() {
    return ventModuleService.getInletFan();
  }

  @GetMapping(value = "/fans/outlet")
  public Mono<Fan> getOutletFan() {
    return ventModuleService.getOutletFan();
  }
}
