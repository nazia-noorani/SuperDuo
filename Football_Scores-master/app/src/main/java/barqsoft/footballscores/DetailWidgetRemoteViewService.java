package barqsoft.footballscores;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by nazianoorani on 10/03/16.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewService extends RemoteViewsService {
    private static final String[] FORECAST_COLUMNS = {
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.TIME_COL

    };
    // indices must match the projection
    private static final int INDEX_MATCH_ID = 0;
    private static final int INDEX_HOME = 1;
    private static final int INDEX_AWAY = 2;
    private static final int INDEX_AWAY_GOALS = 3;
    private static final int INDEX_HOME_GOALS = 4;
    private static final int INDEX_TIME = 5;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;
            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {

                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(), FORECAST_COLUMNS, DatabaseContract.scores_table.DATE_COL + " = ",
                        new String[] {new SimpleDateFormat("yyyy-MM-dd").format(new Date())}, DatabaseContract.scores_table.TIME_COL );


                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                if(data != null){
                return data.getCount();}
                else { return 0;}
            }

            @Override
            public RemoteViews getViewAt(int position) {

                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);


                String homeName = data.getString(INDEX_HOME);
                String awayName = data.getString(INDEX_AWAY);
                int homeCrest = Utilies.getTeamCrestByTeamName(homeName, getApplicationContext());
                int awayCrest = Utilies.getTeamCrestByTeamName(awayName, getApplicationContext());
                String matchScore = Utilies.getScores(data.getInt(INDEX_HOME_GOALS), data.getInt(INDEX_AWAY_GOALS));
                String matchTime= data.getString(INDEX_TIME);


                views.setTextViewText(R.id.home_name, homeName);
                views.setTextViewText(R.id.away_name, awayName);
                views.setImageViewResource(R.id.home_crest, homeCrest);
                views.setImageViewResource(R.id.away_crest, awayCrest);
                views.setTextViewText(R.id.widget_score_textview, matchScore);
                views.setTextViewText(R.id.widget_data_textview, matchTime);

                final Intent fillInIntent = new Intent();
                Uri scoreUri = DatabaseContract.scores_table.buildScoreWithId();
                fillInIntent.setData(scoreUri);
                views.setOnClickFillInIntent(R.id.widget, fillInIntent);



                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_MATCH_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
