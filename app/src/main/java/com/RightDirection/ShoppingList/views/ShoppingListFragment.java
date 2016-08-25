package com.RightDirection.ShoppingList.views;

import android.graphics.Rect;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.helpers.ListAdapter;
import com.RightDirection.ShoppingList.interfaces.IObserver;

public class ShoppingListFragment extends android.app.ListFragment implements IObserver{

    @Override
    public void layoutHasDrawn() {
        // Необходимо сместить кнопку сохранения списка, если она пересекается с одной или несколькими
        // кнопками удаления конкретного элемента списка
        Button btnShoppingListSave = (Button) getActivity().findViewById(R.id.btnShoppingListSave);
        if (btnShoppingListSave == null){
            // Кнопка сохранения списка в активности отсутствует, дальше обрабатывать нет смысла
            return;
        }

        ListAdapter listAdapter = (ListAdapter) getListAdapter();
        boolean isIntersect = false;
        for(int i = 0; i < listAdapter.getCount(); ++i) {
            View view = getVisibleViewByPosition(i);
            if (view != null) {
                ImageView imgDelete = (ImageView) view.findViewById(R.id.imgDelete);
                if (imgDelete != null && isViewsIntersect(btnShoppingListSave, imgDelete)) {
                    // Нижнюю панель кнопок необходимо сместить
                    isIntersect = true;
                    // Далее проверять не имеет смысла
                    break;
                }
            }
        }

        if (isIntersect){
            setButtonsPanelLeftPadding(getResources().getDimensionPixelSize(R.dimen.save_button_margin));
        }
        else{
            setButtonsPanelLeftPadding(0);
        }
    }

    private View getVisibleViewByPosition(int pos) {
        ListView listView = getListView();

        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos >= firstListItemPosition && pos <= lastListItemPosition) {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
        else{
            return null;
        }
    }

    private boolean isViewsIntersect(View v1, View v2){

        final int[] location = new int[2];

        v1.getLocationInWindow(location);
        Rect rect1 = new Rect(location[0], location[1], location[0] + v1.getWidth(), location[1] + v1.getHeight());

        v2.getLocationInWindow(location);
        Rect rect2 = new Rect(location[0], location[1], location[0] + v2.getWidth(), location[1] + v2.getHeight());

        return rect1.intersect(rect2);
    }

    private void setButtonsPanelLeftPadding(int leftPadding){
        RelativeLayout buttonsPanel = (RelativeLayout)getActivity().findViewById(R.id.buttonsPanel);
        if (buttonsPanel != null) buttonsPanel.setPadding(0, 0, leftPadding, 0);
    }
}

