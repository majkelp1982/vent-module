package pl.smarthouse.ventmodule.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.smarthouse.ventmodule.service.FansService;

@EnableScheduling
@RequiredArgsConstructor
@Service
public class FansScheduler {

  private final FansService fansService;

  @Scheduled(initialDelay = 10000, fixedDelay = 1000)
  public void setRequiredFansPower() {
    fansService.setFansRequiredPower().subscribe();
  }
}
