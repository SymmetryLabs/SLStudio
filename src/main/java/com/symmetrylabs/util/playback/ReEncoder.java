package com.symmetrylabs.util.playback;

import ar.com.hjg.pngj.*;
import jogamp.opengl.util.pngj.ImageLine;

import java.io.File;

public class ReEncoder {
    PngReader pngr;
    PngWriter pngw;

    public ReEncoder(){
        String readName = "test.png";

        String dataPath = System.getProperty("user.home") + "/symmetrylabs/software/SLStudio/shows/pilots/render/";
        String pngPath = dataPath + readName;
        File inFile = new File(pngPath);
        if (inFile.isFile() && inFile.getName().endsWith(".png")) {
            pngr = new PngReader(inFile);
        }

        assert pngr != null;
        ImageInfo r_info = pngr.imgInfo;
        ImageInfo info = new ImageInfo(r_info.cols, r_info.rows, 8, true);
        String pngOutPath = dataPath + readName + "out.png";
        File outFile = new File(pngOutPath);
        pngw = new PngWriter(outFile, info);

    }

    public void transcode() {
        assert pngr != null;
        assert pngw != null;
        int row_index = 0;
        int last_empty_index = 0;

        class ImgLineSando {
            ImageLine lines[];

            ImgLineSando() {
                lines = new ImageLine[3];
            }
        }

        while (pngr.hasMoreRows()) {
            IImageLine l1 = pngr.readRow();
            int[] fourValArray = ((ImageLineInt) l1).getScanline();
            long sum = 0;
            for (int i = 0; i < fourValArray.length; i += 4) {
//                ccs[i/4] = ((fourValArray[i+3] & 0xff) << 24) | ((fourValArray[i] & 0xff) << 16) | ((fourValArray[i+1] & 0xff) << 8) | (fourValArray[i + 2] & 0xff);
                //hmm... seems like alpha was fucked above.  Let's just always make it fully opaque.
                sum += ((fourValArray[i] & 0xff) << 16) | ((fourValArray[i + 1] & 0xff) << 8) | (fourValArray[i + 2] & 0xff);
            }

            // UNFINISHED - logic to check if next and previous row were empty then we should average and fill in the blank here.
            if (sum == 0) {
                if (last_empty_index == row_index - 1) {
                    last_empty_index = row_index;
                }

                pngw.writeRow(l1);
                row_index++;
            }
        }
    }
}
