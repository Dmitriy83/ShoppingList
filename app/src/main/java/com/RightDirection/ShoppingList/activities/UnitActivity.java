package com.RightDirection.ShoppingList.activities;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.RightDirection.ShoppingList.R;
import com.RightDirection.ShoppingList.enums.EXTRAS_KEYS;
import com.RightDirection.ShoppingList.models.Unit;
import com.RightDirection.ShoppingList.utils.Utils;

public class UnitActivity extends BaseActivity{

    private Unit mUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unit_editing);

        if (savedInstanceState != null){
            // Восстановим объект из сохраненных значений
            mUnit = savedInstanceState.getParcelable(EXTRAS_KEYS.UNIT.getValue());
        }else{
            // Получим значения из переданных параметров
            mUnit = getIntent().getParcelableExtra(EXTRAS_KEYS.UNIT.getValue());
        }

        if (mUnit == null) {
            mUnit = new Unit(Utils.EMPTY_ID, "", "");
            mUnit.isNew = true;
        }

        // Если это новый элемент, то сразу отобразим клавиатуру для ввода наименования
        if (mUnit.isNew){
            getWindow().setSoftInputMode(getWindow().getAttributes().softInputMode
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        EditText etUnitName = (EditText) findViewById(R.id.etName);
        if (etUnitName != null) etUnitName.setText(mUnit.getName());
        EditText etUnitShortName = (EditText) findViewById(R.id.etShortName);
        if (etUnitShortName != null) etUnitShortName.setText(mUnit.getShortName());

        // Добавим обработчики кликов по кнопкам
        Button btnSave = (Button)findViewById(R.id.btnSave);
        if (btnSave != null) {
            // Исключим вывод всего текста прописными (для Android старше 4)
            btnSave.setTransformationMethod(null);
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { onBtnSaveClick();  }
            });
        }

        // Установим заголовок формы
        if (mUnit.isNew){
            setTitle(getString(R.string.new_unit));
        }
        else{
            setTitle(getString(R.string.unit_title));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(EXTRAS_KEYS.UNIT.getValue(), mUnit);
        super.onSaveInstanceState(outState);
    }

    private void onBtnSaveClick() {
        EditText etUnitName = (EditText) findViewById(R.id.etName);
        EditText etUnitShortName = (EditText) findViewById(R.id.etShortName);
        if (etUnitName != null && etUnitShortName != null) {
            mUnit.setName(etUnitName.getText().toString());
            mUnit.setShortName(etUnitShortName.getText().toString());

            if (mUnit.isNew) {
                mUnit.addToDB(getApplicationContext());
            } else {
                mUnit.updateInDB(getApplicationContext());
            }
        }

        finish();
    }
}

