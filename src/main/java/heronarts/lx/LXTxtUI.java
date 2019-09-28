package heronarts.lx;

import com.symmetrylabs.util.CaptionSource;

import java.util.WeakHashMap;

public class LXTxtUI extends LXComponent implements LXLoopTask{
    WeakHashMap<CaptionSource, Object> sources = new WeakHashMap<>();
    static final String SPACES = "                                                   ";

    public LXTxtUI(LX lx) {
        super(lx);
    }

    public synchronized void addSource(CaptionSource source) {
        sources.put(source, new Object());
    }

    public synchronized void removeSource(CaptionSource source) {
        sources.remove(source);
    }

    public synchronized String getText() {
        String result = "";
        for (CaptionSource source : sources.keySet()) {
            String caption = source.getCaption();
            if (caption != null && !caption.isEmpty()) {
                String prefix = source.getClass().getSimpleName() + " - ";
                result = result.trim() + "\n" + prefix +
                    caption.replace("\n", "\n" + SPACES.substring(0, prefix.length()));
            }
        }
        return result.trim();
    }

    static int loop_count = 0;
    @Override
    public void loop(double deltaMs) {
        loop_count++;
        if(loop_count%100 == 0){
            if (!getText().equals("")){
                System.out.println(getText());
            }
        }
    }
}
