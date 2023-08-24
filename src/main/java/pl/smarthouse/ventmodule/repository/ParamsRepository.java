package pl.smarthouse.ventmodule.repository;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import pl.smarthouse.ventmodule.model.dao.VentModuleParamsDao;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ParamsRepository {
  private final ReactiveMongoTemplate reactiveMongoTemplate;

  public Mono<VentModuleParamsDao> saveParams(
      final VentModuleParamsDao VentModuleParamsDao, final String paramTableName) {
    return reactiveMongoTemplate
        .remove(new Query(), VentModuleParamsDao.class, paramTableName)
        .then(reactiveMongoTemplate.save(VentModuleParamsDao, paramTableName));
  }

  public Mono<VentModuleParamsDao> getParams(final String paramTableName) {
    return reactiveMongoTemplate
        .findAll(VentModuleParamsDao.class, paramTableName)
        .last()
        .cache(Duration.ofMinutes(1));
  }
}
