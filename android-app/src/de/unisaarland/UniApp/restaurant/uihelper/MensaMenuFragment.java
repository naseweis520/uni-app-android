package de.unisaarland.UniApp.restaurant.uihelper;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.restaurant.MensaMenuActivity.Campuses;
import de.unisaarland.UniApp.restaurant.model.CachedMensaPlan;
import de.unisaarland.UniApp.restaurant.model.MensaDayMenu;
import de.unisaarland.UniApp.utils.NetworkRetrieveAndCache;
import de.unisaarland.UniApp.utils.Util;
import de.unisaarland.UniApp.utils.ui.RemoteOrLocalViewAdapter;

public class MensaMenuFragment extends Fragment {
    private CachedMensaPlan mensaPlan = null;
    private Campuses campus;
    private View rootView;

    private long lastSelectedDate = 0;
    private int positionToSelect = -1;

    public static MensaMenuFragment newInstance(Campuses campus) {
        MensaMenuFragment mensaMenuFragment = new MensaMenuFragment();
        mensaMenuFragment.campus = campus;
        return mensaMenuFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mensa_menu, container, false);
        this.rootView = rootView;

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // The widget passes the item to select, and it's always for the current date.
        positionToSelect = getActivity().getIntent().getIntExtra("position", -1);
        if (positionToSelect != -1) {
            lastSelectedDate = 0;
        }

        ProgressBar bar = rootView.findViewById(R.id.progress_bar);
        bar.setVisibility(View.GONE);

        if (mensaPlan == null) {
            mensaPlan = new CachedMensaPlan(campus, new NetworkDelegate(), getContext());
        }

        mensaPlan.load(15 * 60);
    }

    @Override
    public void onStop() {
        if (mensaPlan != null) {
            mensaPlan.cancel();
            mensaPlan = null;
        }
        super.onStop();
    }

    private void populateItems(final MensaDayMenu[] items) {
        // compute which item to preselect (smallest item >= current day, or last selected item)
        long dateToSelect = lastSelectedDate != 0 ? lastSelectedDate : Util.getStartOfDay().getTimeInMillis();
        int itemToSelect = 0;
        for (MensaDayMenu day : items) {
            if (day.getDayStartMillis() < dateToSelect) {
                ++itemToSelect;
            }
        }

        ViewFlow viewFlow = rootView.findViewById(R.id.viewflow);
        RemoteOrLocalViewAdapter.LocalAdapter adapter = (RemoteOrLocalViewAdapter.LocalAdapter) viewFlow.getAdapter();
        MensaDaysAdapter newAdapter = new MensaDaysAdapter(items, false);
        if (adapter == null) {
            viewFlow.setAdapter(newAdapter.asLocalAdapter(getContext()),
                    itemToSelect);
        } else {
            adapter.update(newAdapter);
            if (viewFlow.getSelectedItemPosition() != itemToSelect) {
                viewFlow.setSelection(itemToSelect);
            }
        }
        viewFlow.setOnViewSwitchListener((view, position) -> lastSelectedDate = items[position].getDayStartMillis());
        CircleFlowIndicator indic = rootView.findViewById(R.id.viewflowindic);
        viewFlow.setFlowIndicator(indic);
        if (positionToSelect != -1) {
            ListView mensaList = viewFlow.getSelectedView().findViewById(R.id.mensaList);
            int count = mensaList.getCount();
            if (positionToSelect < count) {
                mensaList.setSelection(positionToSelect);
            }
        }
    }

    private final class NetworkDelegate
            implements NetworkRetrieveAndCache.Delegate<MensaDayMenu[]> {
        private boolean hasItems = false;

        @Override
        public void onUpdate(MensaDayMenu[] result, boolean fromCache) {
            hasItems = true;

            ProgressBar bar = rootView.findViewById(R.id.progress_bar);
            bar.setVisibility(View.GONE);
            populateItems(result);
        }

        @Override
        public void onStartLoading() {
            ProgressBar bar = rootView.findViewById(R.id.progress_bar);
            bar.setVisibility(View.VISIBLE);
            bar.animate();
        }

        @Override
        public void onFailure(String message) {
            ProgressBar bar = rootView.findViewById(R.id.progress_bar);
            bar.setVisibility(View.GONE);
            Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
                    .show();
        }

        @Override
        public String checkValidity(MensaDayMenu[] result) {
            // validity was already checked in CachedMensaPlan
            return null;
        }
    }
}