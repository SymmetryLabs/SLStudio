package art.lookingup;

import art.lookingup.ui.UIPixliteConfig;
import com.symmetrylabs.shows.firefly.FireflyShow;
import heronarts.lx.output.LXDatagramOutput;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.ArtNetDatagram;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Handles output from our 'colors' buffer to our DMX lights.  Currently using E1.31.
 */
public class KaledoscopeOutput {
    private static final Logger logger = Logger.getLogger(KaledoscopeOutput.class.getName());

    public static final int MAX_OUTPUTS = 32;  // 32 outputs in expanded mode.
    public static LXDatagramOutput butterflyDatagramOutput = null;
    public static LXDatagramOutput flowerDatagramOutput = null;
    public static List<List<Integer>> outputs = new ArrayList<List<Integer>>(MAX_OUTPUTS);

    public static void configurePixliteOutput(LX lx) {
        List<ArtNetDatagram> datagrams = new ArrayList<ArtNetDatagram>();
        List<Integer> countsPerOutput = new ArrayList<Integer>();
        // For each output, track the number of points per panel type so we can log the details to help
        // with output verification.

        String butterflyPixliteIp = FireflyShow.pixliteConfig.butterflyPixliteIp();
        int butterflyPixlitePort = FireflyShow.pixliteConfig.butterflyPixlitePort();
        logger.info("Butterfly ArtNet: " + butterflyPixliteIp + ":" + butterflyPixlitePort);

        String flowerPixliteIp = FireflyShow.pixliteConfig.flowerPixliteIp();
        int flowerPixlitePort = FireflyShow.pixliteConfig.flowerPixlitePort();
        logger.info("Flower ArtNet: " + flowerPixliteIp + ":" + flowerPixlitePort);

        // We need to separate butterfly stands from flower strands and route them to the appropriate pixlites.

        // For each non-empty mapping output parameter, collect all points in wire order from each strand listed.
        // Distribute all points across the necessary number of 170-led sized universes.  One strand corresponds to
        // one output.  A strand will have multiple universes.  A new strand should start a new universe.
        for (int strandType = 0; strandType < 2; strandType++) {
            String targetPixliteIp = butterflyPixliteIp;
            int targetPixlitePort = butterflyPixlitePort;
            if (strandType == 1) {
                targetPixliteIp = flowerPixliteIp;
                targetPixlitePort = flowerPixlitePort;
                logger.info("======= MAPPING FLOWERS ========");
            } else {
                logger.info("======= MAPPING BUTTERFLIES ========");
            }
            int curUniverseNum = 0;
            for (int outputNum = 0; outputNum < 16; outputNum++) {
                logger.info("Loading mapping for output " + (outputNum + 1));
                String strandIds = FireflyShow.mappingConfig.getStringParameter("output" + (outputNum + 1)).getString();
                logger.info("strand ids: " + strandIds);
                if (strandIds.length() > 0) {
                    List<LXPoint> pointsWireOrder = new ArrayList<LXPoint>();


                    // NOTE: We typically shouldn't have multiple strands mapped to a single output since the purpose of
                    // strands are to increase the FPS but we will support it to provide some install time flexibility.
                    String[] ids = strandIds.split(",");
                    for (int i = 0; i < ids.length; i++) {
                        int strandId = Integer.parseInt(ids[i]);
                        if (strandId < KaledoscopeModel.allStrands.size()) {
                            KaledoscopeModel.Strand strand = KaledoscopeModel.allStrands.get(strandId);
                            // The default construction of LED points in a strand is already in wire-order.
                            // On the first pass, we will only map butterfly strands.  We will do a second pass
                            // and map flower strands.
                            if (strandType == 0 && strand.strandType == KaledoscopeModel.Strand.StrandType.BUTTERFLY)
                                pointsWireOrder.addAll(strand.allPoints);
                            if (strandType == 1 && strand.strandType == KaledoscopeModel.Strand.StrandType.FLOWER)
                                pointsWireOrder.addAll(strand.allPoints);
                        }
                    }
                    // If we didn't add any points, this was the wrong strand type so we can just skip ahead to
                    // the next strand.  We don't want to continue below because we would be bumping our universe
                    // number each time we skipped a strand.
                    if (pointsWireOrder.size() == 0) {
                        logger.info("Skipping different strand type.");
                        continue;
                    }

                    int numUniversesThisWire = (int) Math.ceil((float) pointsWireOrder.size() / 170f);
                    int univStartNum = curUniverseNum;
                    int lastUniverseCount = pointsWireOrder.size() - 170 * (numUniversesThisWire - 1);
                    int maxLedsPerUniverse = (pointsWireOrder.size() > 170) ? 170 : pointsWireOrder.size();
                    int[] thisUniverseIndices = new int[maxLedsPerUniverse];
                    int curIndex = 0;
                    int curUnivOffset = 0;
                    for (LXPoint pt : pointsWireOrder) {
                        thisUniverseIndices[curIndex] = pt.index;
                        curIndex++;
                        if (curIndex == 170 || (curUnivOffset == numUniversesThisWire - 1 && curIndex == lastUniverseCount)) {
                            logger.info("Adding datagram: universe=" + (univStartNum + curUnivOffset) + " points=" + curIndex);
                            ArtNetDatagram datagram = new ArtNetDatagram(thisUniverseIndices, curIndex * 3, univStartNum + curUnivOffset);
                            try {
                                InetAddress address;
                                address = InetAddress.getByName(targetPixliteIp);
                                datagram.setAddress(address).setPort(targetPixlitePort);
                            } catch (UnknownHostException uhex) {
                                logger.log(Level.SEVERE, "Configuring ArtNet: " + targetPixliteIp + ":" + targetPixlitePort, uhex);
                            }
                            datagrams.add(datagram);
                            curUnivOffset++;
                            curIndex = 0;
                            if (curUnivOffset == numUniversesThisWire - 1) {
                                thisUniverseIndices = new int[lastUniverseCount];
                            } else {
                                thisUniverseIndices = new int[maxLedsPerUniverse];
                            }
                        }
                    }
                    curUniverseNum += curUnivOffset;
                }
            }

            LXDatagramOutput datagramOutput = null;

            try {
                datagramOutput = new LXDatagramOutput(lx);
                for (ArtNetDatagram datagram : datagrams) {
                    datagramOutput.addDatagram(datagram);
                }
                // We need to keep track of these individually so that we can restart the output properly on
                // reconfigures. This version of LX doesn't allow us to reference the children of an output
                // so we can't iterate them and remove them before rebuilding the output.
                if (strandType == 0)
                    butterflyDatagramOutput = datagramOutput;
                else
                    flowerDatagramOutput = datagramOutput;
          /*
          try {
              datagramOutput.addDatagram(new ArtSyncDatagram().setAddress(targetPixliteIp).setPort(targetPixlitePort));
          } catch (UnknownHostException uhex) {
              logger.log(Level.SEVERE, "Unknown host for ArtNet sync.", uhex);
          }
          */
            } catch (SocketException sex) {
                logger.log(Level.SEVERE, "Initializing LXDatagramOutput failed.", sex);
            }
            if (datagramOutput != null) {
                datagramOutput.enabled.setValue(true);
                lx.engine.output.addChild(datagramOutput);
            } else {
                logger.log(Level.SEVERE, "Did not configure output, error during LXDatagramOutput init");
            }
        }
    }
}
