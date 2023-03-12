package pl.smarthouse.ventmodule.controller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.ventmodule.service.TempOldComfortModuleService;

@Controller
@Slf4j
@RestController
@RequestMapping("/tempcomfort")
public class TempUdpController {
  private static final int PACKET_SIZE_MODULE_COMFORT = 31;
  private static final int BUFFER_SIZE = 128;
  private static MulticastSocket multicastSocket;
  private static int[] packetData;
  int localPort = 6000;
  Thread UDPThread = new Thread(new UDPListener());
  TempOldComfortModuleService tempOldComfortModuleService;

  public TempUdpController(
      @Autowired final TempOldComfortModuleService tempOldComfortModuleService) {
    this.tempOldComfortModuleService = tempOldComfortModuleService;
    try {
      multicastSocket = new MulticastSocket(localPort);
    } catch (final IOException e) {
      e.printStackTrace();
    }
    UDPThread.start();
  }

  @GetMapping
  public HashMap<String, Object> getTempComfortZones() {
    return tempOldComfortModuleService.getTempComfortZones();
  }

  @PostMapping(value = "/humidityThresholdLow")
  public void setHumidityThresholdLow(@RequestParam final int threshold) {
    tempOldComfortModuleService.setHumidityThresholdLow(threshold);
  }

  @PostMapping(value = "/humidityThresholdHigh")
  public void humidityThresholdHigh(@RequestParam final int threshold) {
    tempOldComfortModuleService.setHumidityThresholdHigh(threshold);
  }

  @PostMapping(value = "/heatingThresholdLow")
  public void heatingThresholdLow(@RequestParam final int threshold) {
    tempOldComfortModuleService.setHeatingThresholdLow(threshold);
  }

  @PostMapping(value = "/heatingThresholdHigh")
  public void heatingThresholdHigh(@RequestParam final int threshold) {
    tempOldComfortModuleService.setHeatingThresholdHigh(threshold);
  }

  @PostMapping(value = "/coolingThresholdHigh")
  public void coolingThresholdHigh(@RequestParam final int threshold) {
    tempOldComfortModuleService.setCoolingThresholdHigh(threshold);
  }

  @PostMapping(value = "/coolingThresholdLow")
  public void coolingThresholdLow(@RequestParam final int threshold) {
    tempOldComfortModuleService.setCoolingThresholdLow(threshold);
  }

  @PostMapping(value = "/airConditionThresholdHigh")
  public void airConditionThresholdHigh(@RequestParam final int threshold) {
    tempOldComfortModuleService.setAirConditionThresholdHigh(threshold);
  }

  @PostMapping(value = "/airConditionThresholdLow")
  public void airConditionThresholdLow(@RequestParam final int threshold) {
    tempOldComfortModuleService.setAirConditionThresholdLow(threshold);
  }

  @PostMapping(value = "/{zoneName}/forcedAirSystemEnabled")
  public void setForcedAirSystemEnabled(
      @PathVariable final ZoneName zoneName, @RequestParam final boolean enabled) {
    tempOldComfortModuleService.setForcedAirSystemEnabled(zoneName, enabled);
  }

  public class UDPListener implements Runnable {
    @Override
    public void run() {
      while (true) {
        final byte[] buffer = new byte[BUFFER_SIZE];
        final DatagramPacket packet = new DatagramPacket(buffer, BUFFER_SIZE);
        packetData = new int[BUFFER_SIZE];
        try {
          multicastSocket.receive(packet);
        } catch (final IOException e) {
          e.printStackTrace();
        }
        if (packet.getLength() > 0) {
          convertToUnsignedIntValues(packet);
          if (isModuleComfortUdpFrame(packet)) {
            final StringBuilder comfortUdp = new StringBuilder();
            comfortUdp.append("KOMFORT UDP=");
            for (int i = 0; i < PACKET_SIZE_MODULE_COMFORT; i++) {
              comfortUdp.append("[").append(packetData[i]).append("]");
            }
            log.info(comfortUdp.toString());
            tempOldComfortModuleService.setOldComfortModuleParameters(packetData);
          }
        }
      }
    }

    private boolean isModuleComfortUdpFrame(final DatagramPacket packet) {
      final int TYPE_MODULE_COMFORT = 10;
      return (packetData[0] == TYPE_MODULE_COMFORT)
        && packet.getLength() == PACKET_SIZE_MODULE_COMFORT;
    }

    private void convertToUnsignedIntValues(final DatagramPacket packet) {
      for (int i = 0; i < packet.getLength(); i++) {
        packetData[i] = (packet.getData()[i] & 0xff); // 0xFF to change values to unsigned int
      }
    }
  }
}
