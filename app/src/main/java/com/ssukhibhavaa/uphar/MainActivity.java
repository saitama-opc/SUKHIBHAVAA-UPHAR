package com.ssukhibhavaa.uphar;

import android.app.*;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
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
    int orange = Color.rgb(255,90,31), dark = Color.rgb(25,25,25), muted = Color.rgb(115,115,115);

    static class Item { String category,name,desc,tag; int price; boolean veg; Item(String c,String n,int p,boolean v,String d,String t){category=c;name=n;price=p;veg=v;desc=d;tag=t;} }

    @Override public void onCreate(Bundle b){ super.onCreate(b); getWindow().setStatusBarColor(Color.WHITE); getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); loadData(); buildUI(); renderMenu(); }

    void loadData(){
        try {
            InputStream is=getAssets().open("menu.json"); ByteArrayOutputStream out=new ByteArrayOutputStream(); byte[] buf=new byte[4096]; int n;
            while((n=is.read(buf))!=-1) out.write(buf,0,n); is.close();
            JSONObject root=new JSONObject(out.toString("UTF-8")); JSONArray cats=root.getJSONArray("categories");
            for(int i=0;i<cats.length();i++){ JSONObject c=cats.getJSONObject(i); String cn=c.getString("category"); JSONArray a=c.getJSONArray("items");
                for(int j=0;j<a.length();j++){ JSONObject x=a.getJSONObject(j); items.add(new Item(cn,x.getString("name"),x.getInt("price"),x.getBoolean("is_veg"),x.optString("description",""),x.optString("tag",""))); }
            }
        } catch(Exception e){ Toast.makeText(this,"Menu loading error",Toast.LENGTH_LONG).show(); }
    }

    void buildUI(){
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.rgb(247,247,247));
        root.addView(header(), new LinearLayout.LayoutParams(-1,wrap(76)));
        LinearLayout controls=new LinearLayout(this); controls.setOrientation(LinearLayout.VERTICAL); controls.setPadding(dp(16),dp(6),dp(16),0);
        search=new EditText(this); search.setSingleLine(true); search.setHint("Search pizzas, burgers, momos..."); search.setTextSize(14); search.setPadding(dp(16),0,dp(16),0); search.setBackground(round(Color.WHITE,16,Color.TRANSPARENT));
        controls.addView(search,new LinearLayout.LayoutParams(-1,dp(48)));
        LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(0,dp(6),0,dp(4));
        TextView filter=new TextView(this); filter.setText("VEG ONLY"); filter.setTextSize(12); filter.setTypeface(Typeface.DEFAULT,Typeface.BOLD); filter.setTextColor(muted); row.addView(filter,new LinearLayout.LayoutParams(0,dp(40),1));
        vegOnly=new Switch(this); vegOnly.setChecked(false); row.addView(vegOnly,new LinearLayout.LayoutParams(wrap(58),wrap(40)));
        controls.addView(row);
        categoryContainer=new LinearLayout(this); categoryContainer.setOrientation(LinearLayout.HORIZONTAL); categoryContainer.setPadding(0,dp(4),0,dp(8));
        HorizontalScrollView hsv=new HorizontalScrollView(this); hsv.setHorizontalScrollBarEnabled(false); hsv.addView(categoryContainer); controls.addView(hsv,new LinearLayout.LayoutParams(-1,dp(52)));
        root.addView(controls,new LinearLayout.LayoutParams(-1,wrap(180)));
        ScrollView scroll=new ScrollView(this); menuContainer=new LinearLayout(this); menuContainer.setOrientation(LinearLayout.VERTICAL); menuContainer.setPadding(dp(16),0,dp(16),dp(14)); scroll.addView(menuContainer); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        root.addView(cartBar(),new LinearLayout.LayoutParams(-1,dp(72)));
        setContentView(root);
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){} public void onTextChanged(CharSequence s,int st,int b,int c){renderMenu();} public void afterTextChanged(Editable e){}});
        vegOnly.setOnCheckedChangeListener((v,c)->renderMenu());
        buildCategories(); updateCartBar();
    }

    View header(){
        LinearLayout h=new LinearLayout(this); h.setOrientation(LinearLayout.HORIZONTAL); h.setGravity(Gravity.CENTER_VERTICAL); h.setPadding(dp(16),dp(8),dp(16),dp(4)); h.setBackgroundColor(Color.WHITE);
        LinearLayout t=new LinearLayout(this); t.setOrientation(LinearLayout.VERTICAL); TextView name=txt("Ssukhibhavaa Uphar",20,dark,true); TextView sub=txt("Fresh • Fast • Delicious",12,muted,false); t.addView(name); t.addView(sub); h.addView(t,new LinearLayout.LayoutParams(0,-2,1));
        TextView badge=txt("₹  MENU",11,Color.WHITE,true); badge.setGravity(Gravity.CENTER); badge.setPadding(dp(13),0,dp(13),0); badge.setBackground(round(orange,30,orange)); h.addView(badge,new LinearLayout.LayoutParams(wrap(80),dp(36))); return h;
    }

    void buildCategories(){
        categoryContainer.removeAllViews(); addCat("All"); LinkedHashSet<String> seen=new LinkedHashSet<>(); for(Item x:items) seen.add(x.category); for(String c:seen) addCat(c);
    }
    void addCat(String c){ TextView v=txt(c,13,c.equals(selectedCategory)?Color.WHITE:dark,true); v.setGravity(Gravity.CENTER); v.setPadding(dp(16),0,dp(16),0); v.setBackground(round(c.equals(selectedCategory)?orange:Color.WHITE,24,c.equals(selectedCategory)?orange:Color.LTGRAY)); v.setOnClickListener(z->{selectedCategory=c;buildCategories();renderMenu();}); categoryContainer.addView(v,new LinearLayout.LayoutParams(wrap(90),dp(40))); ((LinearLayout.LayoutParams)v.getLayoutParams()).setMargins(dp(3),0,dp(3),0); }

    void renderMenu(){
        if(menuContainer==null)return; menuContainer.removeAllViews(); String q=search==null?"":search.getText().toString().trim().toLowerCase(Locale.ROOT); int shown=0;
        String last="";
        for(Item x:items){ if(!selectedCategory.equals("All")&&!x.category.equals(selectedCategory))continue; if(vegOnly!=null&&vegOnly.isChecked()&&!x.veg)continue; if(!q.isEmpty()&&!x.name.toLowerCase(Locale.ROOT).contains(q)&&!x.category.toLowerCase(Locale.ROOT).contains(q))continue;
            if(!x.category.equals(last)&&selectedCategory.equals("All")){ TextView head=txt(x.category,17,dark,true); head.setPadding(0,dp(12),0,dp(7)); menuContainer.addView(head); last=x.category; }
            menuContainer.addView(card(x)); shown++;
        }
        if(shown==0){ TextView empty=txt("No items found",16,muted,true); empty.setGravity(Gravity.CENTER); empty.setPadding(0,dp(50),0,0); menuContainer.addView(empty); }
    }

    View card(Item x){
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.HORIZONTAL); box.setPadding(dp(14),dp(13),dp(12),dp(13)); box.setGravity(Gravity.CENTER_VERTICAL); box.setBackground(round(Color.WHITE,18,Color.TRANSPARENT));
        LinearLayout info=new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL);
        LinearLayout titleRow=new LinearLayout(this); titleRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView dot=txt("●",13,x.veg?Color.rgb(22,163,74):Color.rgb(220,38,38),true); titleRow.addView(dot,new LinearLayout.LayoutParams(wrap(18),wrap(25)));
        TextView nm=txt(x.name,15,dark,true); nm.setMaxLines(2); titleRow.addView(nm,new LinearLayout.LayoutParams(0,wrap(40),1));
        if(!x.tag.isEmpty()){ TextView tag=txt(x.tag,9,orange,true); tag.setGravity(Gravity.CENTER); tag.setPadding(dp(7),0,dp(7),0); tag.setBackground(round(Color.rgb(255,242,237),20,Color.TRANSPARENT)); titleRow.addView(tag,new LinearLayout.LayoutParams(wrap(75),dp(25))); }
        info.addView(titleRow);
        TextView price=txt("₹"+x.price,15,dark,true); info.addView(price);
        if(!x.desc.isEmpty()){TextView d=txt(x.desc,11,muted,false); d.setMaxLines(2); info.addView(d);}
        box.addView(info,new LinearLayout.LayoutParams(0,-2,1));
        LinearLayout action=new LinearLayout(this); action.setGravity(Gravity.CENTER); int qty=cart.containsKey(x.name)?cart.get(x.name):0;
        if(qty==0){ TextView add=txt("ADD",12,orange,true); add.setGravity(Gravity.CENTER); add.setBackground(round(Color.WHITE,12,orange)); add.setPadding(dp(15),0,dp(15),0); add.setOnClickListener(v->{cart.put(x.name,1);updateCartBar();renderMenu();}); action.addView(add,new LinearLayout.LayoutParams(dp(72),dp(38))); }
        else { TextView minus=txt("−",20,dark,true); minus.setGravity(Gravity.CENTER); minus.setBackground(round(Color.rgb(245,245,245),10,Color.LTGRAY)); TextView qv=txt(""+qty,14,dark,true); qv.setGravity(Gravity.CENTER); TextView plus=txt("+",20,Color.WHITE,true); plus.setGravity(Gravity.CENTER); plus.setBackground(round(orange,10,orange)); action.addView(minus,new LinearLayout.LayoutParams(dp(34),dp(34))); action.addView(qv,new LinearLayout.LayoutParams(dp(32),dp(34))); action.addView(plus,new LinearLayout.LayoutParams(dp(34),dp(34))); minus.setOnClickListener(v->{int n=cart.get(x.name)-1;if(n<=0)cart.remove(x.name);else cart.put(x.name,n);updateCartBar();renderMenu();}); plus.setOnClickListener(v->{cart.put(x.name,cart.get(x.name)+1);updateCartBar();renderMenu();}); }
        box.addView(action,new LinearLayout.LayoutParams(wrap(100),-2)); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,wrap(-2)); lp.setMargins(0,0,0,dp(10)); box.setLayoutParams(lp); return box;
    }

    View cartBar(){ LinearLayout b=new LinearLayout(this); b.setGravity(Gravity.CENTER_VERTICAL); b.setPadding(dp(16),dp(9),dp(16),dp(9)); b.setBackgroundColor(Color.WHITE); TextView left=txt("0 items",13,muted,true); cartCount=left; b.addView(left,new LinearLayout.LayoutParams(0,-1,1)); cartTotal=txt("₹0",16,dark,true); b.addView(cartTotal,new LinearLayout.LayoutParams(wrap(70),-1)); TextView checkout=txt("VIEW CART  ›",13,Color.WHITE,true); checkout.setGravity(Gravity.CENTER); checkout.setBackground(round(orange,14,orange)); checkout.setOnClickListener(v->showCheckout()); b.addView(checkout,new LinearLayout.LayoutParams(dp(125),dp(50))); return b; }

    void updateCartBar(){ int count=0,total=0; for(Item x:items){int q=cart.containsKey(x.name)?cart.get(x.name):0;count+=q;total+=q*x.price;} if(cartCount!=null)cartCount.setText(count+" item"+(count==1?"":"s")); if(cartTotal!=null)cartTotal.setText("₹"+total); }

    void showCheckout(){
        if(cart.isEmpty()){Toast.makeText(this,"Your cart is empty",Toast.LENGTH_SHORT).show();return;}
        LinearLayout body=new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL); body.setPadding(dp(20),dp(8),dp(20),0); ScrollView sv=new ScrollView(this); LinearLayout list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL);
        int total=0; for(Item x:items){if(!cart.containsKey(x.name))continue; int q=cart.get(x.name); total+=q*x.price; TextView r=txt(q+" × "+x.name+"     ₹"+(q*x.price),14,dark,false); r.setPadding(0,dp(9),0,dp(9)); list.addView(r);} sv.addView(list); body.addView(sv,new LinearLayout.LayoutParams(-1,dp(300))); TextView totalV=txt("Total  ₹"+total,19,dark,true); totalV.setGravity(Gravity.RIGHT); totalV.setPadding(0,dp(12),0,dp(12)); body.addView(totalV);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Your Order").setView(body).setNegativeButton("CLOSE",null).setPositiveButton("PLACE ORDER",null).create(); d.setOnShowListener(x->{d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(orange); d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{d.dismiss();cart.clear();updateCartBar();renderMenu();Toast.makeText(this,"Order placed successfully! 🎉",Toast.LENGTH_LONG).show();});}); d.show();
    }

    TextView txt(String s,float size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setTypeface(Typeface.DEFAULT,bold?Typeface.BOLD:Typeface.NORMAL);return t;}
    GradientDrawable round(int color,float radius,int stroke){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));if(stroke!=Color.TRANSPARENT)g.setStroke(dp(1),stroke);return g;}
    int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+0.5f);} int wrap(int x){return x<0?LinearLayout.LayoutParams.WRAP_CONTENT:dp(x);}
}
