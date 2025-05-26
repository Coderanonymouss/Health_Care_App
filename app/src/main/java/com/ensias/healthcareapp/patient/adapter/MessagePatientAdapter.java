package com.ensias.healthcareapp.patient.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.model.Message;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MessagePatientAdapter extends FirestoreRecyclerAdapter<Message, MessagePatientAdapter.MessageHolder> {
    private Context context;
    private MediaPlayer mediaPlayer;
    private LottieAnimationView lastWaveView; // для анимации волны возле плеера

    public MessagePatientAdapter(@NonNull FirestoreRecyclerOptions<Message> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull MessageHolder holder, int position, @NonNull Message model) {
        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        boolean isOwn = model.getSender().equals(currentUser);

        holder.leftLayout.setVisibility(View.GONE);
        holder.rightLayout.setVisibility(View.GONE);

        String time = "";
        if (model.getDateCreated() != null) {
            Date date = model.getDateCreated().toDate();
            time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
        }

        if (isOwn) {
            holder.rightLayout.setVisibility(View.VISIBLE);
            setUpBubble(holder, model, true, time);
            holder.timeRight.setText(time);
        } else {
            holder.leftLayout.setVisibility(View.VISIBLE);
            setUpBubble(holder, model, false, time);
            holder.timeLeft.setText(time);
        }
    }

    private void setUpBubble(MessageHolder holder, Message model, boolean isOwn, String time) {
        TextView text = isOwn ? holder.textRight : holder.textLeft;
        ImageView image = isOwn ? holder.imageRight : holder.imageLeft;
        ImageView audio = isOwn ? holder.audioRight : holder.audioLeft;
        TextView audioDuration = isOwn ? holder.audioDurationRight : holder.audioDurationLeft;
        LottieAnimationView audioWave = isOwn ? holder.waveRight : holder.waveLeft;
        TextView senderName = isOwn ? holder.senderNameRight : holder.senderNameLeft;

        text.setVisibility(View.GONE);
        image.setVisibility(View.GONE);
        audio.setVisibility(View.GONE);
        audioDuration.setVisibility(View.GONE);
        audioWave.setVisibility(View.INVISIBLE);
        senderName.setVisibility(View.VISIBLE);
        senderName.setText(model.getSenderName());

        switch (model.getType()) {
            case "text":
                text.setVisibility(View.VISIBLE);
                text.setText(model.getText());
                break;
            case "image":
                image.setVisibility(View.VISIBLE);
                Picasso.get().load(model.getFileUrl()).into(image);
                break;
            case "audio":
                audio.setVisibility(View.VISIBLE);
                audioDuration.setVisibility(View.VISIBLE);
                int durationMs = model.getDuration();
                if (durationMs > 0) {
                    audioDuration.setText(formatDuration(durationMs));
                } else {
                    audioDuration.setText("--:--");
                }
                audio.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                audio.setOnClickListener(v -> {
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                        if (lastWaveView != null) {
                            lastWaveView.setVisibility(View.INVISIBLE);
                            lastWaveView.cancelAnimation();
                        }
                    }
                    mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(model.getFileUrl());
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        audioWave.setVisibility(View.VISIBLE);
                        audioWave.playAnimation();
                        lastWaveView = audioWave;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mediaPlayer.setOnCompletionListener(mp -> {
                        audioWave.setVisibility(View.INVISIBLE);
                        audioWave.cancelAnimation();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    });
                });
                break;
        }
    }

    private String formatDuration(int durationMs) {
        int seconds = durationMs / 1000;
        int min = seconds / 60;
        int sec = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", min, sec);
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_chat_item, parent, false);
        return new MessageHolder(v);
    }

    public static class MessageHolder extends RecyclerView.ViewHolder {
        LinearLayout leftLayout, rightLayout;
        TextView textLeft, textRight, timeLeft, timeRight, senderNameLeft, senderNameRight;
        ImageView imageLeft, imageRight, audioLeft, audioRight;
        TextView audioDurationLeft, audioDurationRight;
        LottieAnimationView waveLeft, waveRight;
        public MessageHolder(View itemView) {
            super(itemView);
            leftLayout = itemView.findViewById(R.id.leftMessageLayout);
            rightLayout = itemView.findViewById(R.id.rightMessageLayout);
            textLeft = itemView.findViewById(R.id.text_left);
            textRight = itemView.findViewById(R.id.text_right);
            timeLeft = itemView.findViewById(R.id.time_left);
            timeRight = itemView.findViewById(R.id.time_right);
            imageLeft = itemView.findViewById(R.id.image_left);
            imageRight = itemView.findViewById(R.id.image_right);
            audioLeft = itemView.findViewById(R.id.audio_left);
            audioRight = itemView.findViewById(R.id.audio_right);
            audioDurationLeft = itemView.findViewById(R.id.audio_duration_left);
            audioDurationRight = itemView.findViewById(R.id.audio_duration_right);
            waveLeft = itemView.findViewById(R.id.audio_wave_left);
            waveRight = itemView.findViewById(R.id.audio_wave_right);
            senderNameLeft = itemView.findViewById(R.id.sender_name_left);
            senderNameRight = itemView.findViewById(R.id.sender_name_right);
        }
    }
}

