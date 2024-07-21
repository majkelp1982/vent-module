package pl.smarthouse.ventmodule.service;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.smarthouse.sharedobjects.dto.ventilation.VentModuleParamsDto;
import pl.smarthouse.ventmodule.model.dao.VentModuleParamsDao;
import pl.smarthouse.ventmodule.repository.ParamsRepository;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class VentModuleParamsService {
  private final ParamsRepository paramsRepository;
  private final VentModuleService ventModuleService;
  private final ModelMapper modelMapper = new ModelMapper();
  private VentModuleParamsDto ventModuleParamsDto;

  public Mono<VentModuleParamsDto> saveParams(final VentModuleParamsDto ventModuleParamsDto) {
    return getParamTableName()
        .flatMap(
            paramTableName ->
                paramsRepository.saveParams(
                    modelMapper.map(ventModuleParamsDto, VentModuleParamsDao.class),
                    paramTableName))
        .doOnNext(ventModuleParamsDao -> refreshParams())
        .thenReturn(ventModuleParamsDto);
  }

  private Mono<String> getParamTableName() {
    return ventModuleService
        .getModuleName()
        .map(moduleName -> moduleName.toLowerCase() + "_settings");
  }

  public VentModuleParamsDto getParams() {
    if (ventModuleParamsDto == null) {
      log.warn("Waiting for module params");
      refreshParams();
    }

    return ventModuleParamsDto;
  }

  @Scheduled(initialDelay = 5000, fixedDelay = 60 * 1000)
  private void refreshParams() {
    getParamTableName()
        .flatMap(
            paramTableName ->
                paramsRepository
                    .getParams(paramTableName)
                    .doOnNext(
                        ventModuleParamsDao ->
                            log.debug("Successfully retrieve params: {}", ventModuleParamsDao))
                    .map(
                        ventModuleParamsDao ->
                            ventModuleParamsDto =
                                modelMapper.map(ventModuleParamsDao, VentModuleParamsDto.class))
                    .onErrorResume(
                        NoSuchElementException.class,
                        throwable -> {
                          log.warn("No params found for: {}", paramTableName);
                          return Mono.empty();
                        })
                    .doOnError(
                        throwable ->
                            log.error(
                                "Error on get params. Error message: {}, Error: {}",
                                throwable.getMessage(),
                                throwable))
                    .doOnSubscribe(
                        subscription ->
                            log.debug("Get module params from collection: {}", paramTableName)))
        .subscribe();
  }
}
