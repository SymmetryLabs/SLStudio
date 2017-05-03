package heronarts.lx.clip;

import heronarts.lx.LXPattern;

public class PatternClipEvent extends LXClipEvent {

    private final LXPattern pattern;

    PatternClipEvent(LXClip clip, LXPattern pattern) {
        super(clip, pattern);
        this.pattern = pattern;
    }

    @Override
    public void execute() {
        this.clip.channel.goPattern(this.pattern);
    }
}
