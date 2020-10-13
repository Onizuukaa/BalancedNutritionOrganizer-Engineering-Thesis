package hfad.com.balancednutritionorganizer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

import hfad.com.balancednutritionorganizer.adapters.GroceryAdapter;
import hfad.com.balancednutritionorganizer.adapters.RecyclerViewComposeMealAdapter;
import hfad.com.balancednutritionorganizer.database_things.ComposedMealsDBHelper;
import hfad.com.balancednutritionorganizer.database_things.GroceryContract;
import hfad.com.balancednutritionorganizer.database_things.ComposedMealsColumns;
import hfad.com.balancednutritionorganizer.database_things.GroceryDBHelper;

import static java.lang.Integer.parseInt;

public class ComposeMealActivity extends AppCompatActivity {
    //SQLiteDatabase db;

    RecyclerView recyclerView;
    private SQLiteDatabase mDatabase, mDatabaseComposedMeals;
    private GroceryAdapter mAdapter;
    Cursor cursor;

    TextView textViewComposeMealKcal, textViewComposeMealCarbohydrates, textViewComposeMealGram,
            textViewComposeMealSugar, textViewComposeMealFats, textViewComposeMealSaturatedFats,
            textViewComposeMealProtein, textViewNoData;
    EditText editTextMealName;
    double caloriesSum, carbohydratesSum, sugarSum, fatsSum, saturatedFatsSum, proteinSum, gramSum;
    DecimalFormat format;
    EditText editText_removeItem;
    Button button_removeItem, button_removeAllItems;
    RecyclerViewComposeMealAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_meal);

        GroceryDBHelper dbHelper = new GroceryDBHelper(this);
        ComposedMealsDBHelper dbHelperComposedMeals = new ComposedMealsDBHelper(this);
        mDatabase = dbHelper.getWritableDatabase();
        mDatabaseComposedMeals = dbHelperComposedMeals.getWritableDatabase();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        format = new DecimalFormat("#.#");
        format.setDecimalSeparatorAlwaysShown(false);

        initViews();
        initRecyclerView();
        sumAndViewMacros();

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                removeItem((long) viewHolder.itemView.getTag());
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void removeItem(long id) {
        mDatabase.delete(GroceryContract.GroceryEntry.TABLE_NAME,
                GroceryContract.GroceryEntry._ID + "=" + id, null);
        mAdapter.swapCursor(getAllItems());

        cursor = getAllItems();
        caloriesSum = 0;
        carbohydratesSum = 0;
        sugarSum = 0;
        fatsSum = 0;
        saturatedFatsSum = 0;
        proteinSum = 0;
        gramSum = 0;

        while (cursor.moveToNext()) {
            caloriesSum += cursor.getDouble(2);
            carbohydratesSum += cursor.getDouble(3);
            sugarSum += cursor.getDouble(4);
            fatsSum += cursor.getDouble(5);
            saturatedFatsSum += cursor.getDouble(6);
            proteinSum += cursor.getDouble(7);
            gramSum += cursor.getDouble(8);
        }
        textViewComposeMealKcal.setText(format.format(caloriesSum) + "\nKCAL");
        textViewComposeMealCarbohydrates.setText(format.format(carbohydratesSum) + "\ncarbohydrates");
        textViewComposeMealSugar.setText(format.format(sugarSum) + "\nsugar");
        textViewComposeMealFats.setText(format.format(fatsSum) + "\nfats");
        textViewComposeMealSaturatedFats.setText(format.format(saturatedFatsSum) + "\nsaturated fats");
        textViewComposeMealProtein.setText(format.format(proteinSum) + "\nprotein");
        textViewComposeMealGram.setText(format.format(gramSum) + "\nweight");

        showOrHideNoDataTextView();
    }

    private void buttonRemoveItem(int position) {
        if (position >= cursor.getCount() || position == -1) {
            Toast.makeText(this, R.string.No_product_with_this_index, Toast.LENGTH_SHORT).show();
        } else {
            cursor.moveToPosition(position);
             int productPosition = cursor.getInt(0);

            mDatabase.delete(GroceryContract.GroceryEntry.TABLE_NAME,
                    GroceryContract.GroceryEntry._ID + "=" + productPosition, null);
            mAdapter.swapCursor(getAllItems());

            sumAndViewMacros();
            //Pozostałości kodu, zostawiłem dla notifyRemoved(position) bo nie wiem co to robi.
//            productSaturatedFatsArrayList.remove(position);
//            productProteinArrayList.remove(position);
//            adapter.notifyItemRemoved(position);

        }
        showOrHideNoDataTextView();
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.composingDishesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new GroceryAdapter(this, getAllItems());

        recyclerView.setAdapter(mAdapter);

        showOrHideNoDataTextView();
    }

    private Cursor getAllItems() {
        return mDatabase.query(
                GroceryContract.GroceryEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                //GroceryContract.GroceryEntry.COLUMN_TIMESTAMP + " DESC"
                null
        );
    }

    private void sumAndViewMacros() {

        cursor = getAllItems();

        caloriesSum = 0.0;
        carbohydratesSum = 0.0;
        gramSum = 0.0;
        sugarSum = 0.0;
        fatsSum = 0.0;
        saturatedFatsSum = 0.0;
        proteinSum = 0.0;

        while (cursor.moveToNext()) {
            caloriesSum += cursor.getDouble(2);
            carbohydratesSum += cursor.getDouble(3);
            sugarSum += cursor.getDouble(4);
            fatsSum += cursor.getDouble(5);
            saturatedFatsSum += cursor.getDouble(6);
            proteinSum += cursor.getDouble(7);
            gramSum += cursor.getDouble(8);
        }
        textViewComposeMealKcal.setText(format.format(caloriesSum) + "\nKCAL");
        textViewComposeMealCarbohydrates.setText(format.format(carbohydratesSum) + "\ncarbohydrates");
        textViewComposeMealSugar.setText(format.format(sugarSum) + "\nsugar");
        textViewComposeMealFats.setText(format.format(fatsSum) + "\nfats");
        textViewComposeMealSaturatedFats.setText(format.format(saturatedFatsSum) + "\nsaturated fats");
        textViewComposeMealProtein.setText(format.format(proteinSum) + "\nprotein");
        textViewComposeMealGram.setText(format.format(gramSum) + "\nweight");
    }

    public void button_ClearProductTable(View view) {

        mDatabase.delete(GroceryContract.GroceryEntry.TABLE_NAME,
                GroceryContract.GroceryEntry._ID + ">" + 0, null);
        mAdapter.swapCursor(getAllItems());

        sumAndViewMacros();
        showOrHideNoDataTextView();
    }

    public void button_AddMeal(View view) {
        //W tym miejscu ma być kod, który wyśle do bazy produkty wraz z nazwą posiłku - jednak może na początku inaczej

        ContentValues cv = new ContentValues();
        cv.put(ComposedMealsColumns.ComposedMealsColumnsEntry.COLUMN_MEALNAME, editTextMealName.getText().toString());
        cv.put(ComposedMealsColumns.ComposedMealsColumnsEntry.COLUMN_CALORIES, format.format(caloriesSum));
        cv.put(ComposedMealsColumns.ComposedMealsColumnsEntry.COLUMN_WEIGHT, format.format(gramSum));
        cv.put(ComposedMealsColumns.ComposedMealsColumnsEntry.COLUMN_CARBO, format.format(carbohydratesSum));
        cv.put(ComposedMealsColumns.ComposedMealsColumnsEntry.COLUMN_SUGAR, format.format(sugarSum));
        cv.put(ComposedMealsColumns.ComposedMealsColumnsEntry.COLUMN_PROTEIN, format.format(proteinSum));
        cv.put(ComposedMealsColumns.ComposedMealsColumnsEntry.COLUMN_FATS, format.format(fatsSum));
        cv.put(ComposedMealsColumns.ComposedMealsColumnsEntry.COLUMN_SATURATEDFATS, format.format(saturatedFatsSum));
        mDatabaseComposedMeals.insert(ComposedMealsColumns.ComposedMealsColumnsEntry.TABLE_NAME, null, cv);
    }

    public void button_AddDish(View view) {
        // Tutaj chcę dodać tabelkę do bazy danych grocerylist.db

        //lala2();

    }

//    public void lala2(){
//
//       // mDatabase.update("CREATE TABLE IF NOT EXITS" + )
//
//        final String SQL_CREATE_GROCERYLIST_TABLE2 = "CREATE TABLE " +
//                GroceryContract.GroceryEntry2.TABLE_NAME + " (" +
//                GroceryContract.GroceryEntry2._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                GroceryContract.GroceryEntry2.COLUMN_NAME + " TEXT NOT NULL, " +
//                GroceryContract.GroceryEntry2.COLUMN_AMOUNT + " DOUBLE NOT NULL, " +
//                GroceryContract.GroceryEntry2.COLUMN_CARBO + " DOUBLE NOT NULL, " +
//                GroceryContract.GroceryEntry2.COLUMN_SUGAR + " DOUBLE NOT NULL, " +
//                GroceryContract.GroceryEntry2.COLUMN_FATS + " DOUBLE NOT NULL, " +
//                GroceryContract.GroceryEntry2.COLUMN_SATURATEDFATS + " DOUBLE NOT NULL, " +
//                GroceryContract.GroceryEntry2.COLUMN_PROTEIN + " DOUBLE NOT NULL, " +
//                GroceryContract.GroceryEntry2.COLUMN_WEIGHT + " DOUBLE NOT NULL, " +
//                GroceryContract.GroceryEntry2.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
//                ");";
//        mDatabase.execSQL(SQL_CREATE_GROCERYLIST_TABLE2);
//
//        ContentValues cv = new ContentValues();
//        while (cursor.moveToNext()) {
//            cv.put(GroceryContract.GroceryEntry2.COLUMN_NAME, cursor.getString(1));
//            cv.put(GroceryContract.GroceryEntry2.COLUMN_AMOUNT, cursor.getDouble(2));
//            cv.put(GroceryContract.GroceryEntry2.COLUMN_CARBO, cursor.getDouble(3));
//            cv.put(GroceryContract.GroceryEntry2.COLUMN_SUGAR, cursor.getDouble(4));
//            cv.put(GroceryContract.GroceryEntry2.COLUMN_FATS, cursor.getDouble(5));
//            cv.put(GroceryContract.GroceryEntry2.COLUMN_SATURATEDFATS, cursor.getDouble(6));
//            cv.put(GroceryContract.GroceryEntry2.COLUMN_PROTEIN, cursor.getDouble(7));
//            cv.put(GroceryContract.GroceryEntry2.COLUMN_WEIGHT, cursor.getDouble(8));
//            mDatabase.insert(GroceryContract.GroceryEntry2.TABLE_NAME, null, cv);
//        }
//        lala();
//    }

//    public Cursor lala() {
//        return mDatabase.query(
//                GroceryContract.GroceryEntry2.TABLE_NAME,
//                null,
//                null,
//                null,
//                null,
//                null,
//                GroceryContract.GroceryEntry2.COLUMN_TIMESTAMP + " DESC"
//        );
//    }

    private void initViews() {
        textViewComposeMealKcal = (TextView) findViewById(R.id.textViewComposhingDishesKcal);
        textViewComposeMealCarbohydrates = (TextView) findViewById(R.id.textViewComposhingDishesCarbohydrates);
        textViewComposeMealGram = (TextView) findViewById(R.id.textViewComposhingDishesGram);
        textViewComposeMealSugar = (TextView) findViewById(R.id.textViewComposhingDishesSugar);
        textViewComposeMealFats = (TextView) findViewById(R.id.textViewComposhingDishesFats);
        textViewComposeMealSaturatedFats = (TextView) findViewById(R.id.textViewComposhingDishesSaturatedFats);
        textViewComposeMealProtein = (TextView) findViewById(R.id.textViewComposhingDishesProtein);
        textViewNoData = (TextView) findViewById(R.id.textViewNoData);
        textViewNoData.setVisibility(View.INVISIBLE);
        editText_removeItem = (EditText) findViewById(R.id.editText_removeItem);
        button_removeItem = (Button) findViewById(R.id.button_removeItem);
        button_removeAllItems = (Button) findViewById(R.id.buttonResetIngredients);

        button_removeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = parseInt(editText_removeItem.getText().toString()) - 1;
                buttonRemoveItem(position);
            }
        });
        editTextMealName = (EditText) findViewById(R.id.editTextMealName);
    }

    private void showOrHideNoDataTextView() {
        if (getAllItems().getCount() == 0)
            textViewNoData.setVisibility(View.VISIBLE);
        else
            textViewNoData.setVisibility(View.INVISIBLE);
    }
}