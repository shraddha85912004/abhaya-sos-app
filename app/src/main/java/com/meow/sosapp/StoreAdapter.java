package com.meow.sosapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Locale; // Import Locale for String.format

// Custom adapter to display Store objects in a ListView
public class StoreAdapter extends BaseAdapter {
    private Context context;
    private List<Store> stores;
    private LayoutInflater inflater; // Use LayoutInflater directly

    /**
     * Constructor for the StoreAdapter.
     *
     * @param context The context (usually the Activity).
     * @param stores The list of Store objects to display.
     */
    public StoreAdapter(Context context, List<Store> stores) {
        this.context = context;
        this.stores = stores;
        this.inflater = LayoutInflater.from(context); // Initialize LayoutInflater
    }

    /**
     * Returns the number of items in the list.
     */
    @Override
    public int getCount() {
        return stores.size();
    }

    /**
     * Gets the data item associated with the specified position in the list.
     * @param position The position of the item.
     * @return The Store object at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return stores.get(position);
    }

    /**
     * Gets the row id associated with the specified position in the list.
     * @param position The position of the item.
     * @return The row id (using position as id here).
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Creates or recycles a view to display the data for the specified position.
     * This method is crucial for optimizing ListView performance.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder; // Use a ViewHolder pattern

        // Check if an existing view is being reused, otherwise inflate a new one
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.store_item, parent, false);
            holder = new ViewHolder();
            // Find the TextViews and store them in the ViewHolder
            holder.storeName = convertView.findViewById(R.id.store_name);
            holder.storeAddress = convertView.findViewById(R.id.store_address);
            holder.storeDistance = convertView.findViewById(R.id.store_distance);
            // holder.storePhone = convertView.findViewById(R.id.store_phone); // Commented out if not used

            // Set the ViewHolder as a tag on the view
            convertView.setTag(holder);
        } else {
            // If view is recycled, retrieve the ViewHolder from the tag
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the Store object for the current position
        Store store = stores.get(position);

        // Populate the TextViews with data from the Store object
        holder.storeName.setText(store.getName());
        holder.storeAddress.setText(store.getAddress());

        // Format the distance for display (convert meters to kilometers)
        // Using Locale.getDefault() is good practice for localization
        holder.storeDistance.setText(String.format(Locale.getDefault(), "%.2f km", store.getDistance() / 1000));

        // Set the phone number text (if you decide to include it later)
        // holder.storePhone.setText(store.getPhoneNumber());

        return convertView; // Return the populated view
    }

    // ViewHolder pattern to improve ListView scrolling performance
    // Holds references to the TextViews in the list item layout
    private static class ViewHolder {
        TextView storeName;
        TextView storeAddress;
        TextView storeDistance;
        // TextView storePhone; // Include if phone number is displayed
    }

    /**
     * Notifies the attached observers that the underlying data has been changed
     * and any View reflecting the data set should refresh itself.
     * This method is already in BaseAdapter, but keeping it here for clarity if needed.
     */
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    /**
     * Method to update the data in the adapter and refresh the ListView.
     * This is a cleaner way to update the adapter's data.
     * @param newStores The new list of Store objects.
     */
    public void updateStores(List<Store> newStores) {
        this.stores = newStores; // Update the internal list reference
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }
}
