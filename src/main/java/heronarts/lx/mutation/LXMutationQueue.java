package heronarts.lx.mutation;

import heronarts.lx.*;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.warp.LXWarp;

import java.lang.reflect.InvocationTargetException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class LXMutationQueue extends LXComponent {
    public static class MutationRequest {
        public final Mutation mutation;
        public final Consumer<Exception> callback;

        public MutationRequest(Mutation mutation, Consumer<Exception> callback) {
            this.mutation = mutation;
            this.callback = callback;
        }
    }

    private final LX lx;

    public final BooleanParameter enabled = new BooleanParameter("enabled", false);
    public final StringParameter server = new StringParameter("server", "localhost");

    private final Queue<MutationRequest> mutations;

    public final LXMutationSender sender;

    public final String[] lastMutations = new String[10];
    public int mutationBufferNext = 0;

    public LXMutationQueue(LX lx) {
        this.lx = lx;
        sender = new LXMutationSender();
        mutations = new ConcurrentLinkedQueue<>();

        addParameter(enabled);
        addParameter(server);
    }

    public void enqueue(MutationRequest mut) {
        sender.send(mut.mutation);
        mutations.add(mut);
    }

    public void enqueue(Mutation mut) {
        enqueue(new MutationRequest(mut, null));
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

    public void enqueue(AddEffect.Builder b) {
        enqueue(Mutation.newBuilder().setAddEffect(b).build());
    }

    public void enqueue(AddWarp.Builder b) {
        enqueue(Mutation.newBuilder().setAddWarp(b).build());
    }

    public void enqueue(RemovePattern.Builder b) {
        enqueue(Mutation.newBuilder().setRemovePattern(b).build());
    }

    public void enqueue(RemoveEffect.Builder b) {
        enqueue(Mutation.newBuilder().setRemoveEffect(b).build());
    }

    public void enqueue(RemoveWarp.Builder b) {
        enqueue(Mutation.newBuilder().setRemoveWarp(b).build());
    }

    // must be called from engine thread!
    public void dispatchAll() {
        MutationRequest m;
        do {
            m = mutations.poll();
            if (m != null) {
                try {
                    dispatch(m);
                } catch (Exception e) {
                    if (m.callback != null) {
                        System.err.println("caught error when applying mutation " + m);
                        e.printStackTrace();
                        m.callback.accept(e);
                    } else {
                        throw e;
                    }
                }
            }
        } while (m != null);
    }

    protected void dispatch(MutationRequest mr) {
        final Mutation mut = mr.mutation;
        final LXEngine e = lx.engine;
        LXLook look;
        LXBus bus;
        LXChannel chan;
        LXPattern pattern;
        LXEffect effect;
        LXWarp warp;
        int index;

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
            case ADDEFFECT:
                look = e.looks.get(mut.getAddEffect().getLook());
                index = mut.getAddEffect().getChannel();
                bus = index < 0 ? lx.engine.masterChannel : look.getChannel(index);
                effect = instantiateComponent(LXEffect.class, mut.getAddEffect().getEffectType());
                if (effect != null) {
                    bus.addEffect(effect);
                }
                break;
            case ADDWARP:
                look = e.looks.get(mut.getAddWarp().getLook());
                index = mut.getAddWarp().getChannel();
                bus = index < 0 ? lx.engine.masterChannel : look.getChannel(index);
                warp = instantiateComponent(LXWarp.class, mut.getAddWarp().getWarpType());
                if (warp != null) {
                    bus.addWarp(warp);
                }
                break;
            case REMOVEPATTERN:
                look = e.looks.get(mut.getRemovePattern().getLook());
                chan = look.getChannel(mut.getRemovePattern().getChannel());
                chan.removePattern(chan.getPattern(mut.getRemovePattern().getPattern()));
                break;
            case REMOVEEFFECT:
                look = e.looks.get(mut.getRemoveEffect().getLook());
                index = mut.getRemoveEffect().getChannel();
                bus = index < 0 ? lx.engine.masterChannel : look.getChannel(index);
                bus.removeEffect(bus.getEffect(mut.getRemoveEffect().getEffect()));
                break;
            case REMOVEWARP:
                look = e.looks.get(mut.getRemoveWarp().getLook());
                index = mut.getRemoveWarp().getChannel();
                bus = index < 0 ? lx.engine.masterChannel : look.getChannel(index);
                bus.removeWarp(bus.getWarp(mut.getRemoveWarp().getWarp()));
                break;
            default:
                System.out.println("Unsupported mutation type in queue: " + mut.getValueCase());
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
