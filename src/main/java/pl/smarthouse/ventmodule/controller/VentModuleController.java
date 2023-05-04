package pl.smarthouse.ventmodule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.smarthouse.sharedobjects.dto.ventilation.VentModuleDto;
import pl.smarthouse.ventmodule.service.VentModuleService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/vent")
@RequiredArgsConstructor
public class VentModuleController {
  private final VentModuleService ventModuleService;

  @GetMapping()
  public Mono<VentModuleDto> getVentModule() {
    return ventModuleService.getVentModule();
  }
}
