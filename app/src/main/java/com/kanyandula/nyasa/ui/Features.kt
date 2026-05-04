package com.kanyandula.nyasa.ui

/**
 * Compile-time feature flags. Flip and rebuild to enable.
 *
 * When re-enabling [ADAPTIVE_LAYOUT_ENABLED], re-run the Phase 3 manual verification matrix
 * in `docs/superpowers/specs/2026-05-02-h7-phase3a-3b-design.md` §7.2 on a Pixel Fold or
 * Pixel Tablet (≥ 840dp width) before merging.
 */
internal object Features {
    /**
     * Gates H7 Phase 3a+3b — the Expanded-class hero+grid+rail layout on Home and the
     * 2-column grid on Search. Off by default; the runtime path falls back to Phase 2's
     * dual-pane scaffold on Expanded class. See PR #67 for the design contract.
     */
    const val ADAPTIVE_LAYOUT_ENABLED: Boolean = false
}
