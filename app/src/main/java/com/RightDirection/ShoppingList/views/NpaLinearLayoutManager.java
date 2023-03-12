package com.RightDirection.ShoppingList.views;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * No Predictive Animations LinearLayoutManager
 */
public class NpaLinearLayoutManager extends LinearLayoutManager {
    /**
     * Disable predictive animations. There is a bug in RecyclerView which causes views that
     * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
     * adapter size has decreased since the ViewHolder was recycled.
     */
    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }

    public NpaLinearLayoutManager(Context context) {
        super(context);
    }
}
