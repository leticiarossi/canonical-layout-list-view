package com.example.listviewcanonicallayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.ReactiveGuide;
import androidx.core.util.Consumer;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.window.java.layout.WindowInfoTrackerCallbackAdapter;
import androidx.window.layout.DisplayFeature;
import androidx.window.layout.FoldingFeature;
import androidx.window.layout.WindowInfoTracker;
import androidx.window.layout.WindowLayoutInfo;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigationrail.NavigationRailView;

import java.util.List;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private View detailViewContainer;
    private ReactiveGuide guideline;

    @Nullable private WindowInfoTrackerCallbackAdapter windowInfoTracker;
    private final Consumer<WindowLayoutInfo> stateContainer = new StateContainer();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Executor executor = command -> handler.post(() -> handler.post(command));

    private ConstraintLayout constraintLayout;
    private Configuration configuration;
    private FragmentManager fragmentManager;
    private AdaptiveListViewFragment listViewFragment;
    private AdaptiveListViewDetailFragment detailViewFragment;

    @Override
    protected void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);

        windowInfoTracker =
                new WindowInfoTrackerCallbackAdapter(WindowInfoTracker.getOrCreate(this));
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        constraintLayout = findViewById(R.id.list_view_activity_constraint_layout);
        NavigationView modalNavDrawer = findViewById(R.id.modal_nav_drawer);
        detailViewContainer = findViewById(R.id.list_view_detail_fragment_container);
        guideline = findViewById(R.id.guideline);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        FloatingActionButton fab = findViewById(R.id.fab);
        NavigationRailView navRail = findViewById(R.id.nav_rail);
        NavigationView navDrawer = findViewById(R.id.nav_drawer);
        ExtendedFloatingActionButton navFab = findViewById(R.id.nav_fab);

        configuration = getResources().getConfiguration();
        fragmentManager = getSupportFragmentManager();
        listViewFragment = new AdaptiveListViewFragment();
        detailViewFragment = new AdaptiveListViewDetailFragment();

        // Update navigation views according to screen width size.
        int screenWidth = configuration.screenWidthDp;
        AdaptiveUtils.updateNavigationViewLayout(
                screenWidth, drawerLayout, modalNavDrawer, fab, bottomNav, navRail, navDrawer, navFab);

        // Clear backstack to prevent unexpected behaviors when pressing back button.
        int backStrackEntryCount = fragmentManager.getBackStackEntryCount();
        for (int entry = 0; entry < backStrackEntryCount; entry++) {
            fragmentManager.popBackStack();
        }
    }

    private void updatePortraitLayout() {
        int listViewFragmentId = R.id.list_view_fragment_container;
        guideline.setGuidelineEnd(0);
        detailViewContainer.setVisibility(View.GONE);
        listViewFragment.setDetailViewContainerId(listViewFragmentId);
        fragmentManager.beginTransaction().replace(listViewFragmentId, listViewFragment).commit();
    }

    private void updateLandscapeLayout(int guidelinePosition, int foldWidth) {
        int listViewFragmentId = R.id.list_view_fragment_container;
        int detailViewFragmentId = R.id.list_view_detail_fragment_container;
        ConstraintSet landscapeLayout = new ConstraintSet();
        landscapeLayout.clone(constraintLayout);
        landscapeLayout.setMargin(detailViewFragmentId, ConstraintSet.START, foldWidth);
        landscapeLayout.applyTo(constraintLayout);
        guideline.setGuidelineEnd(guidelinePosition);
        listViewFragment.setDetailViewContainerId(detailViewFragmentId);
        fragmentManager
                .beginTransaction()
                .replace(listViewFragmentId, listViewFragment)
                .replace(detailViewFragmentId, detailViewFragment)
                .commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (windowInfoTracker != null) {
            windowInfoTracker.addWindowLayoutInfoListener(this, executor, stateContainer);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (windowInfoTracker != null) {
            windowInfoTracker.removeWindowLayoutInfoListener(stateContainer);
        }
    }

    private class StateContainer implements Consumer<WindowLayoutInfo> {

        public StateContainer() {}

        @Override
        public void accept(WindowLayoutInfo windowLayoutInfo) {

            List<DisplayFeature> displayFeatures = windowLayoutInfo.getDisplayFeatures();
            boolean hasVerticalFold = false;

            // Update layout according to orientation.
            if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                updatePortraitLayout();
            } else {
                for (DisplayFeature displayFeature : displayFeatures) {
                    if (displayFeature instanceof FoldingFeature) {
                        FoldingFeature foldingFeature = (FoldingFeature) displayFeature;
                        FoldingFeature.Orientation orientation = foldingFeature.getOrientation();
                        if (orientation.equals(FoldingFeature.Orientation.VERTICAL)) {
                            int foldPosition = foldingFeature.getBounds().left;
                            int foldWidth = foldingFeature.getBounds().right - foldPosition;
                            updateLandscapeLayout(foldPosition, foldWidth);
                            hasVerticalFold = true;
                        }
                    }
                }
                if (!hasVerticalFold) {
                    updateLandscapeLayout(constraintLayout.getWidth() / 2, 0);
                }
            }
        }
    }
}