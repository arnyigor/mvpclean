package com.arny.mvpclean.data.adapters;

public interface SupportBindableAdapterFilter {
    boolean onFilterItem(CharSequence constraint, String item);
}