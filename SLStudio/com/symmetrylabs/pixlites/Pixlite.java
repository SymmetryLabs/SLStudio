package com.symmetrylabs.pixlites;

import com.symmetrylabs.mappings.Sun10BackBottomPixliteConfig;
import com.symmetrylabs.mappings.Sun10BackTopPixliteConfig;
import com.symmetrylabs.mappings.Sun10FrontBottomPixliteConfig;
import com.symmetrylabs.mappings.Sun10FrontTopPixliteConfig;
import com.symmetrylabs.mappings.Sun1BackPixliteConfig;
import com.symmetrylabs.mappings.Sun1FrontPixliteConfig;
import com.symmetrylabs.mappings.Sun2BackPixliteConfig;
import com.symmetrylabs.mappings.Sun2FrontPixliteConfig;
import com.symmetrylabs.mappings.Sun3BackTopPixliteConfig;
import com.symmetrylabs.mappings.Sun3FrontTopPixliteConfig;
import com.symmetrylabs.mappings.Sun4BackPixliteConfig;
import com.symmetrylabs.mappings.Sun4FrontPixliteConfig;
import com.symmetrylabs.mappings.Sun5BackTopPixliteConfig;
import com.symmetrylabs.mappings.Sun5FrontTopPixliteConfig;
import com.symmetrylabs.mappings.Sun6BackBottomPixliteConfig;
import com.symmetrylabs.mappings.Sun6BackTopPixliteConfig;
import com.symmetrylabs.mappings.Sun6FrontBottomPixliteConfig;
import com.symmetrylabs.mappings.Sun6FrontTopPixliteConfig;
import com.symmetrylabs.mappings.Sun7BackBottomPixliteConfig;
import com.symmetrylabs.mappings.Sun7BackTopPixliteConfig;
import com.symmetrylabs.mappings.Sun7FrontBottomPixliteConfig;
import com.symmetrylabs.mappings.Sun7FrontTopPixliteConfig;
import com.symmetrylabs.mappings.Sun8BackBottomPixliteConfig;
import com.symmetrylabs.mappings.Sun8BackTopPixliteConfig;
import com.symmetrylabs.mappings.Sun8FrontBottomPixliteConfig;
import com.symmetrylabs.mappings.Sun8FrontTopPixliteConfig;
import com.symmetrylabs.mappings.Sun9BackBottomPixliteConfig;
import com.symmetrylabs.mappings.Sun9BackTopPixliteConfig;
import com.symmetrylabs.mappings.Sun9FrontBottomPixliteConfig;
import com.symmetrylabs.mappings.Sun9FrontTopPixliteConfig;
import com.symmetrylabs.model.Slice;
import heronarts.lx.LX;
import heronarts.lx.output.LXOutputGroup;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class Pixlite extends LXOutputGroup {
    public Slice slice;
    public final String ipAddress;

    public Pixlite(LX lx, String ipAddress, Slice slice) {
        super(lx);
        this.ipAddress = ipAddress;
        this.slice = slice;

        if (slice == null) {
            IllegalArgumentException e = new IllegalArgumentException("slice is null for " + ipAddress);
            e.printStackTrace();
            throw new IllegalArgumentException("slice is null for " + ipAddress);
        }
        if (slice.id == null) throw new IllegalArgumentException("slice.id is null for " + ipAddress);

        try {
            // com.symmetrylabs.model.Sun 1
            if (slice.id.equals("sun1_top_front")) {
                new Sun1FrontPixliteConfig(lx, slice, ipAddress, this);
            }
            if (slice.id.equals("sun1_top_back")) {
                new Sun1BackPixliteConfig(lx, slice, ipAddress, this);
            }

            // com.symmetrylabs.model.Sun 2
            if (slice.id.equals("sun2_top_front")) {
                new Sun2FrontPixliteConfig(lx, slice, ipAddress, this);
            }
            if (slice.id.equals("sun2_top_back")) {
                new Sun2BackPixliteConfig(lx, slice, ipAddress, this);
            }

            // com.symmetrylabs.model.Sun 3
            if (slice.id.equals("sun3_top_front")) {
                new Sun3FrontTopPixliteConfig(lx, slice, ipAddress, this);
            }
            if (slice.id.equals("sun3_top_back")) {
                new Sun3BackTopPixliteConfig(lx, slice, ipAddress, this);
            }

            // com.symmetrylabs.model.Sun 4
            if (slice.id.equals("sun4_top_front")) {
                new Sun4FrontPixliteConfig(lx, slice, ipAddress, this);
            }
            if (slice.id.equals("sun4_top_back")) {
                new Sun4BackPixliteConfig(lx, slice, ipAddress, this);
            }

            // com.symmetrylabs.model.Sun 5
            if (slice.id.equals("sun5_top_front")) {
                new Sun5FrontTopPixliteConfig(lx, slice, ipAddress, this);
            }
            if (slice.id.equals("sun5_top_back")) {
                new Sun5BackTopPixliteConfig(lx, slice, ipAddress, this);
            }

            // com.symmetrylabs.model.Sun 6
            if (slice.id.equals("sun6_top_front")) {
                new Sun6FrontTopPixliteConfig(lx, slice, ipAddress, this);
            }
            if (slice.id.equals("sun6_bottom_front")) {
                new Sun6FrontBottomPixliteConfig(lx, slice, ipAddress, this);
            }
            if (slice.id.equals("sun6_top_back")) {
                new Sun6BackTopPixliteConfig(lx, slice, ipAddress, this);
            }
            if (slice.id.equals("sun6_bottom_back")) {
                new Sun6BackBottomPixliteConfig(lx, slice, ipAddress, this);
            }

            // com.symmetrylabs.model.Sun 7
            if (slice.id.equals("sun7_top_front")) {
                new Sun7FrontTopPixliteConfig(lx, slice, ipAddress, this);
            }
            if (slice.id.equals("sun7_bottom_front")) {
                new Sun7FrontBottomPixliteConfig(lx, slice, ipAddress, this);
            }
            if (slice.id.equals("sun7_top_back")) {
                new Sun7BackTopPixliteConfig(lx, slice, ipAddress, this);
            }
            if (slice.id.equals("sun7_bottom_back")) {
                new Sun7BackBottomPixliteConfig(lx, slice, ipAddress, this);
            }

            // com.symmetrylabs.model.Sun 8
            if (slice.id.equals("sun8_top_front")) {
                new Sun8FrontTopPixliteConfig(lx, slice, ipAddress, this);
            }
            if (slice.id.equals("sun8_bottom_front")) {
                new Sun8FrontBottomPixliteConfig(lx, slice, ipAddress, this);
            }
            if (slice.id.equals("sun8_top_back")) {
                new Sun8BackTopPixliteConfig(lx, slice, ipAddress, this);
            }
            if (slice.id.equals("sun8_bottom_back")) {
                new Sun8BackBottomPixliteConfig(lx, slice, ipAddress, this);
            }

            // com.symmetrylabs.model.Sun 9
            if (slice.id.equals("sun9_top_front")) {
                new Sun9FrontTopPixliteConfig(lx, slice, ipAddress, this);
            }
            if (slice.id.equals("sun9_bottom_front")) {
                new Sun9FrontBottomPixliteConfig(lx, slice, ipAddress, this);
            }
            if (slice.id.equals("sun9_top_back")) {
                new Sun9BackTopPixliteConfig(lx, slice, ipAddress, this);
            }
            if (slice.id.equals("sun9_bottom_back")) {
                new Sun9BackBottomPixliteConfig(lx, slice, ipAddress, this);
            }

            // com.symmetrylabs.model.Sun 10
            if (slice.id.equals("sun10_top_front")) {
                new Sun10FrontTopPixliteConfig(lx, slice, ipAddress, this);
            }
            if (slice.id.equals("sun10_bottom_front")) {
                new Sun10FrontBottomPixliteConfig(lx, slice, ipAddress, this);
            }
            if (slice.id.equals("sun10_top_back")) {
                new Sun10BackTopPixliteConfig(lx, slice, ipAddress, this);
            }
            if (slice.id.equals("sun10_bottom_back")) {
                new Sun10BackBottomPixliteConfig(lx, slice, ipAddress, this);
            }

            // // com.symmetrylabs.model.Sun 11
            // if(slice.id.equals("sun11_top_front")) {
            //   new com.symmetrylabs.mappings.Sun11FrontTopPixliteConfig(lx, slice, ipAddress, this);
            // }
            // if(slice.id.equals("sun11_bottom_front")) {
            //   new com.symmetrylabs.mappings.Sun11FrontBottomPixliteConfig(lx, slice, ipAddress, this);
            // }
            // if(slice.id.equals("sun11_top_back")) {
            //   new com.symmetrylabs.mappings.Sun11BackTopPixliteConfig(lx, slice, ipAddress, this);
            // }
            // if(slice.id.equals("sun11_bottom_back")) {
            //   new com.symmetrylabs.mappings.Sun11BackBottomPixliteConfig(lx, slice, ipAddress, this);
            // }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
