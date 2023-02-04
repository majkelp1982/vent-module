package pl.smarthouse.ventmodule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.smarthouse.ventmodule.model.dao.ZoneDao;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class FansService {

  private final ZoneService zoneService;

  public Flux<ZoneDao> setFansRequiredPower() {
    // TODO
    return Flux.empty();
  }
}
