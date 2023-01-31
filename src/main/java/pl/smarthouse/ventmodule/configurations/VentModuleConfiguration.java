package pl.smarthouse.ventmodule.configurations;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import pl.smarthouse.ventmodule.enums.FunctionType;
import pl.smarthouse.ventmodule.model.dao.ThrottleDao;
import pl.smarthouse.ventmodule.model.dao.ZoneDao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Getter
public class VentModuleConfiguration {
  // ZONES
  public static final String ZONE_SALON = "salon";
  public static final int ZONE_SALON_THROTTLE_OPEN_POSITION = 0;
  public static final int ZONE_SALON_THROTTLE_CLOSE_POSITION = 0;
  public static final String ZONE_KUCHNIA = "kuchnia";
  public static final int ZONE_KUCHNIA_THROTTLE_OPEN_POSITION = 0;
  public static final int ZONE_KUCHNIA_THROTTLE_CLOSE_POSITION = 0;
  public static final String ZONE_BIURO = "Biuro";
  public static final int ZONE_BIURO_THROTTLE_OPEN_POSITION = 0;
  public static final int ZONE_BIURO_THROTTLE_CLOSE_POSITION = 0;
  public static final String ZONE_LAZ_DOL = "łazienka_dół";
  public static final int ZONE_LAZ_DOL_THROTTLE_OPEN_POSITION = 0;
  public static final int ZONE_LAZ_DOL_THROTTLE_CLOSE_POSITION = 0;
  public static final String ZONE_PRZEDPOKOJ = "przedpokój";
  public static final int ZONE_PRZEDPOKOJ_THROTTLE_OPEN_POSITION = 0;
  public static final int ZONE_PRZEDPOKOJ_THROTTLE_CLOSE_POSITION = 0;
  public static final String ZONE_PRALNIA = "pralnia";
  public static final int ZONE_PRALNIA_THROTTLE_OPEN_POSITION = 0;
  public static final int ZONE_PRALNIA_THROTTLE_CLOSE_POSITION = 0;
  public static final String ZONE_WARSZTAT = "warsztat";
  public static final int ZONE_WARSZTAT_THROTTLE_OPEN_POSITION = 0;
  public static final int ZONE_WARSZTAT_THROTTLE_CLOSE_POSITION = 0;
  public static final String ZONE_RODZICE = "rodzice";
  public static final int ZONE_RODZICE_THROTTLE_OPEN_POSITION = 0;
  public static final int ZONE_RODZICE_THROTTLE_CLOSE_POSITION = 0;
  public static final String ZONE_GARDEROBA = "garderoba";
  public static final int ZONE_GARDEROBA_THROTTLE_OPEN_POSITION = 0;
  public static final int ZONE_GARDEROBA_THROTTLE_CLOSE_POSITION = 0;
  public static final String ZONE_NATALIA = "Natalia";
  public static final int ZONE_NATALIA_THROTTLE_OPEN_POSITION = 0;
  public static final int ZONE_NATALIA_THROTTLE_CLOSE_POSITION = 0;
  public static final String ZONE_LAZ_GORA = "łazienka_góra";
  public static final int ZONE_LAZ_GORA_THROTTLE_OPEN_POSITION = 0;
  public static final int ZONE_LAZ_GORA_THROTTLE_CLOSE_POSITION = 0;
  public static final String ZONE_KAROLINA = "Karolina";
  public static final int ZONE_KAROLINA_THROTTLE_OPEN_POSITION = 0;
  public static final int ZONE_KAROLINA_THROTTLE_CLOSE_POSITION = 0;

  // INTAKE THROTTLE
  public static final int THROTTLE_INTAKE_OPEN_POSITION = 0;
  public static final int THROTTLE_INTAKE_CLOSE_POSITION = 0;

  private final List<ZoneDao> zoneDaoList = new ArrayList<>();
  private final ThrottleDao airIntake =
      ThrottleDao.builder()
          .openPosition(THROTTLE_INTAKE_OPEN_POSITION)
          .closePosition(THROTTLE_INTAKE_CLOSE_POSITION)
          .goalPosition(THROTTLE_INTAKE_CLOSE_POSITION)
          .currentPosition(0)
          .build();

  public VentModuleConfiguration() {
    addZones();
  }

  private void addZones() {
    zoneDaoList.add(
        createZone(
            ZONE_SALON,
            FunctionType.OUTLET,
            ZONE_SALON_THROTTLE_OPEN_POSITION,
            ZONE_SALON_THROTTLE_CLOSE_POSITION));
    zoneDaoList.add(
        createZone(
            ZONE_KUCHNIA,
            FunctionType.INLET,
            ZONE_KUCHNIA_THROTTLE_OPEN_POSITION,
            ZONE_KUCHNIA_THROTTLE_CLOSE_POSITION));
    zoneDaoList.add(
        createZone(
            ZONE_BIURO,
            FunctionType.OUTLET,
            ZONE_BIURO_THROTTLE_OPEN_POSITION,
            ZONE_BIURO_THROTTLE_CLOSE_POSITION));
    zoneDaoList.add(
        createZone(
            ZONE_LAZ_DOL,
            FunctionType.INLET,
            ZONE_LAZ_DOL_THROTTLE_OPEN_POSITION,
            ZONE_LAZ_DOL_THROTTLE_CLOSE_POSITION));
    zoneDaoList.add(
        createZone(
            ZONE_PRZEDPOKOJ,
            FunctionType.INLET,
            ZONE_PRZEDPOKOJ_THROTTLE_OPEN_POSITION,
            ZONE_PRZEDPOKOJ_THROTTLE_CLOSE_POSITION));
    zoneDaoList.add(
        createZone(
            ZONE_PRALNIA,
            FunctionType.INLET,
            ZONE_PRALNIA_THROTTLE_OPEN_POSITION,
            ZONE_PRALNIA_THROTTLE_CLOSE_POSITION));
    zoneDaoList.add(
        createZone(
            ZONE_WARSZTAT,
            FunctionType.INLET,
            ZONE_WARSZTAT_THROTTLE_OPEN_POSITION,
            ZONE_PRALNIA_THROTTLE_CLOSE_POSITION));
    zoneDaoList.add(
        createZone(
            ZONE_RODZICE,
            FunctionType.OUTLET,
            ZONE_RODZICE_THROTTLE_OPEN_POSITION,
            ZONE_RODZICE_THROTTLE_CLOSE_POSITION));
    zoneDaoList.add(
        createZone(
            ZONE_GARDEROBA,
            FunctionType.INLET,
            ZONE_GARDEROBA_THROTTLE_OPEN_POSITION,
            ZONE_GARDEROBA_THROTTLE_CLOSE_POSITION));
    zoneDaoList.add(
        createZone(
            ZONE_NATALIA,
            FunctionType.OUTLET,
            ZONE_NATALIA_THROTTLE_OPEN_POSITION,
            ZONE_NATALIA_THROTTLE_CLOSE_POSITION));
    zoneDaoList.add(
        createZone(
            ZONE_LAZ_GORA,
            FunctionType.INLET,
            ZONE_LAZ_GORA_THROTTLE_OPEN_POSITION,
            ZONE_LAZ_GORA_THROTTLE_CLOSE_POSITION));
    zoneDaoList.add(
        createZone(
            ZONE_KAROLINA,
            FunctionType.OUTLET,
            ZONE_KAROLINA_THROTTLE_OPEN_POSITION,
            ZONE_KAROLINA_THROTTLE_CLOSE_POSITION));
  }

  private ZoneDao createZone(
      final String name,
      final FunctionType functionType,
      final int openPosition,
      final int closePosition) {
    return ZoneDao.builder()
        .name(name)
        .functionType(functionType)
        .throttleDao(
            ThrottleDao.builder()
                .openPosition(openPosition)
                .closePosition(closePosition)
                .currentPosition(0)
                .goalPosition(closePosition)
                .build())
        .lastUpdate(LocalDateTime.now())
        .build();
  }
}
