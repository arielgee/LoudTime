package com.arielg.loudtime;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.VectorDrawable;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;

import java.util.Locale;
import java.util.Set;

public abstract class BaseFragment extends Fragment {

    public final static String PARAM_OPEN_FRAGMENT = "com.arielg.loudtime.extra.OPEN_FRAGMENT";

    public final static String PARAM_VALUE_OPEN_TIMER_FRAGMENT = "OPEN_TIMER_FRAGMENT";
    public final static String PARAM_VALUE_OPEN_STOPWATCH_FRAGMENT = "OPEN_STOPWATCH_FRAGMENT";

    // The fragment argument representing the section number for this fragment
    static final String ARG_SECTION_NUMBER = "section_number";

    final long COUNT_DOWN_INTERVAL = 1000;
    String UTTERANCE_ID;

    TextToSpeech mTts = null;

    FloatingActionButton mFab;

    private int mIconResId = 0;
    private Bitmap mLargeIcon = null;

    //====================================================================================================
    protected abstract void onActionButton();

    //====================================================================================================
    void InitTextToSpeechEngine(final boolean maleVoice) {

        if (mTts != null)
            return;

        final String voiceNamePrefix = (maleVoice ? "en-us-x-sfg#male" : "en-us-x-sfg#female");

        mTts = new TextToSpeech(this.getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                mTts.setLanguage(Locale.US);
                mTts.setSpeechRate((float) 1.5);

                Set<Voice> vs = mTts.getVoices();
                for (Voice v : vs) {
                    if (v.getName().startsWith(voiceNamePrefix)) {
                        mTts.setVoice(v);
                        return;
                    }
                }
                /*          Log.d("(v)", "Default: " + mTts.getDefaultVoice().toString());
                            for(int i=0; i<mTts.getVoices().toArray().length; i++) {
                                Log.d("(voice)", "#" + i +" - " + mTts.getVoices().toArray()[i].toString());
                            }                                                                                               */
            }
        });

    }

    //====================================================================================================
    @Override
    public void onDestroy() {

        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }

        super.onDestroy();
    }

    //====================================================================================================
    void Notify(Context context, String title, String text, String fragmentToOpen, int notificationId, int iconResId) {

        if(mIconResId != iconResId || mLargeIcon == null) try {
            VectorDrawable d = (VectorDrawable) context.getResources().getDrawable(iconResId, null);
            mLargeIcon = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mLargeIcon);
            d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            d.draw(canvas);
            mIconResId = iconResId;
        } catch (Exception e) {
            mIconResId = 0;
            mLargeIcon = null;
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.putExtra(PARAM_OPEN_FRAGMENT, fragmentToOpen);
        PendingIntent contentIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(context)
                .setTicker(context.getResources().getString(R.string.app_name))
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(mLargeIcon)
                .setContentIntent(contentIntent);

        // Gets an instance of the NotificationManager service
        NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifyMgr.notify(notificationId, builder.build());
    }

    //====================================================================================================
    void DismissNotify(Context context, int notificationId) {
        NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifyMgr.cancel(notificationId);
    }
}
