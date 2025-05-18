package com.example.nyaritabor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class BookingCampAdapter extends RecyclerView.Adapter<BookingCampAdapter.ViewHolder> implements Filterable {
    private ArrayList<BookingCamp> mBookingCampData = new ArrayList<>();
    private ArrayList<BookingCamp> mBookingCampDataAll = new ArrayList<>();
    private Context mContext;
    private int lastPosition = -1;

    BookingCampAdapter(Context context, ArrayList<BookingCamp> itemsData) {
        this.mBookingCampData = itemsData;
        this.mBookingCampDataAll = itemsData;
        this.mContext = context;
    }

    public interface OnBookingClickListener {
        void onBookingClick(BookingCamp camp);
    }

    private OnBookingClickListener bookingClickListener;

    public void setOnBookingClickListener(OnBookingClickListener listener) {
        this.bookingClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(BookingCampAdapter.ViewHolder holder, int position) {
        BookingCamp currentItem = mBookingCampData.get(position);

        holder.bindTo(currentItem);


        if(holder.getAdapterPosition() > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.silide_in_row);
            holder.itemView.startAnimation(animation);
            lastPosition = holder.getAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return mBookingCampData.size();
    }


    @Override
    public Filter getFilter() {
        return bookingFilter;
    }

    private Filter bookingFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<BookingCamp> filteredList = new ArrayList<>();
            FilterResults results = new FilterResults();

            if(charSequence == null || charSequence.length() == 0) {
                results.count = mBookingCampDataAll.size();
                results.values = mBookingCampDataAll;
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for(BookingCamp item : mBookingCampDataAll) {
                    if(item.getName().toLowerCase().contains(filterPattern)){
                        filteredList.add(item);
                    }
                }

                results.count = filteredList.size();
                results.values = filteredList;
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mBookingCampData = (ArrayList)filterResults.values;
            notifyDataSetChanged();
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitleText;
        private TextView mInfoText;
        private TextView mPriceText;
        private ImageView mItemImage;
        private RatingBar mRatingBar;
        private TextView mAgeText;
        private TextView mDateText;
        private TextView mFreeSpotsText;
        private TextView mPlace;

        ViewHolder(View itemView) {
            super(itemView);

            mTitleText = itemView.findViewById(R.id.itemTitle);
            mInfoText = itemView.findViewById(R.id.subTitle);
            mItemImage = itemView.findViewById(R.id.itemImage);
            mRatingBar = itemView.findViewById(R.id.ratingBar);
            mPriceText = itemView.findViewById(R.id.price);
            mAgeText = itemView.findViewById(R.id.age);
            mDateText = itemView.findViewById(R.id.date);
            mFreeSpotsText = itemView.findViewById(R.id.freeSpots);
            mPlace = itemView.findViewById(R.id.place);


        }

        void bindTo(BookingCamp currentItem){
            mTitleText.setText(currentItem.getName());
            mInfoText.setText(currentItem.getInfo());
            mPriceText.setText(currentItem.getPrice() + " €");
            mRatingBar.setRating(currentItem.getRatedInfo());
            mAgeText.setText("Korcsoport: " + currentItem.getAgeGroup());
            mDateText.setText(currentItem.getDate());
            mFreeSpotsText.setText(currentItem.getFreePlace() + " szabad hely");
            mPlace.setText(currentItem.getPlace());

            Glide.with(mContext).load(currentItem.getImageResource()).into(mItemImage);
            itemView.findViewById(R.id.add_to_cart).setOnClickListener(view -> {
                if (bookingClickListener != null) {
                    bookingClickListener.onBookingClick(currentItem);
                }
            });
            itemView.findViewById(R.id.delete).setOnClickListener(view -> ((BookingListActivity)mContext).deleteCamp(currentItem));
        }
    }
}

