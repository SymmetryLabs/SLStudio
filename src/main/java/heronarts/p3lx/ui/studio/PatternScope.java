/**
 * Shared UI flag that controls where "add effect/warp" actions in the
 * effect/warp managers attach the new component.
 * Supports: Channel → Pattern
 */
package heronarts.p3lx.ui.studio;

public class PatternScope {
    public enum EffectScope {
        CHANNEL,    // Effects apply to entire channel (all banks)
        PATTERN,    // Effects apply to focused pattern only
        BANK        // Effects apply to all patterns in focused bank
    }

    /**
     * Current scope for adding effects/warps.
     * Cycles: Channel → Pattern
     */
    public static volatile EffectScope currentScope = EffectScope.CHANNEL;

    /**
     * When scope is BANK, this indicates which bank index to target.
     * -1 means use the focused bank.
     */
    public static volatile int targetBankIndex = -1;

    /**
     * Legacy flag for backward compatibility.
     * When true, behaves as if currentScope == PATTERN.
     */
    @Deprecated
    public static volatile boolean addToFocusedPattern = false;

    /**
     * Get the display label for the current scope.
     */
    public static String getScopeLabel() {
        switch (currentScope) {
            case CHANNEL:
                return "Effect: Channel";
            case PATTERN:
                return "Effect: Pattern";
            case BANK:
                return "Effect: Bank";
            default:
                return "Effect: Channel";
        }
    }

    /**
     * Cycle to the next scope. Order: Channel → Pattern → Channel
     */
    public static void cycleScope() {
        targetBankIndex = -1;
        if (currentScope == EffectScope.CHANNEL) {
            currentScope = EffectScope.PATTERN;
            addToFocusedPattern = true;
        } else {
            currentScope = EffectScope.CHANNEL;
            addToFocusedPattern = false;
        }
    }
}
