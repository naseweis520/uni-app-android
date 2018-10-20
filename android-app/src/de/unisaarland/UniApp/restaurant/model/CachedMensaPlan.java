package de.unisaarland.UniApp.restaurant.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.restaurant.MensaAppWidgetProvider;
import de.unisaarland.UniApp.restaurant.MensaMenuActivity.Campuses;
import de.unisaarland.UniApp.utils.ContentCache;
import de.unisaarland.UniApp.utils.NetworkRetrieveAndCache;
import de.unisaarland.UniApp.utils.Util;

public class CachedMensaPlan {
    private static final String MENSA_URL_HOM = "http://studentenwerk-saarland.de/_menu/actual/speiseplan-homburg.xml";
    private static final String MENSA_URL_MENSAGARTEN = "http://studentenwerk-saarland.de/_menu/actual/speiseplan-mensagarten.xml";
    private static final String MENSA_URL_SB = "http://studentenwerk-saarland.de/_menu/actual/speiseplan-saarbruecken.xml";
    private final NetworkRetrieveAndCache.Delegate<MensaDayMenu[]> networkDelegate;
    private final Context context;
    private Campuses campus;
    private NetworkRetrieveAndCache<MensaDayMenu[]> mensaFetcher = null;

    public CachedMensaPlan(Campuses campus,
                           NetworkRetrieveAndCache.Delegate<MensaDayMenu[]> networkDelegate,
                           Context context) {
        if (campus == null) {
            // Load campus from preferences
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            String campusString = settings.getString(context.getString(R.string.pref_campus), context.getString(R.string.pref_campus_saar));
            this.campus = campusString.equals(context.getString(R.string.pref_campus_saar)) ? Campuses.Saarbruecken : Campuses.Homburg;
        } else {
            this.campus = campus;
        }
        this.networkDelegate = networkDelegate;
        this.context = context;
    }

    public static boolean loadedSince(Campuses campus, long timeMillis, Context context) {
        CachedMensaPlan plan = new CachedMensaPlan(campus, null, context);
        plan.initFetcher();
        boolean ret = plan.loadedSince(timeMillis);
        plan.cancel();
        return ret;
    }

    public static MensaDayMenu getTodaysMenuIfLoaded(Campuses campus, Context context) {
        final MensaDayMenu[] todayMenu = new MensaDayMenu[1];
        final long todayMillis = Util.getStartOfDay().getTimeInMillis();

        NetworkRetrieveAndCache.Delegate<MensaDayMenu[]> delegate =
                new NetworkRetrieveAndCache.Delegate<MensaDayMenu[]>() {
                    public boolean hasShown = false;

                    @Override
                    public void onUpdate(MensaDayMenu[] result, boolean fromCache) {
                        if (hasShown)
                            throw new AssertionError("we should only be updated once");
                        hasShown = true;
                        for (MensaDayMenu menu : result)
                            if (menu.getDayStartMillis() == todayMillis)
                                todayMenu[0] = menu;
                    }

                    @Override
                    public void onStartLoading() {
                        throw new AssertionError("we should not load");
                    }

                    @Override
                    public void onFailure(String message) {
                        throw new AssertionError("without loading there should be no failure");
                    }

                    @Override
                    public String checkValidity(MensaDayMenu[] result) {
                        return null;
                    }
                };
        CachedMensaPlan plan = new CachedMensaPlan(campus, delegate, context);
        plan.load(-1);
        return todayMenu[0];
    }

    private void initFetcher() {
        String mensaUrl;
        switch (campus) {
            case Homburg:
                mensaUrl = MENSA_URL_HOM;
                break;
            case Mensagarten:
                mensaUrl = MENSA_URL_MENSAGARTEN;
                break;
            case Saarbruecken:
            default:
                mensaUrl = MENSA_URL_SB;
        }

        if (mensaFetcher == null || !mensaUrl.equals(mensaFetcher.getUrl())) {
            ContentCache cache = Util.getContentCache(context);

            String mensaMenuLang = MensaXMLParser.getMensaLanguage(context);
            mensaFetcher = new NetworkRetrieveAndCache<>(mensaUrl, "mensa-" + campus + "-" + mensaMenuLang, cache,
                    new MensaXMLParser(mensaMenuLang), new NetworkDelegate(), context);
        }
    }

    /**
     * @return true if the data was initially loaded from the cache, false otherwise. in any case,
     * reloading might have been triggered.
     */
    public boolean load(int reloadIfOlderSeconds) {
        initFetcher();
        return mensaFetcher.loadAsynchronously(reloadIfOlderSeconds);
    }

    public void cancel() {
        if (mensaFetcher != null) {
            mensaFetcher.cancel();
            mensaFetcher = null;
        }
    }

    public boolean loadedSince(long timeMillis) {
        initFetcher();
        return mensaFetcher.loadedSince(timeMillis);
    }

    private final class NetworkDelegate
            implements NetworkRetrieveAndCache.Delegate<MensaDayMenu[]> {
        @Override
        public void onUpdate(MensaDayMenu[] result, boolean fromCache) {
            if (!fromCache)
                MensaAppWidgetProvider.updateAllWidgets(context);
            if (networkDelegate != null)
                networkDelegate.onUpdate(result, fromCache);
        }

        @Override
        public void onStartLoading() {
            if (networkDelegate != null)
                networkDelegate.onStartLoading();
        }

        @Override
        public void onFailure(String message) {
            if (networkDelegate != null)
                networkDelegate.onFailure(message);
        }

        @Override
        public String checkValidity(MensaDayMenu[] result) {
            if (result.length == 0)
                return context.getString(R.string.emptyDocumentError);
            return null;
        }
    }
}