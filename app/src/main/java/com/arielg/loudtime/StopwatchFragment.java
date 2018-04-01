package com.arielg.loudtime;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class StopwatchFragment extends BaseFragment {

    private TextView mTextStopwatch;

    private Stopwatch mStopwatch = null;

    private boolean m1MinuteContinuousSpeak = false;
    private int mIconResId = R.drawable.ic_launcher;

    //====================================================================================================
    public StopwatchFragment() {
        UTTERANCE_ID = "338";
        mIconResId = R.drawable.ic_stopwatch;
    }

    //====================================================================================================
    // Returns a new instance of this fragment for the given section number.
    public static StopwatchFragment newInstance(int sectionNumber) {
        StopwatchFragment fragment = new StopwatchFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    //====================================================================================================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_stopwatch, container, false);

        InitTextToSpeechEngine(false);

        int mColorProgress;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mColorProgress = getResources().getColor(R.color.progress, null);
        } else {
            mColorProgress = getResources().getColor(R.color.progress);
        }

        mTextStopwatch = (TextView) rootView.findViewById(R.id.textTimeCounter);
        TextView mBtnToggle1MinuteContinuousSpeak = (TextView) rootView.findViewById(R.id.btnToggle1MinuteContinuousSpeak);
        mFab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        mTextStopwatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onResetStopwatch();
            }
        });
        mTextStopwatch.setTextColor(mColorProgress);

        mBtnToggle1MinuteContinuousSpeak.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                view.playSoundEffect(SoundEffectConstants.CLICK);
                m1MinuteContinuousSpeak = !m1MinuteContinuousSpeak;
                Toast.makeText(getContext(), "One minute continuous speak " + (m1MinuteContinuousSpeak ?"ON":"OFF"), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        mFab.setImageResource(android.R.drawable.ic_media_play);
        mFab.setVisibility(View.VISIBLE);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onActionButton();
            }
        });

        return rootView;
    }

    //====================================================================================================
    private void onResetStopwatch() {

        // can't reset if not created
        if(mStopwatch == null || mStopwatch.isRunning())
            return;

        mStopwatch.Stop();
        mStopwatch = null;
        mTextStopwatch.setText(getString(R.string.zero_time));
        mFab.setImageResource(android.R.drawable.ic_media_play);
        mTts.speak("Resetting", TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
        DismissNotify(getContext(), Integer.parseInt(UTTERANCE_ID));
    }

    //====================================================================================================
    @Override
    protected void onActionButton() {

        if (mStopwatch == null) {

            mFab.setImageResource(android.R.drawable.ic_media_pause);

            mTts.speak("Starting", TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
            (mStopwatch = new Stopwatch(this.getContext(), COUNT_DOWN_INTERVAL)).Start();

        } else if (mStopwatch.isRunning()) {
            mTts.speak("Pausing", TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
            mStopwatch.Pause();
            mFab.setImageResource(android.R.drawable.ic_media_play);
            Notify(getContext(), "Stopwatch is paused.", mTextStopwatch.getText().toString(), TimerFragment.PARAM_VALUE_OPEN_STOPWATCH_FRAGMENT, Integer.parseInt(UTTERANCE_ID), mIconResId);
        } else {
            mTts.speak("Resuming", TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
            mStopwatch.Resume();
            mFab.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    //====================================================================================================
    //====================================================================================================
    class Stopwatch extends TimeCounter {

        //====================================================================================================
        public Stopwatch(Context context, long counterInterval) {
            super(context, counterInterval);
        }

        //====================================================================================================
        @Override
        void onCounterInterval(long millis) {

            if(millis > 0) {
                int sec = (int) ((millis / 1000) % 60);
                int min = (int) ((millis / (60000)) % 60);
                int hour = (int) (millis / 3600000);
                String text = String.format(Locale.getDefault(), "%d:%02d:%02d", hour, min, sec);

                mTextStopwatch.setText(text);
                Notify(getContext(), "Stopwatch is running.", text, TimerFragment.PARAM_VALUE_OPEN_STOPWATCH_FRAGMENT, Integer.parseInt(UTTERANCE_ID), mIconResId);

                String speak = null;
                if(hour == 0 && min == 0) {
                    if (sec <= 10 || m1MinuteContinuousSpeak) {
                        speak = Integer.toString(sec) + " sec.";
                    } else if ((sec % 10) == 0) {               // else if ((sec % 10) == 0 || sec == 5)) {
                        speak = Integer.toString(sec) + " seconds";
                    }
                } else if (sec == 0 || sec == 30) {
                    if (hour > 0) {
                        speak = text;
                    } else if (min > 0) {
                        speak = Integer.toString(min) + " minute" + (min > 1 ? "s " : " ") + "and " + Integer.toString(sec) + " seconds";
                    }
                }

                if(speak != null)
                    mTts.speak(speak, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
            }
        }
    }
}
