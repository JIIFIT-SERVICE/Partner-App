package com.company.jifiit;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

  private static  String SHARED_PREF_NAME = "Jiifit";
  private SharedPreferences sharedPreferences;
  Context context;
  private SharedPreferences.Editor editor;


    public SharedPrefManager(Context context) {
        this.context = context;
    }

    public  void savedData(String name, int Id){
      sharedPreferences=context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
      editor=sharedPreferences.edit();
      editor.putString("Name", name);
      editor.putInt("Id",Id);

    }
}
