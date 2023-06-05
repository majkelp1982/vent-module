package pl.smarthouse.ventmodule.utils;

import java.util.HashMap;
import lombok.experimental.UtilityClass;
import pl.smarthouse.sharedobjects.dto.core.Bme280ResponseDto;
import pl.smarthouse.sharedobjects.dto.core.Ds18b20ResultDto;
import pl.smarthouse.sharedobjects.dto.ventilation.VentModuleDto;
import pl.smarthouse.sharedobjects.dto.ventilation.ZoneDto;
import pl.smarthouse.sharedobjects.dto.ventilation.core.*;
import pl.smarthouse.sharedobjects.dto.ventilation.enums.IntakeThrottleState;
import pl.smarthouse.sharedobjects.dto.ventilation.enums.ThrottleState;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280Response;
import pl.smarthouse.smartmodule.model.actors.type.ds18b20.Ds18b20Result;
import pl.smarthouse.ventmodule.model.core.*;
import pl.smarthouse.ventmodule.model.dao.VentModuleDao;
import pl.smarthouse.ventmodule.model.dao.ZoneDao;

@UtilityClass
public class ModelMapper {
  public VentModuleDto toVentModuleDto(final VentModuleDao ventModuleDao) {
    return VentModuleDto.builder()
        .zoneDtoHashMap(toZoneDtoHashMap(ventModuleDao.getZoneDaoHashMap()))
        .fans(toFansDto(ventModuleDao.getFans()))
        .intakeThrottle(toIntakeThrottleDto(ventModuleDao.getIntakeThrottle()))
        .airExchanger(toAirExchangerDto(ventModuleDao.getAirExchanger()))
        .forcedAirSystemExchanger(
            toForcedAirSystemExchangerDto(ventModuleDao.getForcedAirSystemExchanger()))
        .circuitPump(ventModuleDao.getCircuitPump())
        .airCondition(ventModuleDao.getAirCondition())
        .build();
  }

  private ForcedAirSystemExchangerDto toForcedAirSystemExchangerDto(
      final ForcedAirSystemExchanger forcedAirSystemExchanger) {
    return ForcedAirSystemExchangerDto.builder()
        .watterIn(toDs18b20ResultDto(forcedAirSystemExchanger.getWatterIn()))
        .watterOut(toDs18b20ResultDto(forcedAirSystemExchanger.getWatterOut()))
        .airIn(toDs18b20ResultDto(forcedAirSystemExchanger.getAirIn()))
        .airOut(toDs18b20ResultDto(forcedAirSystemExchanger.getAirOut()))
        .build();
  }

  private Ds18b20ResultDto toDs18b20ResultDto(final Ds18b20Result ds18b20Result) {
    return Ds18b20ResultDto.builder()
        .address(ds18b20Result.getAddress())
        .temp(ds18b20Result.getTemp())
        .error(ds18b20Result.isError())
        .lastUpdate(ds18b20Result.getLastUpdate())
        .build();
  }

  private AirExchangerDto toAirExchangerDto(final AirExchanger airExchanger) {
    return AirExchangerDto.builder()
        .inlet(toBme280ResponseDto(airExchanger.getInlet()))
        .outlet(toBme280ResponseDto(airExchanger.getOutlet()))
        .freshAir(toBme280ResponseDto(airExchanger.getFreshAir()))
        .userAir(toBme280ResponseDto(airExchanger.getUserAir()))
        .build();
  }

  private Bme280ResponseDto toBme280ResponseDto(final Bme280Response bme280Response) {
    return Bme280ResponseDto.builder()
        .responseUpdate(bme280Response.getResponseUpdate())
        .temperature(bme280Response.getTemperature())
        .pressure(bme280Response.getPressure())
        .humidity(bme280Response.getHumidity())
        .error(bme280Response.isError())
        .build();
  }

  private IntakeThrottleDto toIntakeThrottleDto(final Throttle intakeThrottle) {
    return IntakeThrottleDto.builder()
        .currentPosition(
            intakeThrottle.getCurrentPosition() == intakeThrottle.getOpenPosition()
                ? IntakeThrottleState.OUTSIDE
                : IntakeThrottleState.INSIDE)
        .goalPosition(
            intakeThrottle.getGoalPosition() == intakeThrottle.getOpenPosition()
                ? IntakeThrottleState.OUTSIDE
                : IntakeThrottleState.INSIDE)
        .build();
  }

  private FansDto toFansDto(final Fans fans) {
    return FansDto.builder()
        .inlet(toFanDto(fans.getInlet()))
        .outlet(toFanDto(fans.getOutlet()))
        .build();
  }

  private FanDto toFanDto(final Fan fan) {
    return FanDto.builder()
        .currentSpeed(fan.getCurrentSpeed())
        .goalSpeed(fan.getGoalSpeed())
        .revolution(fan.getRevolution())
        .build();
  }

  private HashMap<ZoneName, ZoneDto> toZoneDtoHashMap(
      final HashMap<ZoneName, ZoneDao> zoneDaoHashMap) {
    final HashMap<ZoneName, ZoneDto> result = new HashMap<>();
    zoneDaoHashMap.forEach((zoneName, zoneDao) -> result.put(zoneName, toZoneDto(zoneDao)));
    return result;
  }

  public ZoneDto toZoneDto(final ZoneDao zoneDao) {
    return ZoneDto.builder()
        .lastUpdate(zoneDao.getLastUpdate())
        .operation(zoneDao.getOperation())
        .throttle(toThrottleDto(zoneDao.getThrottle()))
        .requiredPower(zoneDao.getRequiredPower())
        .build();
  }

  private ThrottleDto toThrottleDto(final Throttle throttle) {
    return ThrottleDto.builder()
        .currentPosition(
            throttle.getCurrentPosition() == throttle.getOpenPosition()
                ? ThrottleState.OPEN
                : ThrottleState.CLOSE)
        .goalPosition(
            throttle.getGoalPosition() == throttle.getOpenPosition()
                ? ThrottleState.OPEN
                : ThrottleState.CLOSE)
        .build();
  }
}
