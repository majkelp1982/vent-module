package pl.smarthouse.ventmodule.configurations;

import static pl.smarthouse.sharedobjects.enums.ZoneName.*;
import static pl.smarthouse.smartmodule.model.actors.type.pca9685.Pca9685CommandType.*;
import static pl.smarthouse.ventmodule.properties.ThrottleProperties.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import pl.smarthouse.sharedobjects.dto.core.enums.State;
import pl.smarthouse.sharedobjects.dto.ventilation.enums.FunctionType;
import pl.smarthouse.sharedobjects.enums.Operation;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.sharedobjects.utils.FunctionTypeUtil;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280Response;
import pl.smarthouse.smartmodule.model.actors.type.ds18b20.Ds18b20Result;
import pl.smarthouse.smartmodule.model.actors.type.pca9685.Pca9685CommandType;
import pl.smarthouse.smartmonitoring.model.BooleanCompareProperties;
import pl.smarthouse.smartmonitoring.model.EnumCompareProperties;
import pl.smarthouse.smartmonitoring.model.NumberCompareProperties;
import pl.smarthouse.smartmonitoring.properties.defaults.Bme280DefaultProperties;
import pl.smarthouse.smartmonitoring.properties.defaults.Ds18b20DefaultProperties;
import pl.smarthouse.smartmonitoring.service.CompareProcessor;
import pl.smarthouse.smartmonitoring.service.MonitoringService;
import pl.smarthouse.ventmodule.model.core.*;
import pl.smarthouse.ventmodule.model.dao.VentModuleDao;
import pl.smarthouse.ventmodule.model.dao.ZoneDao;
import pl.smarthouse.ventmodule.properties.Esp32ModuleProperties;
import pl.smarthouse.ventmodule.properties.FanProperties;

@Configuration
@RequiredArgsConstructor
@Getter
public class VentModuleConfiguration {
  private final CompareProcessor compareProcessor;
  private final MonitoringService monitoringService;
  private VentModuleDao ventModuleDao;

  @PostConstruct
  void postConstruct() {
    ventModuleDao =
        VentModuleDao.builder()
            .moduleName(Esp32ModuleProperties.MODULE_TYPE)
            .zoneDaoHashMap(createZones())
            .fans(
                Fans.builder()
                    .inlet(Fan.builder().currentSpeed(1).goalSpeed(0).build())
                    .outlet(Fan.builder().currentSpeed(1).goalSpeed(0).build())
                    .build())
            .airExchanger(
                AirExchanger.builder()
                    .inlet(new Bme280Response())
                    .outlet(new Bme280Response())
                    .freshAir(new Bme280Response())
                    .userAir(new Bme280Response())
                    .build())
            .forcedAirSystemExchanger(
                ForcedAirSystemExchanger.builder()
                    .watterIn(new Ds18b20Result())
                    .watterOut(new Ds18b20Result())
                    .airIn(new Ds18b20Result())
                    .airOut(new Ds18b20Result())
                    .build())
            .intakeThrottle(
                Throttle.builder()
                    .openPosition(THROTTLE_INTAKE_EXTERNAL_SOURCE)
                    .closePosition(THROTTLE_INTAKE_INTERNAL_SOURCE)
                    .goalPosition(THROTTLE_INTAKE_EXTERNAL_SOURCE)
                    .currentPosition(0)
                    .commandType(WRITE_SERVO0_MICROSECONDS)
                    .build())
            .airCondition(State.OFF)
            .circuitPump(State.OFF)
            .fireplaceAirOverpressureActive(State.OFF)
            .build();
    monitoringService.setModuleDaoObject(ventModuleDao);
    createCompareMap();
  }

  private void createCompareMap() {
    compareProcessor.addMap("error", BooleanCompareProperties.builder().saveEnabled(true).build());
    compareProcessor.addMap(
        "errorPendingAcknowledge", BooleanCompareProperties.builder().saveEnabled(true).build());

    compareProcessor.addMap(
        "fireplaceAirOverpressureActive",
        EnumCompareProperties.builder().saveEnabled(true).build());
    compareProcessor.addMap(
        "airCondition", EnumCompareProperties.builder().saveEnabled(true).build());
    compareProcessor.addMap(
        "circuitPump", EnumCompareProperties.builder().saveEnabled(true).build());

    Bme280DefaultProperties.setDefaultProperties(compareProcessor, "airExchanger.freshAir");
    Bme280DefaultProperties.setDefaultProperties(compareProcessor, "airExchanger.inlet");
    Bme280DefaultProperties.setDefaultProperties(compareProcessor, "airExchanger.outlet");
    Bme280DefaultProperties.setDefaultProperties(compareProcessor, "airExchanger.userAir");

    compareProcessor.addMap("fans.inlet.currentSpeed", FanProperties.getSpeedProperties());
    compareProcessor.addMap("fans.inlet.goalSpeed", FanProperties.getSpeedProperties());
    compareProcessor.addMap("fans.inlet.revolution", FanProperties.getRevolutionsProperties());

    compareProcessor.addMap("fans.outlet.currentSpeed", FanProperties.getSpeedProperties());
    compareProcessor.addMap("fans.outlet.goalSpeed", FanProperties.getSpeedProperties());
    compareProcessor.addMap("fans.outlet.revolution", FanProperties.getRevolutionsProperties());

    Ds18b20DefaultProperties.setDefaultProperties(
        compareProcessor, "forcedAirSystemExchanger.airIn");
    Ds18b20DefaultProperties.setDefaultProperties(
        compareProcessor, "forcedAirSystemExchanger.airOut");
    Ds18b20DefaultProperties.setDefaultProperties(
        compareProcessor, "forcedAirSystemExchanger.watterIn");
    Ds18b20DefaultProperties.setDefaultProperties(
        compareProcessor, "forcedAirSystemExchanger.watterOut");

    compareProcessor.addMap(
        "intakeThrottle.closePosition",
        NumberCompareProperties.builder().saveEnabled(true).saveTolerance(1).build());
    compareProcessor.addMap(
        "intakeThrottle.commandType", EnumCompareProperties.builder().saveEnabled(false).build());
    compareProcessor.addMap(
        "intakeThrottle.currentPosition",
        NumberCompareProperties.builder().saveEnabled(true).saveTolerance(1).build());
    compareProcessor.addMap(
        "intakeThrottle.goalPosition",
        NumberCompareProperties.builder().saveEnabled(true).saveTolerance(1).build());
    compareProcessor.addMap(
        "intakeThrottle.openPosition",
        NumberCompareProperties.builder().saveEnabled(true).saveTolerance(1).build());
  }

  private HashMap<ZoneName, ZoneDao> createZones() {
    final HashMap<ZoneName, ZoneDao> zoneDaoHashMap = new HashMap<>();
    zoneDaoHashMap.put(
        SALON,
        createZone(
            FunctionTypeUtil.determinateFunctionType(SALON),
            SALON_OPEN_POSITION,
            SALON_CLOSE_POSITION,
            WRITE_SERVO1_MICROSECONDS));
    zoneDaoHashMap.put(
        KUCHNIA,
        createZone(
            FunctionTypeUtil.determinateFunctionType(KUCHNIA),
            KUCHNIA_OPEN_POSITION,
            KUCHNIA_CLOSE_POSITION,
            WRITE_SERVO2_MICROSECONDS));
    zoneDaoHashMap.put(
        BIURO,
        createZone(
            FunctionTypeUtil.determinateFunctionType(BIURO),
            BIURO_OPEN_POSITION,
            BIURO_CLOSE_POSITION,
            WRITE_SERVO3_MICROSECONDS));
    zoneDaoHashMap.put(
        LAZ_DOL,
        createZone(
            FunctionTypeUtil.determinateFunctionType(LAZ_DOL),
            LAZ_DOL_OPEN_POSITION,
            LAZ_DOL_CLOSE_POSITION,
            WRITE_SERVO4_MICROSECONDS));
    zoneDaoHashMap.put(
        PRZEDPOKOJ,
        createZone(
            FunctionTypeUtil.determinateFunctionType(PRZEDPOKOJ),
            PRZEDPOKOJ_OPEN_POSITION,
            PRZEDPOKOJ_CLOSE_POSITION,
            WRITE_SERVO5_MICROSECONDS));
    zoneDaoHashMap.put(
        PRALNIA,
        createZone(
            FunctionTypeUtil.determinateFunctionType(PRALNIA),
            PRALNIA_OPEN_POSITION,
            PRALNIA_CLOSE_POSITION,
            WRITE_SERVO6_MICROSECONDS));
    zoneDaoHashMap.put(
        WARSZTAT,
        createZone(
            FunctionTypeUtil.determinateFunctionType(WARSZTAT),
            WARSZTAT_OPEN_POSITION,
            WARSZTAT_CLOSE_POSITION,
            WRITE_SERVO7_MICROSECONDS));
    zoneDaoHashMap.put(
        RODZICE,
        createZone(
            FunctionTypeUtil.determinateFunctionType(RODZICE),
            RODZICE_OPEN_POSITION,
            RODZICE_CLOSE_POSITION,
            WRITE_SERVO8_MICROSECONDS));
    zoneDaoHashMap.put(
        GARDEROBA,
        createZone(
            FunctionTypeUtil.determinateFunctionType(GARDEROBA),
            GARDEROBA_OPEN_POSITION,
            GARDEROBA_CLOSE_POSITION,
            WRITE_SERVO9_MICROSECONDS));
    zoneDaoHashMap.put(
        NATALIA,
        createZone(
            FunctionTypeUtil.determinateFunctionType(NATALIA),
            NATALIA_OPEN_POSITION,
            NATALIA_CLOSE_POSITION,
            WRITE_SERVO10_MICROSECONDS));
    zoneDaoHashMap.put(
        LAZ_GORA,
        createZone(
            FunctionTypeUtil.determinateFunctionType(LAZ_GORA),
            LAZ_GORA_OPEN_POSITION,
            LAZ_GORA_CLOSE_POSITION,
            WRITE_SERVO11_MICROSECONDS));
    zoneDaoHashMap.put(
        KAROLINA,
        createZone(
            FunctionTypeUtil.determinateFunctionType(KAROLINA),
            KAROLINA_OPEN_POSITION,
            KAROLINA_CLOSE_POSITION,
            WRITE_SERVO12_MICROSECONDS));
    return zoneDaoHashMap;
  }

  private ZoneDao createZone(
      final FunctionType functionType,
      final int openPosition,
      final int closePosition,
      final Pca9685CommandType commandType) {
    return ZoneDao.builder()
        .functionType(functionType)
        .operation(Operation.STANDBY)
        .throttle(
            Throttle.builder()
                .openPosition(openPosition)
                .closePosition(closePosition)
                .currentPosition(0)
                .commandType(commandType)
                .build())
        .lastUpdate(LocalDateTime.now())
        .build();
  }
}
