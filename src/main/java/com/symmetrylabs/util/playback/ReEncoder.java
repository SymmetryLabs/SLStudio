package com.symmetrylabs.util.playback;

import ar.com.hjg.pngj.IImageLine;
import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.PngWriter;

import java.io.File;

public class ReEncoder {
    PngReader pngr;
    PngWriter pngw;

    public ReEncoder(){
        String readName = "test.png";

        String dataPath = "/Users/symmetry/symmetrylabs/software/SLStudio/shows/pilots/render/";
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
        while (pngr.hasMoreRows()) {
            IImageLine l1 = pngr.readRow();
            pngw.writeRow(l1);
        }
    }
}
