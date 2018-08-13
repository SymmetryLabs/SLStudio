package com.symmetrylabs.util;

import com.symmetrylabs.slstudio.model.StripsTopology;
import com.symmetrylabs.slstudio.model.StripsTopology.Bundle;
import com.symmetrylabs.slstudio.model.StripsTopology.Dir;
import com.symmetrylabs.slstudio.model.StripsTopology.Junction;
import com.symmetrylabs.slstudio.model.StripsTopology.Sign;

import java.util.*;

/**
 * Finds sets of connected bundles and junctions in a StripsTopology object.
 */
public class StripsTopologyComponents {
    public static class ConnectedComponent {
        public final Set<Junction> junctions;

        private ConnectedComponent() {
            junctions = new HashSet<>();
        }
    }

    private final List<ConnectedComponent> components;

    public StripsTopologyComponents(StripsTopology topo) {
        components = new ArrayList<>();
        findComponents(topo);
    }

    public List<ConnectedComponent> getComponents() {
        return Collections.unmodifiableList(components);
    }

    private void findComponents(StripsTopology topo) {
        HashSet<Junction> all = new HashSet<>(topo.junctions);
        HashSet<Junction> open = new HashSet<>();

        while (!all.isEmpty()) {
            ConnectedComponent cc = new ConnectedComponent();

            open.clear();
            open.add(pop(all));

            while (!open.isEmpty()) {
                Junction j = pop(open);
                for (Dir d : Dir.values()) {
                    for (Sign s : Sign.values()) {
                        Bundle b = j.get(d, s);
                        if (b == null) {
                            continue;
                        }
                        Junction n = b.get(s);
                        if (cc.junctions.contains(n))
                            continue;
                        open.add(n);
                    }
                }
                cc.junctions.add(j);
            }

            all.removeAll(cc.junctions);
            components.add(cc);
        }
    }

    private Junction pop(HashSet<Junction> js) {
        /* This is the only way to remove-and-return a value from a HashSet */
        Junction res;
        Iterator<Junction> jiter = js.iterator();
        res = jiter.next();
        jiter.remove();
        return res;
    }
}
