package com.example.listviewcanonicallayout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

/** A Fragment that displays an email's details. */
public class AdaptiveListViewDetailFragment extends Fragment {
    public static final String TAG = "AdaptiveListViewDetailDemoFragment";
    private static final String EMAIL_ID_KEY = "email_id_key";

    @NonNull
    public static AdaptiveListViewDetailFragment newInstance(long emailId) {
        AdaptiveListViewDetailFragment fragment = new AdaptiveListViewDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(EMAIL_ID_KEY, emailId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater layoutInflater,
            @Nullable ViewGroup viewGroup,
            @Nullable Bundle bundle) {
        return layoutInflater.inflate(
                R.layout.fragment_detail, viewGroup, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
        long emailId = getEmailId();
        TextView emailTitle = view.findViewById(R.id.email_title);
        emailTitle.append(" " + (emailId + 1));
        // Set transition name that matches the list item to be transitioned from for the shared element
        // transition.
        View container = view.findViewById(R.id.list_view_detail_container);
        ViewCompat.setTransitionName(container, emailTitle.toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        updateEmailSelected(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateEmailSelected(true);
    }

    private void updateEmailSelected(boolean selected) {
        AdaptiveListViewFragment.EmailData.Email email =
                AdaptiveListViewFragment.EmailData.getEmailById(getEmailId());
        email.setSelected(selected);
    }

    private long getEmailId() {
        long emailId = 0L;
        if (getArguments() != null) {
            emailId = getArguments().getLong(EMAIL_ID_KEY, 0L);
        }
        return emailId;
    }
}
