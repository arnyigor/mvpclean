package com.arny.mvpclean.data.adapters;

public class SupportDefaultFilter implements SupportBindableAdapterFilter {

    @Override
    public boolean onFilterItem(CharSequence constraint, String valueText) {
        valueText = valueText.toLowerCase();
        // First match against the whole, non-splitted value
        if (valueText.startsWith(constraint.toString())) {
            return true;
        } else {
            final String[] words = valueText.split(" ");

            // Start at index 0, in case valueText starts with space(s)
            for (String word : words) {
                if (word.contains(constraint)) {
                    return true;
                }
            }
        }
        return false;
    }
}