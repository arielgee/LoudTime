package com.arielg.loudtime;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class TimerFragment extends BaseFragment {

    private final long MAX_BASE_TIME_MILLISECONDS = 359999000;      // 99:59:59

    private TextView mTextCountdown;
    private ProgressBar mProgressBar;

    private String mCacheBaseString;
    private long mBaseTimeMilliseconds;
    private long mProgressbarDivisor;

    private int mColorProgress;
    private int mColorTimesUp;

    private Timer mTimer = null;

    private boolean m1MinuteContinuousSpeak = false;
    private int mIconResId = R.drawable.ic_launcher;

    //====================================================================================================
    public TimerFragment() {
        UTTERANCE_ID = "242";
        mIconResId = R.drawable.ic_timer;
    }

    //====================================================================================================
    // Returns a new instance of this fragment for the given section number.
    public static TimerFragment newInstance(int sectionNumber) {
        TimerFragment fragment = new TimerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    //====================================================================================================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_timer, container, false);

        InitTextToSpeechEngine(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mColorProgress = getResources().getColor(R.color.progress, null);
            mColorTimesUp = getResources().getColor(R.color.timesUp, null);
        } else {
            mColorProgress = getResources().getColor(R.color.progress);
            mColorTimesUp = getResources().getColor(R.color.timesUp);
        }

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mTextCountdown = (TextView) rootView.findViewById(R.id.textTimeCounter);
        TextView mBtnToggle1MinuteContinuousSpeak = (TextView) rootView.findViewById(R.id.btnToggle1MinuteContinuousSpeak);
        mFab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        mTextCountdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSetBaseTime();
            }
        });
        mTextCountdown.setTextColor(mColorProgress);

        mBtnToggle1MinuteContinuousSpeak.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                view.playSoundEffect(SoundEffectConstants.CLICK);
                m1MinuteContinuousSpeak = !m1MinuteContinuousSpeak;
                Toast.makeText(getContext(), "One minute continuous speak " + (m1MinuteContinuousSpeak ?"ON":"OFF"), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        mFab.setVisibility(View.GONE);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onActionButton();
            }
        });

        return rootView;
    }

    //====================================================================================================
    private void onSetBaseTime() {

        // can't modify while running
        if (mTimer != null && mTimer.isRunning())
            return;

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View inputView = inflater.inflate(R.layout.text_time, null);

        final EditText txtInput = (EditText) (inputView.findViewById(R.id.textTime));

        txtInput.setHint("[[0:]00:]00");
        txtInput.setText(mCacheBaseString);
        txtInput.selectAll();

        AlertDialog dialog = new AlertDialog.Builder(this.getContext())
                .setTitle("Set Base Time")
                .setView(inputView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // if clicked then stop if its paused
                        if (mTimer != null) {
                            mTimer.Stop();
                            mTimer = null;
                            DismissNotify(getContext(), Integer.parseInt(UTTERANCE_ID));
                        }

                        String hMMss[] = txtInput.getText().toString().split(":");

                        for (int idx = 0; idx < hMMss.length; idx++) {
                            if (hMMss[idx].isEmpty())
                                hMMss[idx] = "0";
                        }

                        if (hMMss.length == 1) {
                            mBaseTimeMilliseconds = Long.parseLong(hMMss[0]) * 1000;
                        } else if (hMMss.length == 2) {
                            mBaseTimeMilliseconds = (Long.parseLong(hMMss[0]) * 60000) + (Long.parseLong(hMMss[1]) * 1000);
                        } else if (hMMss.length >= 3) {
                            mBaseTimeMilliseconds = (Long.parseLong(hMMss[0]) * 3600000) + (Long.parseLong(hMMss[1]) * 60000) + (Long.parseLong(hMMss[2]) * 1000);
                        }

                        if (mBaseTimeMilliseconds > MAX_BASE_TIME_MILLISECONDS) {
                            mTts.speak("It's way too big", TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
                            mBaseTimeMilliseconds = MAX_BASE_TIME_MILLISECONDS;
                        }

                        mCacheBaseString = String.format(Locale.getDefault(), "%d:%02d:%02d", (mBaseTimeMilliseconds / 3600000), (mBaseTimeMilliseconds / (60000)) % 60, (mBaseTimeMilliseconds / 1000) % 60);

                        mProgressBar.setProgress(0);
                        mTextCountdown.setText(mCacheBaseString);
                        mTextCountdown.setTextColor(mColorProgress);
                        mFab.setImageResource(android.R.drawable.ic_media_play);
                        mFab.setVisibility(View.VISIBLE);

                        if(mCacheBaseString.matches("^[0:]*$"))
                            mTts.speak("Zeroed", TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
                        else
                            mTts.speak("Setting " + mCacheBaseString, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
                    }
                })
                /*
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })*/
                .create();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    //====================================================================================================
    @Override
    protected void onActionButton() {
        if (mTimer == null) {

            if (mBaseTimeMilliseconds == 0)
                return;

            mProgressBar.setProgress(0);
            mTextCountdown.setTextColor(mColorProgress);
            mFab.setImageResource(android.R.drawable.ic_media_pause);

            mProgressbarDivisor = mBaseTimeMilliseconds / 100;

            mTts.speak("Starting", TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
            (mTimer = new Timer(this.getContext(), mBaseTimeMilliseconds, COUNT_DOWN_INTERVAL)).Start();

        } else if (mTimer.isRunning()) {
            mTts.speak("Pausing", TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
            mTimer.Pause();
            mFab.setImageResource(android.R.drawable.ic_media_play);
            Notify(getContext(), "Timer is paused.", mTextCountdown.getText().toString(), TimerFragment.PARAM_VALUE_OPEN_TIMER_FRAGMENT, Integer.parseInt(UTTERANCE_ID), mIconResId);
        } else {
            mTts.speak("Resuming", TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
            mTimer.Resume();
            mFab.setImageResource(android.R.drawable.ic_media_pause);
        }
    }


    //====================================================================================================
    //====================================================================================================
    class Timer extends TimeCounter {

        //====================================================================================================
        public Timer(Context context, long milliseconds, long counterInterval) {
            super(context, milliseconds, counterInterval);
        }

        //====================================================================================================
        @Override
        void onCounterInterval(long millis) {
            mProgressBar.setProgress((int) ((mBaseTimeMilliseconds - millis) / mProgressbarDivisor));

            if (millis == 0) {
                mTextCountdown.setText(getString(R.string.zero_time));
                mTextCountdown.setTextColor(mColorTimesUp);
                mFab.setImageResource(android.R.drawable.ic_media_play);
                mTts.speak("Time's up.", TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
                DismissNotify(getContext(), Integer.parseInt(UTTERANCE_ID));

                mTimer = null;
            } else {
                int sec = (int) ((millis / 1000) % 60);
                int min = (int) ((millis / (60000)) % 60);
                int hour = (int) (millis / 3600000);
                String text = String.format(Locale.getDefault(), "%d:%02d:%02d", hour, min, sec);

                mTextCountdown.setText(text);
                Notify(getContext(), "Timer is running.", text, TimerFragment.PARAM_VALUE_OPEN_TIMER_FRAGMENT, Integer.parseInt(UTTERANCE_ID), mIconResId);

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
