package pl.smarthouse.ventmodule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerResponse;
import pl.smarthouse.sharedobjects.dto.ventilation.VentModuleDto;
import pl.smarthouse.sharedobjects.dto.ventilation.VentModuleParamsDto;
import pl.smarthouse.ventmodule.service.FireplaceAirOverpressureService;
import pl.smarthouse.ventmodule.service.VentModuleParamsService;
import pl.smarthouse.ventmodule.service.VentModuleService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping()
@RequiredArgsConstructor
public class VentModuleController {
  private final VentModuleService ventModuleService;
  private final VentModuleParamsService ventModuleParamsService;
  private final FireplaceAirOverpressureService fireplaceAirOverpressureService;

  @GetMapping("/vent")
  public Mono<VentModuleDto> getVentModule() {
    return Mono.just(ventModuleService.getVentModule());
  }

  @PostMapping("/vent/overpressure/force")
  public Mono<ServerResponse> forceOverpressure() {
    fireplaceAirOverpressureService.forceOverpressure();
    return ServerResponse.status(HttpStatus.ACCEPTED).build();
  }

  @PostMapping("/params")
  public Mono<VentModuleParamsDto> saveParams(
      @RequestBody final VentModuleParamsDto ventModuleParamsDto) {
    return ventModuleParamsService.saveParams(ventModuleParamsDto);
  }

  @GetMapping("/params")
  public Mono<VentModuleParamsDto> getParams() {
    return ventModuleParamsService.getParams();
  }
}
