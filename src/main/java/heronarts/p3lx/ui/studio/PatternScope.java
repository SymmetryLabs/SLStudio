/**
 * Shared UI flag that controls whether "add effect/warp" actions in the
 * effect/warp managers attach the new component to the channel-level chain
 * (the original behavior) or to the focused pattern's per-pattern chain.
 */
package heronarts.p3lx.ui.studio;

public class PatternScope {
    /**
     * When true, "add effect/warp" actions target the focused pattern's per-pattern
     * chain instead of the channel's chain. Defaults to false for backward
     * compatibility.
     */
    public static volatile boolean addToFocusedPattern = false;
}
