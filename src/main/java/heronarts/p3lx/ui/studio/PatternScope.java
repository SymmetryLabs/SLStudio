/**
 * Shared UI flag that controls where "add effect/warp" actions in the
 * effect/warp managers attach the new component.
 * Supports: Channel → Pattern → Bank1 → Bank2 → ...
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
     * Cycles: Channel → Pattern → Bank (for each bank in channel)
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
                if (targetBankIndex >= 0) {
                    return "Effect: Bank " + (targetBankIndex + 1);
                }
                return "Effect: Bank";
            default:
                return "Effect: Channel";
        }
    }

    /**
     * Cycle to the next scope. Order: Channel → Pattern → Bank1 → Bank2 → ... → Channel
     * @param numBanks Total number of banks in the focused channel
     */
    public static void cycleScope(int numBanks) {
        switch (currentScope) {
            case CHANNEL:
                currentScope = EffectScope.PATTERN;
                targetBankIndex = -1;
                addToFocusedPattern = true;
                break;
            case PATTERN:
                if (numBanks > 0) {
                    currentScope = EffectScope.BANK;
                    targetBankIndex = 0;
                    addToFocusedPattern = false;
                } else {
                    currentScope = EffectScope.CHANNEL;
                    targetBankIndex = -1;
                    addToFocusedPattern = false;
                }
                break;
            case BANK:
                if (targetBankIndex + 1 >= numBanks) {
                    // Wrap back to channel
                    currentScope = EffectScope.CHANNEL;
                    targetBankIndex = -1;
                    addToFocusedPattern = false;
                } else {
                    // Move to next bank
                    targetBankIndex++;
                }
                break;
        }
    }
}
