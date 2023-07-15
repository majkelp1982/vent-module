package pl.smarthouse.ventmodule.service;

import java.time.Duration;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pl.smarthouse.sharedobjects.dto.ventilation.VentModuleParamsDto;
import pl.smarthouse.ventmodule.model.dao.VentModuleParamsDao;
import pl.smarthouse.ventmodule.repository.ParamsRepository;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class VentModuleParamsService {
  private final ParamsRepository paramsRepository;
  private final VentModuleService ventModuleService;
  private final ModelMapper modelMapper = new ModelMapper();

  public Mono<VentModuleParamsDto> saveParams(final VentModuleParamsDto ventModuleParamsDto) {
    return getParamTableName()
        .flatMap(
            paramTableName ->
                paramsRepository.saveParams(
                    modelMapper.map(ventModuleParamsDto, VentModuleParamsDao.class),
                    paramTableName))
        .map(
            VentModuleParamsDao -> modelMapper.map(VentModuleParamsDao, VentModuleParamsDto.class));
  }

  private Mono<String> getParamTableName() {
    return ventModuleService
        .getModuleName()
        .map(moduleName -> moduleName.toLowerCase() + "_settings");
  }

  public Mono<VentModuleParamsDto> getParams() {
    return getParamTableName()
        .flatMap(
            paramTableName ->
                paramsRepository
                    .getParams(paramTableName)
                    .doOnNext(
                        ventModuleParamsDao ->
                            log.info("Successfully retrieve params: {}", ventModuleParamsDao))
                    .map(
                        ventModuleParamsDao ->
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
                            log.info("Get module params from collection: {}", paramTableName)))
        .cache(Duration.ofMinutes(1));
  }
}
