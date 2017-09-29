package com.RightDirection.ShoppingList.activities;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.models.Product;
import com.RightDirection.ShoppingList.utils.SL_ContentProvider;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Date;

public class PriceChangeHistoryActivity extends BaseActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor>{
    private final String TAG = PriceChangeHistoryActivity.class.getSimpleName();
    private Product mProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_change_history_grath);

        if (savedInstanceState != null) {
            // Восстановим объект из сохраненных значений (при смене ориентации экрана)
            mProduct = savedInstanceState.getParcelable(EXTRAS_KEYS.PRODUCT.getValue());
            getLoaderManager().restartLoader(0, null, this);
        } else {
            // Получим значения из переданных параметров
            mProduct = getIntent().getParcelableExtra(EXTRAS_KEYS.PRODUCT.getValue());
            getLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(EXTRAS_KEYS.PRODUCT.getValue(), mProduct);
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, SL_ContentProvider.PRICE_CHANGE_HISTORY_URI,
                null, SL_ContentProvider.KEY_PRODUCT_ID + " = ?" , new String[]{String.valueOf(mProduct.getId())}, SL_ContentProvider.KEY_DATE);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null) return;
        GraphView graph = (GraphView) findViewById(R.id.gvPriceChangeHistory);
        DataPoint[] dataPoints = new DataPoint[data.getCount()];
        int i = 0;
        int keyDateIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_DATE);
        int keyPriceIndex = data.getColumnIndexOrThrow(SL_ContentProvider.KEY_PRICE);
        while (data.moveToNext()) {
            Date date = new Date(data.getLong(keyDateIndex));
            dataPoints[i] = new DataPoint(date, data.getFloat(keyPriceIndex));
            i++;
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
        graph.addSeries(series);
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getApplicationContext()));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // так как мало места
        //graph.getGridLabelRenderer().setHumanRounding(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}
}
