package com.symmetrylabs.util.EdgeSwitch;

import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import org.apache.commons.net.telnet.TelnetClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.regexp;


public class EdgeSwitch {
    private final String ip_addr;
    private TelnetClient telnet;

    private Expect expect;

    private double samples[];

    public EdgeSwitch(String ip_addr) throws IOException {
        this.ip_addr = ip_addr;

        // establish the connection
        this.telnet = new TelnetClient();
        telnet.connect("10.200.1.242");

        StringBuilder wholeBuffer = new StringBuilder();
        expect = new ExpectBuilder()
            .withOutput(telnet.getOutputStream())
            .withInputs(telnet.getInputStream())
            .withEchoOutput(wholeBuffer)
            .withEchoInput(wholeBuffer)
            .withExceptionOnFailure()
            .build();


        samples = new double[24];
    }

    public void retrieve_port_power_output() throws IOException {
        String printme;

        printme = expect.expect(contains("User:")).getInput();
//        System.out.println(printme);
        expect.sendLine("ubnt");

        printme = expect.expect(contains("Password:")).getInput();
//        System.out.println(printme);
        expect.sendLine("ubnt");

        expect.expect(contains("(UBNT EdgeSwitch) >"));

        expect.sendLine("enable");

        expect.expect(contains("(UBNT EdgeSwitch) #"));

        String poe_power_command1 = "show poe status 0/1-0/12";
        String poe_power_command13 = "show poe status 0/13-0/24";
        String commands[] = { poe_power_command1, poe_power_command13};


        int sample_index = 0;
        for (String command : commands){
            expect.sendLine(command);

            // capture file list
            List<String> items;
            for (int i = -3; i < 13; i++){
                String list = expect.expect(regexp("\n")).getBefore();
//                items = Arrays.asList(list.split("\\s* \\s*"));
                items = Arrays.asList(list.split("\\s{2,}"));


                // The switch returns data in this format.  We only care about certain lines
                //====================================================================================
                // Intf      Detection      Class   Consumed(W) Voltage(V) Current(mA) Temperature(C)
                //--------- -------------- ------- ----------- ---------- ----------- --------------
                //0/1       Open Circuit   Unknown        0.00       0.00        0.00             43
                //0/2       Open Circuit   Unknown        0.00       0.00        0.00             43
                //0/3       Good           Class4         5.31      52.37      101.44             43
                // ...
                // ...
                //====================================================================================
                String pattern = "^(\\d+)/(\\d+)"; // This regex will match the lines that have data
                Pattern p = Pattern.compile(pattern);
                Matcher m = p.matcher(list);

                if (m.find()){
                    // ok this should be a valid sample
                    samples[sample_index++] = Double.parseDouble(items.get(3));
                }
            }
            expect.expect(contains("(UBNT EdgeSwitch) #"));
        }
        System.out.println(Arrays.toString(samples));
    }
}
