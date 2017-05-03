package heronarts.lx.clip;

import heronarts.lx.LXChannel;
import heronarts.lx.LXPattern;

public class PatternClipEvent extends LXClipEvent {

    private final LXPattern pattern;
    private final LXChannel channel;

    PatternClipEvent(LXChannelClip clip, LXPattern pattern) {
        super(clip, pattern);
        this.pattern = pattern;
        this.channel = clip.channel;
    }

    @Override
    public void execute() {
        this.channel.goPattern(this.pattern);
    }
}
