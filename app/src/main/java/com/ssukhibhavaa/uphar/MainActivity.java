package com.ssukhibhavaa.uphar;

import android.app.*;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {

    LinearLayout menuContainer, categoryContainer;
    EditText search;
    Switch vegOnly;
    TextView cartCount, cartTotal;
    ArrayList<Item> items = new ArrayList<>();
    LinkedHashMap<String,Integer> cart = new LinkedHashMap<>();
    String selectedCategory = "All";

    final int ORANGE = Color.rgb(255,87,34);
    final int DARK = Color.rgb(24,24,24);
    final int MUTED = Color.rgb(105,105,105);
    final int BG = Color.rgb(247,247,247);
    final int GREEN = Color.rgb(20,150,75);
    final int RED = Color.rgb(215,45,45);

    static class Item {
        String category,name,desc,tag;
        int price;
        boolean veg;

        Item(String c,String n,int p,boolean v,String d,String t) {
            category=c;
            name=n;
            price=p;
            veg=v;
            desc=d;
            tag=t;
        }
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        );

        loadData();
        buildUI();
        renderMenu();
    }

    void loadData() {
        try {
            InputStream is = getAssets().open("menu.json");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;

            while ((n=is.read(buf))!=-1) out.write(buf,0,n);

            is.close();

            JSONObject root =
                    new JSONObject(out.toString("UTF-8"));

            JSONArray cats = root.getJSONArray("categories");

            for(int i=0;i<cats.length();i++) {

                JSONObject c = cats.getJSONObject(i);
                String category = c.getString("category");
                JSONArray arr = c.getJSONArray("items");

                for(int j=0;j<arr.length();j++) {

                    JSONObject x = arr.getJSONObject(j);

                    items.add(new Item(
                            category,
                            x.getString("name"),
                            x.getInt("price"),
                            x.getBoolean("is_veg"),
                            x.optString("description",""),
                            x.optString("tag","")
                    ));
                }
            }

        } catch(Exception e) {
            Toast.makeText(
                    this,
                    "Menu loading error",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    void buildUI() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        root.addView(
                header(),
                new LinearLayout.LayoutParams(
                        -1, dp(92)
                )
        );

        LinearLayout content =
                new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        content.setPadding(
                dp(16),
                dp(8),
                dp(16),
                0
        );

        content.addView(
                hero(),
                new LinearLayout.LayoutParams(
                        -1, dp(112)
                )
        );

        content.addView(
                searchBox(),
                new LinearLayout.LayoutParams(
                        -1, dp(58)
                )
        );

        LinearLayout filterRow =
                new LinearLayout(this);

        filterRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView recommended =
                txt(
                        "Recommended",
                        18,
                        DARK,
                        true
                );

        filterRow.addView(
                recommended,
                new LinearLayout.LayoutParams(
                        0, dp(48), 1
                )
        );

        TextView vegText =
                txt(
                        "VEG ONLY",
                        11,
                        MUTED,
                        true
                );

        filterRow.addView(
                vegText,
                new LinearLayout.LayoutParams(
                        -2, dp(40)
                )
        );

        vegOnly = new Switch(this);
        vegOnly.setChecked(false);

        filterRow.addView(
                vegOnly,
                new LinearLayout.LayoutParams(
                        dp(55), dp(45)
                )
        );

        content.addView(
                filterRow,
                new LinearLayout.LayoutParams(
                        -1, dp(48)
                )
        );

        categoryContainer =
                new LinearLayout(this);

        categoryContainer.setOrientation(
                LinearLayout.HORIZONTAL
        );

        HorizontalScrollView categories =
                new HorizontalScrollView(this);

        categories.setHorizontalScrollBarEnabled(false);

        categories.addView(
                categoryContainer
        );

        content.addView(
                categories,
                new LinearLayout.LayoutParams(
                        -1, dp(56)
                )
        );

        root.addView(
                content,
                new LinearLayout.LayoutParams(
                        -1, dp(274)
                )
        );

        ScrollView scroll =
                new ScrollView(this);

        scroll.setFillViewport(true);

        menuContainer =
                new LinearLayout(this);

        menuContainer.setOrientation(
                LinearLayout.VERTICAL
        );

        menuContainer.setPadding(
                dp(16),
                0,
                dp(16),
                dp(18)
        );

        scroll.addView(menuContainer);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1, 0, 1
                )
        );

        root.addView(
                cartBar(),
                new LinearLayout.LayoutParams(
                        -1, dp(78)
                )
        );

        setContentView(root);

        search.addTextChangedListener(
                new TextWatcher() {

                    public void beforeTextChanged(
                            CharSequence s,
                            int st,
                            int c,
                            int a) {}

                    public void onTextChanged(
                            CharSequence s,
                            int st,
                            int b,
                            int c) {
                        renderMenu();
                    }

                    public void afterTextChanged(
                            Editable e) {}
                }
        );

        vegOnly.setOnCheckedChangeListener(
                (v,c) -> renderMenu()
        );

        buildCategories();
        updateCartBar();
    }

    View header() {

        LinearLayout h =
                new LinearLayout(this);

        h.setGravity(
                Gravity.CENTER_VERTICAL
        );

        h.setPadding(
                dp(16),
                dp(8),
                dp(16),
                dp(8)
        );

        h.setBackgroundColor(
                Color.WHITE
        );

        TextView logo =
                txt("SU",18,Color.WHITE,true);

        logo.setGravity(Gravity.CENTER);

        logo.setBackground(
                round(ORANGE,18,ORANGE)
        );

        h.addView(
                logo,
                new LinearLayout.LayoutParams(
                        dp(54),dp(54)
                )
        );

        LinearLayout names =
                new LinearLayout(this);

        names.setOrientation(
                LinearLayout.VERTICAL
        );

        names.setPadding(
                dp(12),0,0,0
        );

        TextView title =
                txt(
                        "Ssukhibhavaa Uphar",
                        19,
                        DARK,
                        true
                );

        TextView subtitle =
                txt(
                        "Fresh • Fast • Delicious",
                        11,
                        MUTED,
                        false
                );

        names.add
cd ~/storage/downloads/SsukhibhavaaUphar && cat > app/src/main/java/com/ssukhibhavaa/uphar/MainActivity.java <<'EOF'
package com.ssukhibhavaa.uphar;

import android.app.*;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {

    LinearLayout menuContainer, categoryContainer;
    EditText search;
    Switch vegOnly;
    TextView cartCount, cartTotal;
    ArrayList<Item> items = new ArrayList<>();
    LinkedHashMap<String,Integer> cart = new LinkedHashMap<>();
    String selectedCategory = "All";

    final int ORANGE = Color.rgb(255,87,34);
    final int DARK = Color.rgb(24,24,24);
    final int MUTED = Color.rgb(105,105,105);
    final int BG = Color.rgb(247,247,247);
    final int GREEN = Color.rgb(20,150,75);
    final int RED = Color.rgb(215,45,45);

    static class Item {
        String category,name,desc,tag;
        int price;
        boolean veg;

        Item(String c,String n,int p,boolean v,String d,String t) {
            category=c;
            name=n;
            price=p;
            veg=v;
            desc=d;
            tag=t;
        }
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        );

        loadData();
        buildUI();
        renderMenu();
    }

    void loadData() {
        try {
            InputStream is = getAssets().open("menu.json");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;

            while ((n=is.read(buf))!=-1) out.write(buf,0,n);

            is.close();

            JSONObject root =
                    new JSONObject(out.toString("UTF-8"));

            JSONArray cats = root.getJSONArray("categories");

            for(int i=0;i<cats.length();i++) {

                JSONObject c = cats.getJSONObject(i);
                String category = c.getString("category");
                JSONArray arr = c.getJSONArray("items");

                for(int j=0;j<arr.length();j++) {

                    JSONObject x = arr.getJSONObject(j);

                    items.add(new Item(
                            category,
                            x.getString("name"),
                            x.getInt("price"),
                            x.getBoolean("is_veg"),
                            x.optString("description",""),
                            x.optString("tag","")
                    ));
                }
            }

        } catch(Exception e) {
            Toast.makeText(
                    this,
                    "Menu loading error",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    void buildUI() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        root.addView(
                header(),
                new LinearLayout.LayoutParams(
                        -1, dp(92)
                )
        );

        LinearLayout content =
                new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        content.setPadding(
                dp(16),
                dp(8),
                dp(16),
                0
        );

        content.addView(
                hero(),
                new LinearLayout.LayoutParams(
                        -1, dp(112)
                )
        );

        content.addView(
                searchBox(),
                new LinearLayout.LayoutParams(
                        -1, dp(58)
                )
        );

        LinearLayout filterRow =
                new LinearLayout(this);

        filterRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView recommended =
                txt(
                        "Recommended",
                        18,
                        DARK,
                        true
                );

        filterRow.addView(
                recommended,
                new LinearLayout.LayoutParams(
                        0, dp(48), 1
                )
        );

        TextView vegText =
                txt(
                        "VEG ONLY",
                        11,
                        MUTED,
                        true
                );

        filterRow.addView(
                vegText,
                new LinearLayout.LayoutParams(
                        -2, dp(40)
                )
        );

        vegOnly = new Switch(this);
        vegOnly.setChecked(false);

        filterRow.addView(
                vegOnly,
                new LinearLayout.LayoutParams(
                        dp(55), dp(45)
                )
        );

        content.addView(
                filterRow,
                new LinearLayout.LayoutParams(
                        -1, dp(48)
                )
        );

        categoryContainer =
                new LinearLayout(this);

        categoryContainer.setOrientation(
                LinearLayout.HORIZONTAL
        );

        HorizontalScrollView categories =
                new HorizontalScrollView(this);

        categories.setHorizontalScrollBarEnabled(false);

        categories.addView(
                categoryContainer
        );

        content.addView(
                categories,
                new LinearLayout.LayoutParams(
                        -1, dp(56)
                )
        );

        root.addView(
                content,
                new LinearLayout.LayoutParams(
                        -1, dp(274)
                )
        );

        ScrollView scroll =
                new ScrollView(this);

        scroll.setFillViewport(true);

        menuContainer =
                new LinearLayout(this);

        menuContainer.setOrientation(
                LinearLayout.VERTICAL
        );

        menuContainer.setPadding(
                dp(16),
                0,
                dp(16),
                dp(18)
        );

        scroll.addView(menuContainer);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1, 0, 1
                )
        );

        root.addView(
                cartBar(),
                new LinearLayout.LayoutParams(
                        -1, dp(78)
                )
        );

        setContentView(root);

        search.addTextChangedListener(
                new TextWatcher() {

                    public void beforeTextChanged(
                            CharSequence s,
                            int st,
                            int c,
                            int a) {}

                    public void onTextChanged(
                            CharSequence s,
                            int st,
                            int b,
                            int c) {
                        renderMenu();
                    }

                    public void afterTextChanged(
                            Editable e) {}
                }
        );

        vegOnly.setOnCheckedChangeListener(
                (v,c) -> renderMenu()
        );

        buildCategories();
        updateCartBar();
    }

    View header() {

        LinearLayout h =
                new LinearLayout(this);

        h.setGravity(
                Gravity.CENTER_VERTICAL
        );

        h.setPadding(
                dp(16),
                dp(8),
                dp(16),
                dp(8)
        );

        h.setBackgroundColor(
                Color.WHITE
        );

        TextView logo =
                txt("SU",18,Color.WHITE,true);

        logo.setGravity(Gravity.CENTER);

        logo.setBackground(
                round(ORANGE,18,ORANGE)
        );

        h.addView(
                logo,
                new LinearLayout.LayoutParams(
                        dp(54),dp(54)
                )
        );

        LinearLayout names =
                new LinearLayout(this);

        names.setOrientation(
                LinearLayout.VERTICAL
        );

        names.setPadding(
                dp(12),0,0,0
        );

        TextView title =
                txt(
                        "Ssukhibhavaa Uphar",
                        19,
                        DARK,
                        true
                );

        TextView subtitle =
                txt(
                        "Fresh • Fast • Delicious",
                        11,
                        MUTED,
                        false
                );

        names.addView(title);
        names.addView(subtitle);

        h.addView(
                names,
                new LinearLayout.LayoutParams(
                        0,-2,1
                )
        );

        TextView open =
                txt(
                        "OPEN",
                        10,
                        GREEN,
                        true
                );

        open.setGravity(Gravity.CENTER);

        open.setBackground(
                round(
                        Color.rgb(235,250,240),
                        30,
                        Color.TRANSPARENT
                )
        );

        h.addView(
                open,
                new LinearLayout.LayoutParams(
                        dp(58),dp(32)
                )
        );

        return h;
    }

    View hero() {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                dp(18),
                dp(12),
                dp(18),
                dp(10)
        );

        GradientDrawable bg =
                new GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        new int[]{
                                Color.rgb(255,98,45),
                                Color.rgb(240,55,20)
                        }
                );

        bg.setCornerRadius(dp(20));

        box.setBackground(bg);

        TextView small =
                txt(
                        "🍕  FOOD THAT MAKES YOU HAPPY",
                        10,
                        Color.WHITE,
                        true
                );

        TextView big =
                txt(
                        "Hungry? We've got you covered.",
                        19,
                        Color.WHITE,
                        true
                );

        TextView sub =
                txt(
                        "Pizza • Burgers • Momos • Fried Chicken",
                        11,
                        Color.WHITE,
                        false
                );

        box.addView(small);
        box.addView(big);
        box.addView(sub);

        return box;
    }

    View searchBox() {

        LinearLayout box =
                new LinearLayout(this);

        box.setGravity(
                Gravity.CENTER_VERTICAL
        );

        box.setPadding(
                dp(14),0,dp(14),0
        );

        box.setBackground(
                round(
                        Color.WHITE,
                        18,
                        Color.TRANSPARENT
                )
        );

        TextView icon =
                txt(
                        "⌕",
                        25,
                        MUTED,
                        false
                );

        box.addView(
                icon,
                new LinearLayout.LayoutParams(
                        dp(32),-1
                )
        );

        search =
                new EditText(this);

        search.setSingleLine(true);
        search.setTextSize(14);
        search.setHint(
                "Search for dishes..."
        );
        search.setTextColor(DARK);
        search.setHintTextColor(MUTED);
        search.setBackgroundColor(
                Color.TRANSPARENT
        );

        box.addView(
                search,
                new LinearLayout.LayoutParams(
                        0,-1,1
                )
        );

        return box;
    }

    void buildCategories() {

        categoryContainer.removeAllViews();

        addCat("All");

        LinkedHashSet<String> seen =
                new LinkedHashSet<>();

        for(Item x:items)
            seen.add(x.category);

        for(String c:seen)
            addCat(c);
    }

    void addCat(String c) {

        boolean selected =
                c.equals(selectedCategory);

        TextView v =
                txt(
                        c,
                        12,
                        selected ? Color.WHITE : DARK,
                        true
                );

        v.setGravity(
                Gravity.CENTER
        );

        v.setPadding(
                dp(15),0,dp(15),0
        );

        v.setBackground(
                round(
                        selected ? ORANGE : Color.WHITE,
                        22,
                        selected ? ORANGE : Color.LTGRAY
                )
        );

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        -2,dp(40)
                );

        lp.setMargins(
                dp(3),0,dp(3),dp(4)
        );

        categoryContainer.addView(v,lp);

        v.setOnClickListener(
                z -> {
                    selectedCategory=c;
                    buildCategories();
                    renderMenu();
                }
        );
    }

    void renderMenu() {

        if(menuContainer==null)
            return;

        menuContainer.removeAllViews();

        String q =
                search==null
                        ? ""
                        : search.getText()
                            .toString()
                            .trim()
                            .toLowerCase(Locale.ROOT);

        int shown=0;
        String last="";

        for(Item x:items) {

            if(
                    !selectedCategory.equals("All")
                    &&
                    !x.category.equals(
                            selectedCategory
                    )
            ) continue;

            if(
                    vegOnly!=null
                    &&
                    vegOnly.isChecked()
                    &&
                    !x.veg
            ) continue;

            if(
                    !q.isEmpty()
                    &&
                    !x.name
                            .toLowerCase(Locale.ROOT)
                            .contains(q)
                    &&
                    !x.category
                            .toLowerCase(Locale.ROOT)
                            .contains(q)
            ) continue;

            if(
                    !x.category.equals(last)
                    &&
                    selectedCategory.equals("All")
            ) {

                TextView head =
                        txt(
                                x.category,
                                17,
                                DARK,
                                true
                        );

                head.setPadding(
                        dp(2),
                        dp(12),
                        0,
                        dp(8)
                );

                menuContainer.addView(head);

                last=x.category;
            }

            menuContainer.addView(
                    card(x)
            );

            shown++;
        }

        if(shown==0) {

            TextView empty =
                    txt(
                            "No dishes found 😕",
                            16,
                            MUTED,
                            true
                    );

            empty.setGravity(
                    Gravity.CENTER
            );

            empty.setPadding(
                    0,dp(55),0,dp(55)
            );

            menuContainer.addView(empty);
        }
    }

    View card(Item x) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                dp(14),
                dp(13),
                dp(14),
                dp(13)
        );

        card.setBackground(
                round(
                        Color.WHITE,
                        18,
                        Color.TRANSPARENT
                )
        );

        LinearLayout top =
                new LinearLayout(this);

        top.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView indicator =
                txt(
                        "●",
                        12,
                        x.veg ? GREEN : RED,
                        true
                );

        top.addView(
                indicator,
                new LinearLayout.LayoutParams(
                        dp(18),dp(25)
                )
        );

        TextView name =
                txt(
                        x.name,
                        15,
                        DARK,
                        true
                );

        name.setMaxLines(2);

        top.addView(
                name,
                new LinearLayout.LayoutParams(
                        0,-2,1
                )
        );

        if(!x.tag.isEmpty()) {

            TextView tag =
                    txt(
                            x.tag.toUpperCase(),
                            9,
                            ORANGE,
                            true
                    );

            tag.setGravity(
                    Gravity.CENTER
            );

            tag.setPadding(
                    dp(7),0,dp(7),0
            );

            tag.setBackground(
                    round(
                            Color.rgb(255,242,237),
                            20,
                            Color.TRANSPARENT
                    )
            );

            top.addView(
                    tag,
                    new LinearLayout.LayoutParams(
                            -2,dp(27)
                    )
            );
        }

        card.addView(top);

        LinearLayout bottom =
                new LinearLayout(this);

        bottom.setGravity(
                Gravity.CENTER_VERTICAL
        );

        LinearLayout details =
                new LinearLayout(this);

        details.setOrientation(
                LinearLayout.VERTICAL
        );

        TextView price =
                txt(
                        "₹"+x.price,
                        16,
                        DARK,
                        true
                );

        details.addView(price);

        if(!x.desc.isEmpty()) {

            TextView desc =
                    txt(
                            x.desc,
                            11,
                            MUTED,
                            false
                    );

            desc.setMaxLines(2);

            details.addView(desc);
        }

        bottom.addView(
                details,
                new LinearLayout.LayoutParams(
                        0,-2,1
                )
        );

        int qty =
                cart.containsKey(x.name)
                        ? cart.get(x.name)
                        : 0;

        if(qty==0) {

            TextView add =
                    txt(
                            "ADD",
                            12,
                            ORANGE,
                            true
                    );

            add.setGravity(
                    Gravity.CENTER
            );

            add.setBackground(
                    round(
                            Color.WHITE,
                            12,
                            ORANGE
                    )
            );

            add.setOnClickListener(
                    v -> {
                        cart.put(x.name,1);
                        updateCartBar();
                        renderMenu();
                    }
            );

            bottom.addView(
                    add,
                    new LinearLayout.LayoutParams(
                            dp(76),dp(38)
                    )
            );

        } else {

            LinearLayout controls =
                    new LinearLayout(this);

            controls.setGravity(
                    Gravity.CENTER
            );

            TextView minus =
                    txt("−",20,DARK,true);

            minus.setGravity(
                    Gravity.CENTER
            );

            minus.setBackground(
                    round(
                            Color.rgb(242,242,242),
                            10,
                            Color.LTGRAY
                    )
            );

            TextView number =
                    txt(
                            String.valueOf(qty),
                            14,
                            DARK,
                            true
                    );

            number.setGravity(
                    Gravity.CENTER
            );

            TextView plus =
                    txt("+",20,Color.WHITE,true);

            plus.setGravity(
                    Gravity.CENTER
            );

            plus.setBackground(
                    round(
                            ORANGE,
                            10,
                            ORANGE
                    )
            );

            controls.addView(
                    minus,
                    new LinearLayout.LayoutParams(
                            dp(34),dp(34)
                    )
            );

            controls.addView(
                    number,
                    new LinearLayout.LayoutParams(
                            dp(34),dp(34)
                    )
            );

            controls.addView(
                    plus,
                    new LinearLayout.LayoutParams(
                            dp(34),dp(34)
                    )
            );

            minus.setOnClickListener(
                    v -> {
                        int n=cart.get(x.name)-1;

                        if(n<=0)
                            cart.remove(x.name);
                        else
                            cart.put(x.name,n);

                        updateCartBar();
                        renderMenu();
                    }
            );

            plus.setOnClickListener(
                    v -> {
                        cart.put(
                                x.name,
                                cart.get(x.name)+1
                        );

                        updateCartBar();
                        renderMenu();
                    }
            );

            bottom.addView(
                    controls,
                    new LinearLayout.LayoutParams(
                            dp(102),dp(38)
                    )
            );
        }

        card.addView(
                bottom
        );

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        lp.setMargins(
                0,
                0,
                0,
                dp(10)
        );

        card.setLayoutParams(lp);

        return card;
    }

    View cartBar() {

        LinearLayout bar =
                new LinearLayout(this);

        bar.setGravity(
                Gravity.CENTER_VERTICAL
        );

        bar.setPadding(
                dp(14),
                dp(8),
                dp(14),
                dp(8)
        );

        bar.setBackgroundColor(
                Color.WHITE
        );

        LinearLayout info =
                new LinearLayout(this);

        info.setOrientation(
                LinearLayout.VERTICAL
        );

        cartCount =
                txt(
                        "0 items",
                        12,
                        MUTED,
                        true
                );

        cartTotal =
                txt(
                        "₹0",
                        18,
                        DARK,
                        true
                );

        info.addView(cartCount);
        info.addView(cartTotal);

        bar.addView(
                info,
                new LinearLayout.LayoutParams(
                        0,-2,1
                )
        );

        TextView checkout =
                txt(
                        "VIEW CART  ›",
                        13,
                        Color.WHITE,
                        true
                );

        checkout.setGravity(
                Gravity.CENTER
        );

        checkout.setBackground(
                round(
                        ORANGE,
                        15,
                        ORANGE
                )
        );

        checkout.setOnClickListener(
                v -> showCheckout()
        );

        bar.addView(
                checkout,
                new LinearLayout.LayoutParams(
                        dp(145),dp(52)
                )
        );

        return bar;
    }

    void updateCartBar() {

        int count=0;
        int total=0;

        for(Item x:items) {

            int q =
                    cart.containsKey(x.name)
                            ? cart.get(x.name)
                            : 0;

            count+=q;
            total+=q*x.price;
        }

        if(cartCount!=null)
            cartCount.setText(
                    count+" item"+
                    (count==1 ? "" : "s")
            );

        if(cartTotal!=null)
            cartTotal.setText(
                    "₹"+total
            );
    }

    void showCheckout() {

        if(cart.isEmpty()) {

            Toast.makeText(
                    this,
                    "Your cart is empty",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        LinearLayout body =
                new LinearLayout(this);

        body.setOrientation(
                LinearLayout.VERTICAL
        );

        body.setPadding(
                dp(20),
                dp(8),
                dp(20),
                0
        );

        ScrollView scroll =
                new ScrollView(this);

        LinearLayout list =
                new LinearLayout(this);

        list.setOrientation(
                LinearLayout.VERTICAL
        );

        int total=0;

        for(Item x:items) {

            if(!cart.containsKey(x.name))
                continue;

            int q=cart.get(x.name);

            total+=q*x.price;

            TextView row =
                    txt(
                            q+" × "+x.name+
                            "     ₹"+(q*x.price),
                            14,
                            DARK,
                            false
                    );

            row.setPadding(
                    0,dp(9),0,dp(9)
            );

            list.addView(row);
        }

        scroll.addView(list);

        body.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,dp(300)
                )
        );

        TextView totalText =
                txt(
                        "TOTAL  ₹"+total,
                        19,
                        DARK,
                        true
                );

        totalText.setGravity(
                Gravity.RIGHT
        );

        totalText.setPadding(
                0,dp(12),0,dp(12)
        );

        body.addView(totalText);

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Your Order 🛒")
                        .setView(body)
                        .setNegativeButton(
                                "CLOSE",
                                null
                        )
                        .setPositiveButton(
                                "PLACE ORDER",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                x -> {

                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    ).setTextColor(ORANGE);

                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    ).setOnClickListener(
                            v -> {

                                dialog.dismiss();

                                cart.clear();

                                updateCartBar();
                                renderMenu();

                                Toast.makeText(
                                        this,
                                        "Order placed successfully! 🎉",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                    );
                }
        );

        dialog.show();
    }

    TextView txt(
            String s,
            float size,
            int color,
            boolean bold
    ) {

        TextView t =
                new TextView(this);

        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);

        t.setTypeface(
                Typeface.DEFAULT,
                bold
                        ? Typeface.BOLD
                        : Typeface.NORMAL
        );

        return t;
    }

    GradientDrawable round(
            int color,
            float radius,
            int stroke
    ) {

        GradientDrawable g =
                new GradientDrawable();

        g.setColor(color);
        g.setCornerRadius(
                dp(radius)
        );

        if(stroke!=Color.TRANSPARENT)
            g.setStroke(
                    dp(1),
                    stroke
            );

        return g;
    }

    int dp(float x) {

        return (int)(
                x *
                getResources()
                    .getDisplayMetrics()
                    .density
                + 0.5f
        );
    }
}
