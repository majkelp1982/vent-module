package pl.smarthouse.ventmodule.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.smarthouse.ventmodule.service.ZoneService;

@EnableScheduling
@RequiredArgsConstructor
@Service
public class ZoneOutdatedScheduler {

  private final ZoneService zoneService;

  @Scheduled(initialDelay = 10000, fixedDelay = 10000)
  public void zoneOutdatedScheduler() {
    zoneService.checkIfZonesOutdated().subscribe();
  }
}
