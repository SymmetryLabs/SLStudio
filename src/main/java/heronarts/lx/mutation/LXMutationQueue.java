package heronarts.lx.mutation;

import heronarts.lx.*;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.parameter.BooleanParameter;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import static heronarts.lx.mutation.Mutations.*;

public class LXMutationQueue extends LXComponent {
    private final LX lx;

    public final BooleanParameter enabled = new BooleanParameter("enabled", false);
    public final StringParameter server = new StringParameter("server", "localhost");

    private final Queue<Mutation> mutations;

    public final String[] lastMutations = new String[10];
    public int mutationBufferNext = 0;

    public LXMutationQueue(LX lx) {
        this.lx = lx;
        mutations = new ConcurrentLinkedQueue<>();

        addParameter(enabled);
        addParameter(server);
    }

    public void enqueue(Mutation mut) {
        mutations.add(mut);
    }

    public void enqueue(AddChannel.Builder b) {
        enqueue(Mutation.newBuilder().setAddChannel(b).build());
    }

    public void enqueue(RemoveChannel.Builder b) {
        enqueue(Mutation.newBuilder().setRemoveChannel(b).build());
    }

    public void enqueue(AddPattern.Builder b) {
        enqueue(Mutation.newBuilder().setAddPattern(b).build());
    }

    // must be called from engine thread!
    public void dispatchAll() {
        Mutation m;
        do {
            m = mutations.poll();
            if (m != null) {
                dispatch(m);
            }
        } while (m != null);
    }

    protected void dispatch(Mutation mut) {
        LXEngine e = lx.engine;
        LXLook look;
        LXChannel chan;
        LXPattern pattern;

        switch (mut.getValueCase()) {
            case ADDLOOK:
                e.addLook();
                break;
            case REMOVELOOK:
                look = e.looks.get(mut.getRemoveLook().getLook());
                e.removeLook(look);
                break;
            case ADDCHANNEL:
                look = e.looks.get(mut.getAddChannel().getLook());
                chan = look.addChannel();
                look.setFocusedChannel(chan);
                break;
            case REMOVECHANNEL:
                look = e.looks.get(mut.getRemoveChannel().getLook());
                chan = look.getChannel(mut.getRemoveChannel().getChannel());
                look.removeChannel(chan);
                break;
            case ADDPATTERN:
                look = e.looks.get(mut.getAddPattern().getLook());
                chan = look.getChannel(mut.getAddPattern().getChannel());
                pattern = instantiateComponent(LXPattern.class, mut.getAddPattern().getPatternType());
                if (pattern != null) {
                    chan.addPattern(pattern);
                }
                break;
        }

        lastMutations[mutationBufferNext] = mut.toString();
        mutationBufferNext = (mutationBufferNext + 1) % lastMutations.length;
    }

    protected <T> T instantiateComponent(Class<T> resClass, String className) {
        Class<? extends Object> cls;
        try {
            cls = getClass().getClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        Object inst = null;
        try {
            inst = cls.getConstructor(LX.class).newInstance(lx);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        try {
            return resClass.cast(inst);
        } catch (ClassCastException e) {
            e.printStackTrace();
            return null;
        }
    }
}
