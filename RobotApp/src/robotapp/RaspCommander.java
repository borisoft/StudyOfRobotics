/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robotapp;

import com.pi4j.io.gpio.*;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author hiber
 */
public class RaspCommander {

    static double x, y, a;
    static Serial serial = null;

    static {
        x = y = a = 0;
        System.out.println("Static initializer of coordinates invoked");
        serial = SerialFactory.createInstance();
        serial.addListener(new SerialDataListener() {
            @Override
            public void dataReceived(SerialDataEvent event) {
                try {
                    // print out the data received to the console
                    String dataReceived = event.getData();
                    System.out.println("Data: " + dataReceived);
                    // regular expressions
                    Pattern pattern = Pattern.compile("\\{(x=([\\-]?[0-9]*\\.?[0-9]+)).*(y=([\\-]?[0-9]*\\.?[0-9]+)).*(angle=([\\-]?[0-9]*\\.?[0-9]+)).*\\}");
                    Matcher m = pattern.matcher(dataReceived);
                    while (m.find()) {
                        x = Double.parseDouble(m.group(2));
                        y = Double.parseDouble(m.group(4));
                        a = Double.parseDouble(m.group(6));
                    }

                } catch (IllegalStateException ex) {
                    System.out.println("Init illegal state error " + ex.getMessage());
                }
            }
        });
        serial.open(Serial.DEFAULT_COM_PORT, 9600);
    }

    static public String doCommand(String command, String angle, String distance) throws InterruptedException {
        try {
            if (!command.equals("coordinates")) {
                System.out.println("Command '" + command + "' x=" + x + "; y = " + y + "; a=" + a);
            }
            
            switch (command) {
                case "stop":
                    System.out.println("Stop command invoked in arduino");
                    serial.write("s");
                    return "{status: 'stopped'}";
                case "shutdown":
                    return shutdown();
                case "coordinates":
                    // {"x"="0.0", "y"="10.9", "angle"="20"}
                    return "{\"x\":\"" + x + "\", \"y\":\"" + y + "\", \"angle\":\"" + a + "\"}";
                case "forward":
                    if (distance.equals("0"))
                        return manageTeam("f");
                    return manageTeam("_f_" + distance);
                case "back":
                    if (distance.equals("0"))
                        return manageTeam("b");
                    return manageTeam("_b_" + distance);
                case "turn_right":
                    return manageTeam("r");
                case "turn_left":
                    return manageTeam("l");
                case "left":
                    return manageTeam("_l_" + angle);
                case "right":
                    return manageTeam("_r_" + angle);
                case "up":
                    return manageAnotherTeam("_up_" + angle);
                case "down":
                    return manageAnotherTeam("_down_" + angle);
                case "shot":
                    return manageTeam("shot");
                default:
                    return "{status: 'unrecognized command'}";
            }
        } catch (SerialPortException ex) {
            System.out.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
            return "{status: 'error " + ex.getMessage() + "'}";
        } catch (IllegalStateException | InterruptedException ex) {
            ex.printStackTrace();
            System.out.println("Thread error: " + ex.getMessage());
            return "{status: 'error " + ex.getMessage() + "'}";
        } catch (Exception ex) {
            System.out.println("Common error: " + ex.getMessage());
            return "{status: 'error " + ex.getMessage() + "'}";
        }
    }

    static private String shutdown() throws InterruptedException {
        try {
            if (null != serial && serial.isOpen()) {
                serial.close();
                serial.shutdown();
                System.out.println("Serial port is closed by shutdown command");
            }
            final GpioController gpio = GpioFactory.getInstance();
            final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, PinState.HIGH);
            pin.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
            System.out.println("--> GPIO state should be: ON");
            System.out.println("    This program will automatically terminate in 10 seconds,");
            System.out.println("    or you can use the CTRL-C keystroke to terminate at any time.");
            System.out.println("    When the program terminates, the GPIO state should be shutdown and set to: OFF");

            Thread.sleep(10000);
            System.out.println(" .. shutting down now ...");
            gpio.shutdown();
            return "{status: 'stopped'}";
        }
        catch (Exception ex) {
            System.out.println("Common error: " + ex.getMessage());
            return "";
        }
    }

    static public String manageTeam(String str) throws InterruptedException {
        try {
            serial.write(str);
            Thread.currentThread().sleep(100);
            return "{status: 'executed'}";
        }
        catch (Exception ex) {
            System.out.println("Common error: " + ex.getMessage());
            return "";
        }
    }
    
    static public String manageAnotherTeam(String str) throws InterruptedException {
        try {
            serial.write(str);
            Thread.currentThread().sleep(100);
            return "{status: 'executed'}";
        }
        catch (Exception ex) {
            System.out.println("Common error: " + ex.getMessage());
            return "";
        }
    }
}
