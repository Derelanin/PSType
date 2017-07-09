package com.example.pstype_v1.useful;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Derelanin on 04.06.2017.
 */

public class Functions {
    Context mContext;

    public Functions(Context mContext) {
        this.mContext = mContext;
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    public String translate (String text) {
        if (text.equals("User with this username already exists")) return "Пользователь с таким именем уже существует";
        if (text.equals("Username is required")) return "Отсутсвует имя пользователя";
        if (text.equals("Password is required")) return "Отсутсвует пароль";
        if (text.equals("Age must be >= 14")) return "Возраст должен быть больше либо равен 14";
        if (text.equals("Age must be <= 110")) return "Возраст должен быть меньше либо равен 110";
        if (text.equals("Username must be longer than 6 symbols and consist only of latin symbols")) return "Имя пользователя должно содержать только латинские буквы и цифры и состоять из 6 и более символов";
        if (text.equals("Password must be longer than 6 symbols and consist only of latin symbols")) return "Пароль должен содержать только латинские буквы и цифры и состоять из 6 и более символов";
        if (text.equals("Age must be an integer")) return "Возраст должен быть целым числом";
        if (text.contains("Cast to Number")) return "Возраст должен быть целым числом";
        return text;
    }

    public static Drawable tintMyDrawable(Drawable drawable, int color) {
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, color);
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
        return drawable;
    }

}
