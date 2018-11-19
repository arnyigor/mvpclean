package com.arny.mvpclean.data.adapters;

import android.support.annotation.LayoutRes
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter


abstract class SimpleAbstractAdapter<T>(private var items: ArrayList<T> = arrayListOf()) : RecyclerView.Adapter<SimpleAbstractAdapter.VH>() {
    protected var listener: OnViewHolderListener<T>? = null
    private val filter = ArrayFilter()
    private val lock = Any()
    protected abstract fun getLayout(): Int
    protected abstract fun bindView(item: T, viewHolder: VH)
    protected abstract fun getDiffCallback(): DiffCallback<T>?
    private var onFilterObjectCallback: OnFilterObjectCallback? = null
    private var constraint: CharSequence? = ""

    override fun onBindViewHolder(vh: VH, position: Int) {
        bindView(getItem(position), vh)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(parent, getLayout())
    }

    override fun getItemCount(): Int = items.size

    protected abstract class DiffCallback<T> : DiffUtil.Callback() {
        private val mOldItems = ArrayList<T>()
        private val mNewItems = ArrayList<T>()

        fun setItems(oldItems: List<T>, newItems: List<T>) {
            mOldItems.clear()
            mOldItems.addAll(oldItems)
            mNewItems.clear()
            mNewItems.addAll(newItems)
        }

        override fun getOldListSize(): Int {
            return mOldItems.size
        }

        override fun getNewListSize(): Int {
            return mNewItems.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return areItemsTheSame(
                    mOldItems[oldItemPosition],
                    mNewItems[newItemPosition]
            )
        }

        abstract fun areItemsTheSame(oldItem: T, newItem: T): Boolean

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return areContentsTheSame(
                    mOldItems[oldItemPosition],
                    mNewItems[newItemPosition]
            )
        }

        abstract fun areContentsTheSame(oldItem: T, newItem: T): Boolean
    }

    class VH(parent: ViewGroup, @LayoutRes layout: Int) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false))

    interface OnViewHolderListener<T> {
        fun onItemClick(position: Int, item: T)
    }

    fun getItem(position: Int): T {
        return items[position]
    }

    fun getItems(): ArrayList<T> {
        return items
    }

    fun setViewHolderListener(listener: OnViewHolderListener<T>) {
        this.listener = listener
    }

    fun addAll(list: List<T>) {
        val diffCallback = getDiffCallback()
        if (diffCallback != null && !items.isEmpty()) {
            diffCallback.setItems(items, list)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            items.clear()
            items.addAll(list)
            diffResult.dispatchUpdatesTo(this)
        } else {
            items.addAll(list)
            notifyDataSetChanged()
        }
    }

    fun add(item: T) {
        items.add(item)
        notifyDataSetChanged()
    }

    fun add(position: Int, item: T) {
        items.add(position, item)
        notifyItemInserted(position)
    }

    fun remove(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun remove(item: T) {
        items.remove(item)
        notifyDataSetChanged()
    }

    fun clear(notify: Boolean = false) {
        items.clear()
        if (notify) {
            notifyDataSetChanged()
        }
    }

    fun setFilter(filter: SimpleAdapterFilter<T>): ArrayFilter {
        return this.filter.setFilter(filter)
    }

    interface SimpleAdapterFilter<T> {
        fun onFilterItem(contains: CharSequence, item: T): Boolean
    }

    fun convertResultToString(resultValue: Any): CharSequence {
        return filter.convertResultToString(resultValue)
    }

    fun filter(constraint: CharSequence) {
        this.constraint = constraint
        filter.filter(constraint)
    }

    fun filter(constraint: CharSequence, listener: Filter.FilterListener) {
        this.constraint = constraint
        filter.filter(constraint, listener)
    }

    protected fun itemToString(item: T): String? {
        return item.toString()
    }

    fun getFilter(): Filter {
        return filter
    }

    interface OnFilterObjectCallback {
        fun handle(countFilterObject: Int)
    }

    fun setOnFilterObjectCallback(objectCallback: OnFilterObjectCallback) {
        onFilterObjectCallback = objectCallback
    }

    inner class ArrayFilter : Filter() {
        private var filter: SimpleAdapterFilter<T> = DefaultFilter<T>()

        private val supportFilter = SupportDefaultFilter()

        fun setFilter(filter: SimpleAdapterFilter<T>): ArrayFilter {
            this.filter = filter
            return this
        }

        override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
            val results = Filter.FilterResults()
            if (constraint == null || constraint.isEmpty()) {
                var list: ArrayList<T> = arrayListOf()
                synchronized(lock) {
                    list = ArrayList(getItems())
                }
                results.values = list
                results.count = list.size
            } else {
                var values: ArrayList<T> = arrayListOf()
                synchronized(lock) {
                    values = ArrayList(getItems())
                }
                val newValues = ArrayList<T>()
                for (value in values) {
                    if (!constraint.isBlank() && value != null) {
                        if (supportFilter.onFilterItem(constraint, itemToString(value))) {
                            value.let { newValues.add(it) }
                        }
                    } else {
                        value?.let { newValues.add(it) }
                    }
                }

//                for (value in values) {
//                    if (itemToString(value) != null) {
//                        if (supportFilter.onFilterItem(constraint, itemToString(value))) {
//                            newValues.add(value)
//                        }
//                    } else {
//                        if (filter == null) {
//                            throw RuntimeException("filter must not be null")
//                        }
//                        if (filter.onFilterItem(constraint, value)) {
//                            newValues.add(value)
//                        }
//                    }
//                }
                results.values = newValues
                results.count = newValues.size
            }
            return results
        }

        override fun publishResults(constraint: CharSequence, results: Filter.FilterResults) {
            items = results.values as? ArrayList<T> ?: arrayListOf()
            onFilterObjectCallback?.handle(results.count)
            notifyDataSetChanged()
        }

    }

    class DefaultFilter<T> : SimpleAdapterFilter<T> {
        override fun onFilterItem(contains: CharSequence, item: T): Boolean {
            val valueText = item.toString().toLowerCase()
            if (valueText.startsWith(contains.toString())) {
                return true
            } else {
                val words = valueText.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (word in words) {
                    if (word.contains(contains)) {
                        return true
                    }
                }
            }
            return false
        }
    }
}