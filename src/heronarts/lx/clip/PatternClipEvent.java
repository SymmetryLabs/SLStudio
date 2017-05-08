package heronarts.lx.clip;

import heronarts.lx.LXChannel;
import heronarts.lx.LXPattern;

public class PatternClipEvent extends LXClipEvent {

    public final LXPattern pattern;
    public final LXChannel channel;

    PatternClipEvent(LXClipLane lane, LXChannel channel, LXPattern pattern) {
        super(lane, pattern);
        this.pattern = pattern;
        this.channel = channel;
    }

    @Override
    public void execute() {
        this.channel.goPattern(this.pattern);
    }
}
