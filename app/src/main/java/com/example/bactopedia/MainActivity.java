package com.example.bactopedia;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    class Item {
        String name;
        String desc;
        String info;
        int miniImage;
        int showImage;
        Item(String name, String desc, String info, int miniImage, int showImage) {
            this.name = name;
            this.desc = desc;
            this.info = info;
            this.miniImage = miniImage;
            this.showImage = showImage;
        }
    }

    private String advancedFilter = ""; // фильтр по форме, способу дыхания и т.д.
    private ArrayList<Item> items = new ArrayList<>();
    SearchView searchView;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.bg)); // цвет строки состояния
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.bg)); // цвет панели навигации
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // цвет значков строки состояния
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // цвет значков панели навигации
                int flags = window.getDecorView().getSystemUiVisibility();
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                window.getDecorView().setSystemUiVisibility(flags);
            }
        }

        try {
            InputStream is = getAssets().open("bacteria.csv");
            InputStreamReader isr = new InputStreamReader(is);
            CSVReader reader = new CSVReader(isr);
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                String name = nextLine[0];
                String desc = nextLine[1];
                String info = nextLine[2];
                info = Html.fromHtml(info, Html.FROM_HTML_MODE_LEGACY).toString();
                int miniImage = getResources().getIdentifier(nextLine[3], "drawable", getPackageName());
                int showImage = getResources().getIdentifier(nextLine[4], "drawable", getPackageName());
                items.add(new Item(name, desc, info, miniImage, showImage));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }

        searchView = findViewById(R.id.searchView);
        listView = findViewById(R.id.listView);

        final ArrayList<Item> originalItems = new ArrayList<>(items);

        ArrayAdapter<Item> adapter = new ArrayAdapter<Item>(this, R.layout.name_item, items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.name_item, parent, false);
                }
                Item item = getItem(position);

                TextView textView = convertView.findViewById(R.id.bacteria_name);
                textView.setText(item.name);

                TextView textView2 = convertView.findViewById(R.id.bacteria_desc);
                textView2.setText(item.desc);

                ImageView imageView = convertView.findViewById(R.id.imageView3);
                imageView.setImageResource(item.miniImage);
                return convertView;
            }

            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        if (constraint == null || constraint.length() == 0) {
                            results.values = originalItems;
                            results.count = originalItems.size();
                        } else {
                            List<Item> filteredItems = new ArrayList<>();
                            String[] words = constraint.toString().toLowerCase().split("\\s+");
                            Pattern pattern;
                            Matcher matcher;
                            for (Item item : originalItems) {
                                String itemData = (item.name + " " + item.desc + " " + item.info).toLowerCase();
                                boolean isMatch = true;
                                for (String word : words) {
                                    if (word.startsWith("|") && word.contains("---")) {
                                        String[] parts = word.split("---");
                                        String searchTerm = parts[0].substring(1);
                                        String negativeTerm = parts.length > 1 ? parts[1].substring(0, parts[1].length() - 1) : "";
                                        pattern = Pattern.compile(searchTerm + "(?!.*" + negativeTerm + ")");
                                        matcher = pattern.matcher(itemData);
                                        if (!matcher.find()) {
                                            isMatch = false;
                                            break;
                                        }
                                    } else if (!word.startsWith("|") || !word.endsWith("|")) {
                                        if (!itemData.contains(word)) {
                                            isMatch = false;
                                            break;
                                        }
                                    }
                                }
                                if (isMatch) {
                                    filteredItems.add(item);
                                }
                            }
                            results.values = filteredItems;
                            results.count = filteredItems.size();
                        }
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        clear();
                        addAll((List<Item>) results.values);
                        notifyDataSetChanged();
                    }
                };
            }




        };

        listView.setAdapter(adapter);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(advancedFilter + " " + query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(advancedFilter + " " + newText);
                return false;
            }
        });

        ImageButton filterButton = findViewById(R.id.imageButton);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.filter_dialog, null);
                builder.setView(dialogView);

                // Тут можно настроить диалог, например, добавить кнопки и обработчики нажатий

                Button applyButton = dialogView.findViewById(R.id.apply);
                final AlertDialog dialog = builder.create();
                applyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        advancedFilter = "";

                        RadioGroup breathingMethodGroup = dialogView.findViewById(R.id.breathing_method);
                        int selectedBreathingMethodId = breathingMethodGroup.getCheckedRadioButtonId();
                        if (selectedBreathingMethodId == R.id.aerob) {
                            advancedFilter += "|аэроб---на|" + " ";
                        }
                        else if (selectedBreathingMethodId == R.id.anaerob) {
                            advancedFilter += "анаэроб" + " ";
                        }


                        RadioGroup gramStainGroup = dialogView.findViewById(R.id.gram_stain);
                        int selectedGramStainId = gramStainGroup.getCheckedRadioButtonId();

                        if (selectedGramStainId == R.id.grampol) {
                            advancedFilter += "положит" + " ";
                        }
                        else if (selectedGramStainId == R.id.gramotr) {
                            advancedFilter += "отрицат" + " ";
                        }


                        CheckBox cocciCheckBox = dialogView.findViewById(R.id.cocci);
                        if (cocciCheckBox.isChecked()) {
                            advancedFilter += "кокк" + " ";
                        }

                        CheckBox rodsCheckBox = dialogView.findViewById(R.id.rods);
                        if (rodsCheckBox.isChecked()) {
                            advancedFilter += "палочк" + " ";
                        }

                        CheckBox spirillaCheckBox = dialogView.findViewById(R.id.spirilla);
                        if (spirillaCheckBox.isChecked()) {
                            advancedFilter += "спир" + " ";
                        }

                        advancedFilter = advancedFilter.trim(); // удалить лишний пробел в конце строки
                        Log.d("MainActivity", "advancedFilter: " + advancedFilter);

                        // Применить фильтр
                        String query = searchView.getQuery().toString();
                        adapter.getFilter().filter(advancedFilter + " " + query);

                        // Закрыть диалоговое окно
                        dialog.dismiss();
                    }
                });


                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();


            }
        });



        LayoutAnimationController controller = AnimationUtils
                .loadLayoutAnimation(this, R.anim.layout_bottom_to_top_slide);
        listView.setLayoutAnimation(controller);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Item item = items.get(position);
                Intent intent = new Intent(getApplicationContext(), BacteriaInfoActivity.class);
                intent.putExtra("selectedBacteria", item.name);
                intent.putExtra("bacteriaInfo", item.info);
                intent.putExtra("bacteriaShowImage", item.showImage);
                startActivity(intent);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        ListView listView = findViewById(R.id.listView);

        LayoutAnimationController controller = AnimationUtils
                .loadLayoutAnimation(this, R.anim.layout_bottom_to_top_slide);
        listView.setLayoutAnimation(controller);
        listView.scheduleLayoutAnimation();
    }
}