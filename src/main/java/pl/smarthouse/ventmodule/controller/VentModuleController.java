package pl.smarthouse.ventmodule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.smarthouse.sharedobjects.dto.ventilation.VentModuleDto;
import pl.smarthouse.sharedobjects.dto.ventilation.VentModuleParamsDto;
import pl.smarthouse.ventmodule.service.VentModuleParamsService;
import pl.smarthouse.ventmodule.service.VentModuleService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/vent")
@RequiredArgsConstructor
public class VentModuleController {
  private final VentModuleService ventModuleService;
  private final VentModuleParamsService ventModuleParamsService;

  @GetMapping()
  public Mono<VentModuleDto> getVentModule() {
    return ventModuleService.getVentModule();
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
