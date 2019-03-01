package com.symmetrylabs.util.ubnt;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import org.apache.commons.net.telnet.TelnetClient;

import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.regexp;

public class UbntTelnetConnection {

    // modify this variable based on whether or not we have a live UBNT system
    private static final String OPT = "UBNT";


    // ms to sleep between calling 'poe opmode shutdown' and 'poe opmode auto' on the telnet interface
    private static final long POWER_CYCLE_SLEEP = 2000;

    TelnetClient telnet = new TelnetClient();

    String ip;

    Expect expect;


    public UbntTelnetConnection(String ipAddr) throws IOException {
        this.ip = ipAddr;

        connect();

        navigateAdmin();

        pollPowerSamples();
    }//main

    public void connect() throws IOException {
        System.out.println("Connecting: " + ip);
        telnet.connect(ip);
    }

    public void powerCycle() throws IOException {

        // the command to turn off power to all ports, and then to turn back on.
        String[] SHUTDOWN_REBOOTAUTO_CMDS = {"poe opmode shutdown", "poe opmode auto"};

        for (String SHUTDOWN_REBOOTAUTO_CMD : SHUTDOWN_REBOOTAUTO_CMDS){

            // navigate to power cycle
            expect.sendLine("configure");
            expect.expect(contains("(UBNT EdgeSwitch) (Config)#"));

            expect.sendLine("interface 0/1-0/24");
            expect.expect(contains("(UBNT EdgeSwitch) (Interface 0/1-0/24)#"));

            expect.sendLine(SHUTDOWN_REBOOTAUTO_CMD);


            expect.close();
        }//ip loop
    }


    private void pollPowerSamples() throws IOException {
        String poe_power_command1 = "show poe status 0/1-0/12";
        String poe_power_command13 = "show poe status 0/13-0/24";
        String commands[] = { poe_power_command1, poe_power_command13};

        for (String command : commands){
            expect.sendLine(command);

            // capture list
            List<String> items;
            for (int i = -3; i < 13; i++){
                String list = expect.expect(regexp("\n")).getBefore();
                items = Arrays.asList(list.split("\\s* \\s*"));

                System.out.println("Items: " + items);

                String pattern = "(\\d+)/(\\d+)";
                Pattern p = Pattern.compile(pattern);
                Matcher m = p.matcher(list);

                if (m.find()){
                    System.out.println("gotem" + list);
                    System.out.println("Found value: " + m.group(0) );
                    System.out.println("Found value: " + m.group(1) ); // the
                    System.out.println("Found value: " + m.group(2) );
                }
                System.out.println("wattage: " + items.get(3));
            }

            expect.expect(contains("(UBNT EdgeSwitch) #"));
        }
    }

    private void navigateAdmin() throws IOException {
        StringBuilder wholeBuffer = new StringBuilder();
        expect = new ExpectBuilder()
            .withOutput(telnet.getOutputStream())
            .withInputs(telnet.getInputStream())
            .withEchoOutput(wholeBuffer)
            .withEchoInput(wholeBuffer)
            .withExceptionOnFailure()
            .build();

        String printme;

        printme = expect.expect(contains("User:")).getInput();
        System.out.println(printme);
        expect.sendLine("ubnt");

        printme = expect.expect(contains("Password:")).getInput();
        System.out.println(printme);
        expect.sendLine("ubnt");

        expect.expect(contains("(UBNT EdgeSwitch) >"));

        expect.sendLine("enable");

        expect.expect(contains("(UBNT EdgeSwitch) #"));


        String response = wholeBuffer.toString();
        System.out.println("The Last Word...");
        System.out.println(response);
    }

}
