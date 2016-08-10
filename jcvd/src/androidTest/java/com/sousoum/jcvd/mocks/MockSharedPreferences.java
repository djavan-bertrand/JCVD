package com.sousoum.jcvd.mocks;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MockSharedPreferences implements SharedPreferences {

    private final Map<String, Object> mPrefs = new HashMap<>();
    private int mChangeCnt;

    public int getChangeCnt() {
        return mChangeCnt;
    }

    @Override
    public Map<String, ?> getAll() {
        return mPrefs;
    }

    @Nullable
    @Override
    public String getString(String key, String defValue) {
        return (String) mPrefs.get(key);
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        return (Set<String>) mPrefs.get(key);
    }

    @Override
    public int getInt(String key, int defValue) {
        return 0;
    }

    @Override
    public long getLong(String key, long defValue) {
        return 0;
    }

    @Override
    public float getFloat(String key, float defValue) {
        return 0;
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return false;
    }

    @Override
    public boolean contains(String key) {
        return false;
    }

    @Override
    public Editor edit() {
        return new Editor() {
            @Override
            public Editor putString(String key, String value) {
                mChangeCnt++;
                if (value != null) {
                    mPrefs.put(key, value);
                } else {
                    mPrefs.remove(key);
                }
                return this;
            }

            @Override
            public Editor putStringSet(String key, Set<String> values) {
                mChangeCnt++;
                if (values != null) {
                    mPrefs.put(key, values);
                } else {
                    mPrefs.remove(key);
                }
                return this;
            }

            @Override
            public Editor putInt(String key, int value) {
                return null;
            }

            @Override
            public Editor putLong(String key, long value) {
                return null;
            }

            @Override
            public Editor putFloat(String key, float value) {
                return null;
            }

            @Override
            public Editor putBoolean(String key, boolean value) {
                return null;
            }

            @Override
            public Editor remove(String key) {
                return null;
            }

            @Override
            public Editor clear() {
                return null;
            }

            @Override
            public boolean commit() {
                return false;
            }

            @Override
            public void apply() {
            }
        };
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
    }
}
