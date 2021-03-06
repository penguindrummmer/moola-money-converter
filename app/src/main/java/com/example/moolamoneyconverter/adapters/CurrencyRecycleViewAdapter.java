package com.example.moolamoneyconverter.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moolamoneyconverter.R;

import java.math.BigDecimal;
import java.util.ArrayList;

public class CurrencyRecycleViewAdapter extends RecyclerView.Adapter {

    private static String TAG = "CurrencyRecycleViewAdapter";

    public interface DipslaySaveButtonListener {
        void displaySaveButton();
        void hideSaveButton();
        void setBaseCurrency(String baseCurrency);
        void setFavoriteCurrencies(ArrayList<String> favoriteCurrencies);
    }

    public interface SaveAmountListener {
        void saveAmount(BigDecimal amount);
    }

    public interface DeleteCurrencyListener {
        void deleteCurrency(String currencyCode);
    }

    private ArrayList<String> currencies = null;
    private String base;
    private ArrayList<String> favorites;
    private int id;
    private Context context;
    private DipslaySaveButtonListener dipslaySaveButtonListener;
    private SaveAmountListener saveAmountListener;
    private DeleteCurrencyListener deleteCurrencyListener;
    private ArrayList<BigDecimal> convertedAmounts = null;


    /*
    Id = recycler adapter accessed from:
    0:  the slider adapter main list
    1:  adapter accessed from
     */


    private int index = -1;
    public CurrencyRecycleViewAdapter(ArrayList<String> currencies, int id, Context context, ArrayList<BigDecimal> convertedAmounts) {
        this.currencies = currencies;
        this.id = id;
        this.context = context;
        if (id == 2) {
            favorites = new ArrayList<>();
        }
        if (context instanceof DipslaySaveButtonListener) {
            dipslaySaveButtonListener = (DipslaySaveButtonListener) context;
        }
        if (context instanceof SaveAmountListener) {
            saveAmountListener = (SaveAmountListener) context;
        }
        if (context instanceof DeleteCurrencyListener) {
            this.deleteCurrencyListener = (DeleteCurrencyListener) context;
        }
        if (convertedAmounts != null) {
            this.convertedAmounts = convertedAmounts;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //determine what list to show
        //show currency list for main activity
        if (id == 0 && currencies != null && !currencies.isEmpty()) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.currency_list_row, parent, false);
            view.setClickable(true);
            return new CurrencyViewHolder(view, context);
        }
        //show selector list when selecting base and favorite currencies
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.selector_list_row, parent, false);
        view.setClickable(true);

        return new CurrencyViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final CurrencyViewHolder currencyHolder = (CurrencyViewHolder) holder;
        BigDecimal amount = null;
        if (convertedAmounts != null && position < convertedAmounts.size()) {
             amount = convertedAmounts.get(position);
        }

        currencyHolder.setViewDetails(currencies.get(position), amount);


        switch (id) {
            //set on click listener for the main activity list
            case 0://prompt user to enter an amount to convert
                currencyHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        index  = position;
                        AlertDialog.Builder alert = new AlertDialog.Builder(context);
                        alert.setTitle("Set Amount");
                        final EditText input = new EditText(context);
                        input.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        alert.setView(input);
                        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String amount = input.getText().toString();
                                if (!amount.isEmpty()) {
                                    Log.d(TAG, "onClick: Save \"" + amount + "\"");
                                    BigDecimal numAmount = new BigDecimal(amount);
                                    saveAmountListener.saveAmount(numAmount);
                                }
                            }
                        });
                        alert.setNegativeButton("Cancel", null);
                        alert.show();
                        notifyDataSetChanged();
                    }
                });//For long press; prompt user to remove a currency
                currencyHolder.itemView.setLongClickable(true);
                currencyHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {AlertDialog.Builder alert = new AlertDialog.Builder(context);
                        alert.setTitle("Remove Currency");
                        final TextView currencyCode = v.findViewById(R.id.textViewCurrencyCode);
                        alert.setMessage(currencyCode.getText().toString());
                        alert.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Log.d(TAG, "onClick: Remove currency" + currencyCode.getText().toString());
                                deleteCurrencyListener.deleteCurrency(currencyCode.getText().toString());
                            }
                        });
                        alert.setNegativeButton("Cancel", null);
                        alert.show();
                        notifyDataSetChanged();
                        return true;
                    }
                });
                break;

                //set on click listen for choosing a base currency
            case 1:
                //display  the save button when a currency is selected //highlight selected currency
                currencyHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String code = ((TextView)v.findViewById(R.id.textViewCurrencyCode)).getText().toString();
                        dipslaySaveButtonListener.setBaseCurrency(code);
                        dipslaySaveButtonListener.displaySaveButton();
                        index = position;
                        notifyDataSetChanged();
                    }
                });

                if (position == currencies.size() -1 ) {
                    holder.itemView.setClickable(false);
                }

                if (index == position) {
                    holder.itemView.setBackgroundColor(Color.BLUE);
                }else {
                    holder.itemView.setBackgroundColor(Color.BLACK);
                }
                break;

                //set on click listener for selecting favorite currencies
            case 2:
                //display or hide save button depending on the number of selected currencies //highlight selected currencies
                currencyHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String code = ((TextView)v.findViewById(R.id.textViewCurrencyCode)).getText().toString();

                        if (!favorites.contains(code)) {
                            Log.d("code", code);
                            favorites.add(code);
                        }else {
                            favorites.remove(code);
                        }
                        notifyDataSetChanged();
                        for (String f: favorites)  {
                            Log.d("fav", f);
                        }

                        if (favorites.size() > 0) {
                            dipslaySaveButtonListener.displaySaveButton();
                            dipslaySaveButtonListener.setFavoriteCurrencies(favorites);
                        }else {
                            dipslaySaveButtonListener.hideSaveButton();
                        }
                    }
                });

                String code = ((TextView)holder.itemView.findViewById(R.id.textViewCurrencyCode)).getText().toString();

                if (position == currencies.size() -1 ) {
                    holder.itemView.setClickable(false);
                }

                if (favorites.contains(code)) {
                    holder.itemView.setBackgroundColor(Color.RED);
                }else{
                    holder.itemView.setBackgroundColor(Color.BLACK);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return currencies.size();
    }
}

class CurrencyViewHolder extends RecyclerView.ViewHolder {

    private ImageView imageViewFlag;
    private TextView textViewCurrencyCode;
    private TextView textViewConversionAmount;
    private Context context;

    CurrencyViewHolder(@NonNull final View itemView, Context context) {
        super(itemView);

        imageViewFlag =  itemView.findViewById(R.id.imageViewFlag);
        textViewCurrencyCode = itemView.findViewById(R.id.textViewCurrencyCode);
        textViewConversionAmount =  itemView.findViewById(R.id.textViewConversionAmount);
        this.context = context;
    }

    //set view details
    void setViewDetails(String currencyCode, BigDecimal amount)  {
        textViewCurrencyCode.setText(currencyCode);
        if (amount != null && textViewConversionAmount != null) {
            textViewConversionAmount.setText(amount.toPlainString());
        }
        if (currencyCode.equals("")) {
            imageViewFlag.setImageDrawable(null);

        }else{
            if (currencyCode.equals("TRY")) {
                currencyCode += "2";
            }
            Log.d("H", "setViewDetails: " + currencyCode);
            imageViewFlag.setImageDrawable((ContextCompat.getDrawable(context, context.getResources().getIdentifier(currencyCode.toLowerCase(), "drawable", context.getPackageName()))));
        }
    }
}
