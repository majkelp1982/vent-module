package pl.smarthouse.ventmodule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.smarthouse.sharedobjects.dto.ventilation.VentModuleDto;
import pl.smarthouse.sharedobjects.dto.ventilation.VentModuleParamsDto;
import pl.smarthouse.ventmodule.service.VentModuleParamsService;
import pl.smarthouse.ventmodule.service.VentModuleService;
import pl.smarthouse.ventmodule.service.airoverpressure.AirOverpressureStateCalculator;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping()
@RequiredArgsConstructor
public class VentModuleController {
  private final VentModuleService ventModuleService;
  private final VentModuleParamsService ventModuleParamsService;
  private final AirOverpressureStateCalculator airOverpressureStateCalculator;

  @GetMapping("/vent")
  public Mono<VentModuleDto> getVentModule() {
    return Mono.just(ventModuleService.getVentModule());
  }

  @PostMapping("/vent/overpressure/force")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Mono<Void> forceOverpressure() {
    airOverpressureStateCalculator.forceOverpressure();
    return Mono.empty();
  }

  @PostMapping("/params")
  public Mono<VentModuleParamsDto> saveParams(
      @RequestBody final VentModuleParamsDto ventModuleParamsDto) {
    return ventModuleParamsService.saveParams(ventModuleParamsDto);
  }

  @GetMapping("/params")
  public Mono<VentModuleParamsDto> getParams() {
    return Mono.just(ventModuleParamsService.getParams());
  }
}
