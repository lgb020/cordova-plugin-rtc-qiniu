package cordova.plugin.qnrtc.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import cordova.plugin.qnrtc.QNRtc;

/**
 * Fragment for call control.
 */
public class ControlFragment extends Fragment {
    private View mControlView;
    private ImageButton mDisconnectButton;
    private ImageButton mCameraSwitchButton;
    private ImageButton mToggleMuteButton;
    private ImageButton mToggleBeautyButton;
    private ImageButton mToggleSpeakerButton;
    private ImageButton mToggleVideoButton;
    private ImageButton mLogShownButton;
    private LinearLayout mLogView;
    private TextView mLocalTextView;
    private TextView mRemoteTextView;
    private StringBuffer mRemoteLogText;
    private Chronometer mTimer;
    private OnCallEvents mCallEvents;
    private boolean mIsVideoEnabled = true;
    private boolean mIsShowingLog = false;
    private boolean mIsScreenCaptureEnabled = false;
    private boolean mIsAudioOnly = false;

    /**
     * Call control interface for container activity.
     */
    public interface OnCallEvents {
        void onCallHangUp();

        void onCameraSwitch();

        boolean onToggleMic();

        boolean onToggleVideo();

        boolean onToggleSpeaker();

        boolean onToggleBeauty();

    }

    public void setScreenCaptureEnabled(boolean isScreenCaptureEnabled) {
        mIsScreenCaptureEnabled = isScreenCaptureEnabled;
    }

    public void setAudioOnly(boolean isAudioOnly) {
        mIsAudioOnly = isAudioOnly;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mControlView = inflater.inflate(QNRtc.getResourceId("fragment_room", "layout"), container, false);//R.layout.fragment_room

        mDisconnectButton = (ImageButton) mControlView.findViewById(QNRtc.getResourceId("disconnect_button", "id"));//R.id.disconnect_button
        mCameraSwitchButton = (ImageButton) mControlView.findViewById(QNRtc.getResourceId("camera_switch_button", "id"));//R.id.camera_switch_button
        mToggleBeautyButton = (ImageButton) mControlView.findViewById(QNRtc.getResourceId("beauty_button", "id"));//R.id.beauty_button
        mToggleMuteButton = (ImageButton) mControlView.findViewById(QNRtc.getResourceId("microphone_button", "id"));//R.id.microphone_button
        mToggleSpeakerButton = (ImageButton) mControlView.findViewById(QNRtc.getResourceId("speaker_button", "id"));//R.id.speaker_button
        mToggleVideoButton = (ImageButton) mControlView.findViewById(QNRtc.getResourceId("camera_button", "id"));//R.id.camera_button
        mLogShownButton = (ImageButton) mControlView.findViewById(QNRtc.getResourceId("log_shown_button", "id"));//R.id.log_shown_button
        mLogView = (LinearLayout) mControlView.findViewById(QNRtc.getResourceId("log_text", "id"));//R.id.log_text
        mLocalTextView = (TextView) mControlView.findViewById(QNRtc.getResourceId("local_log_text", "id"));//R.id.local_log_text
        mLocalTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        mRemoteTextView = (TextView) mControlView.findViewById(QNRtc.getResourceId("remote_log_text", "id"));//R.id.remote_log_text
        mRemoteTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        mTimer = (Chronometer) mControlView.findViewById(QNRtc.getResourceId("timer", "id"));//R.id.timer

        mDisconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallEvents.onCallHangUp();
            }
        });

        if (!mIsScreenCaptureEnabled && !mIsAudioOnly) {
            mCameraSwitchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCallEvents.onCameraSwitch();
                }
            });
        }

        if (!mIsScreenCaptureEnabled && !mIsAudioOnly) {
            mToggleBeautyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean enabled = mCallEvents.onToggleBeauty();
                    mToggleBeautyButton.setImageResource(enabled ?
                            QNRtc.getResourceId("face_beauty_open", "mipmap") //R.mipmap.face_beauty_open
                            :
                            QNRtc.getResourceId("face_beauty_close", "mipmap") //R.mipmap.face_beauty_close
                    );
                }
            });
        }

        mToggleMuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean enabled = mCallEvents.onToggleMic();
                mToggleMuteButton.setImageResource(enabled ?
                        QNRtc.getResourceId("microphone", "mipmap")//R.mipmap.microphone
                        :
                        QNRtc.getResourceId("microphone_disable", "mipmap")//R.mipmap.microphone_disable
                );
            }
        });

        if (mIsScreenCaptureEnabled || mIsAudioOnly) {
            mToggleVideoButton.setImageResource(QNRtc.getResourceId("video_close", "mipmap"));//R.mipmap.video_close
        } else {
            mToggleVideoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean enabled = mCallEvents.onToggleVideo();
                    mToggleVideoButton.setImageResource(enabled ?
                            QNRtc.getResourceId("video_open", "mipmap")//R.mipmap.video_open
                            :
                            QNRtc.getResourceId("video_close", "mipmap")//R.mipmap.video_close
                    );
                }
            });
        }

        mToggleSpeakerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean enabled = mCallEvents.onToggleSpeaker();
                mToggleSpeakerButton.setImageResource(enabled ?
                        QNRtc.getResourceId("loudspeaker", "mipmap")//R.mipmap.loudspeaker
                        :
                        QNRtc.getResourceId("loudspeaker_disable", "mipmap")//R.mipmap.loudspeaker_disable
                );
            }
        });

        mLogShownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLogView.setVisibility(mIsShowingLog ? View.INVISIBLE : View.VISIBLE);
                mIsShowingLog = !mIsShowingLog;
            }
        });
        return mControlView;
    }

    public void startTimer() {
        mTimer.setBase(SystemClock.elapsedRealtime());
        mTimer.start();
    }

    public void stopTimer() {
        mTimer.stop();
    }

    public void updateLocalLogText(String logText) {
        if (mLogView.getVisibility() == View.VISIBLE) {
            mLocalTextView.setText(logText);
        }
    }

    public void updateRemoteLogText(String logText) {
        if (mRemoteLogText == null) {
            mRemoteLogText = new StringBuffer();
        }
        if (mLogView != null) {
            mRemoteTextView.setText(mRemoteLogText.append(logText + "\n"));
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!mIsVideoEnabled) {
            mCameraSwitchButton.setVisibility(View.INVISIBLE);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallEvents = (OnCallEvents) activity;
    }
}