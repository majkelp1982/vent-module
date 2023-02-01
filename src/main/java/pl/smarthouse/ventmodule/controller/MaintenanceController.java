package pl.smarthouse.ventmodule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.ventmodule.model.dao.ZoneDao;
import pl.smarthouse.ventmodule.service.ZoneService;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

@RestController
@RequestMapping("/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {
  private final ZoneService zoneService;

  @GetMapping
  public Flux<Tuple2<ZoneName, ZoneDao>> getAllZones() {
    return zoneService.getAllZones();
  }
}
