package pl.smarthouse.ventmodule.configurations;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import pl.smarthouse.sharedobjects.enums.Operation;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.smartmodule.model.actors.type.pca9685.Pca9685CommandType;
import pl.smarthouse.ventmodule.enums.FunctionType;
import pl.smarthouse.ventmodule.model.dao.ThrottleDao;
import pl.smarthouse.ventmodule.model.dao.ZoneDao;

import java.time.LocalDateTime;
import java.util.HashMap;

import static pl.smarthouse.sharedobjects.enums.ZoneName.*;
import static pl.smarthouse.smartmodule.model.actors.type.pca9685.Pca9685CommandType.*;
import static pl.smarthouse.ventmodule.properties.ThrottleProperties.*;

@Configuration
@Getter
public class VentModuleConfiguration {
  // INTAKE THROTTLE
  public static final int THROTTLE_INTAKE_OPEN_POSITION = 0;
  public static final int THROTTLE_INTAKE_CLOSE_POSITION = 0;

  private final HashMap<ZoneName, ZoneDao> zoneDaoHashMap = new HashMap<>();

  private final ThrottleDao airIntake =
      ThrottleDao.builder()
          .openPosition(THROTTLE_INTAKE_OPEN_POSITION)
          .closePosition(THROTTLE_INTAKE_CLOSE_POSITION)
          .goalPosition(THROTTLE_INTAKE_CLOSE_POSITION)
          .currentPosition(0)
          .commandType(WRITE_SERVO0_MICROSECONDS)
          .build();

  public VentModuleConfiguration() {
    addZones();
  }

  private void addZones() {
    zoneDaoHashMap.put(
        SALON,
        createZone(
            FunctionType.OUTLET,
            SALON_OPEN_POSITION,
            SALON_CLOSE_POSITION,
            WRITE_SERVO1_MICROSECONDS));
    zoneDaoHashMap.put(
        KUCHNIA,
        createZone(
            FunctionType.INLET,
            KUCHNIA_OPEN_POSITION,
            KUCHNIA_CLOSE_POSITION,
            WRITE_SERVO2_MICROSECONDS));
    zoneDaoHashMap.put(
        BIURO,
        createZone(
            FunctionType.OUTLET,
            BIURO_OPEN_POSITION,
            BIURO_CLOSE_POSITION,
            WRITE_SERVO3_MICROSECONDS));
    zoneDaoHashMap.put(
        LAZ_DOL,
        createZone(
            FunctionType.INLET,
            LAZ_DOL_OPEN_POSITION,
            LAZ_DOL_CLOSE_POSITION,
            WRITE_SERVO4_MICROSECONDS));
    zoneDaoHashMap.put(
        PRZEDPOKOJ,
        createZone(
            FunctionType.INLET,
            PRZEDPOKOJ_OPEN_POSITION,
            PRZEDPOKOJ_CLOSE_POSITION,
            WRITE_SERVO5_MICROSECONDS));
    zoneDaoHashMap.put(
        PRALNIA,
        createZone(
            FunctionType.INLET,
            PRALNIA_OPEN_POSITION,
            PRALNIA_CLOSE_POSITION,
            WRITE_SERVO6_MICROSECONDS));
    zoneDaoHashMap.put(
        WARSZTAT,
        createZone(
            FunctionType.INLET,
            WARSZTAT_OPEN_POSITION,
            WARSZTAT_CLOSE_POSITION,
            WRITE_SERVO7_MICROSECONDS));
    zoneDaoHashMap.put(
        RODZICE,
        createZone(
            FunctionType.OUTLET,
            RODZICE_OPEN_POSITION,
            RODZICE_CLOSE_POSITION,
            WRITE_SERVO8_MICROSECONDS));
    zoneDaoHashMap.put(
        GARDEROBA,
        createZone(
            FunctionType.INLET,
            GARDEROBA_OPEN_POSITION,
            GARDEROBA_CLOSE_POSITION,
            WRITE_SERVO9_MICROSECONDS));
    zoneDaoHashMap.put(
        NATALIA,
        createZone(
            FunctionType.OUTLET,
            NATALIA_OPEN_POSITION,
            NATALIA_CLOSE_POSITION,
            WRITE_SERVO10_MICROSECONDS));
    zoneDaoHashMap.put(
        LAZ_GORA,
        createZone(
            FunctionType.INLET,
            LAZ_GORA_OPEN_POSITION,
            LAZ_GORA_CLOSE_POSITION,
            WRITE_SERVO11_MICROSECONDS));
    zoneDaoHashMap.put(
        KAROLINA,
        createZone(
            FunctionType.OUTLET,
            KAROLINA_OPEN_POSITION,
            KAROLINA_CLOSE_POSITION,
            WRITE_SERVO12_MICROSECONDS));
  }

  private ZoneDao createZone(
      final FunctionType functionType,
      final int openPosition,
      final int closePosition,
      final Pca9685CommandType commandType) {
    return ZoneDao.builder()
        .functionType(functionType)
        .operation(Operation.STANDBY)
        .throttleDao(
            ThrottleDao.builder()
                .openPosition(openPosition)
                .closePosition(closePosition)
                .currentPosition(0)
                .commandType(commandType)
                .build())
        .lastUpdate(LocalDateTime.now())
        .build();
  }
}
