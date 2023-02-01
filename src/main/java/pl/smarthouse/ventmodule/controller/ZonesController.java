package pl.smarthouse.ventmodule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.smarthouse.sharedobjects.enums.Operation;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.ventmodule.model.dto.ZoneDto;
import pl.smarthouse.ventmodule.service.ZoneService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/zones")
@RequiredArgsConstructor
public class ZonesController {
  private final ZoneService zoneService;

  @PostMapping(value = "/{zoneName}/operation")
  public Mono<ZoneDto> setZoneOperation(
      @PathVariable final ZoneName zoneName, @RequestParam final Operation operation) {
    return zoneService.setZoneOperation(zoneName, operation);
  }
}
