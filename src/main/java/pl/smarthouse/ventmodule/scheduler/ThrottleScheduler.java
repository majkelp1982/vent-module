package pl.smarthouse.ventmodule.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.smarthouse.ventmodule.service.ThrottlesService;

@EnableScheduling
@RequiredArgsConstructor
@Service
public class ThrottleScheduler {

  private final ThrottlesService throttlesService;

  @Scheduled(initialDelay = 10000, fixedDelay = 1000)
  public void setThrottlesScheduler() {
    throttlesService.setThrottles().subscribe();
  }
}
