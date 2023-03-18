package pl.smarthouse.ventmodule.controller;

import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.ventmodule.model.core.Fan;
import pl.smarthouse.ventmodule.model.dao.ZoneDao;
import pl.smarthouse.ventmodule.service.VentModuleService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {
  private final VentModuleService ventModuleService;

  @GetMapping(value = "/getZonesFullData")
  public Mono<HashMap<ZoneName, ZoneDao>> getZonesFullData() {
    return ventModuleService.getZonesFullData();
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
