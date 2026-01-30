// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.videoplayer;

import androidx.annotation.NonNull;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlayerMessage;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages pause points for automatic video pausing at specific positions.
 *
 * <p>This class is self-contained and handles all pause point logic using ExoPlayer's PlayerMessage
 * API for frame-accurate timing. It can be easily copied when upgrading video_player versions.
 */
public class PausePointManager {

  /** Listener interface for pause point events. */
  public interface Listener {
    /**
     * Called when a pause point is reached.
     *
     * @param positionInMilliseconds the position where the pause occurred
     */
    void onPausePointReached(long positionInMilliseconds);
  }

  private final ExoPlayer exoPlayer;
  private final Listener listener;
  private final List<PlayerMessage> scheduledMessages = new ArrayList<>();

  /**
   * Creates a new PausePointManager.
   *
   * @param exoPlayer the ExoPlayer instance to manage pause points for
   * @param listener the listener to notify when pause points are reached
   */
  public PausePointManager(@NonNull ExoPlayer exoPlayer, @NonNull Listener listener) {
    this.exoPlayer = exoPlayer;
    this.listener = listener;
  }

  /**
   * Sets pause points at the specified positions.
   *
   * <p>Clears any existing pause points first. When playback reaches any of the specified
   * positions, the video will automatically pause and the listener will be notified.
   *
   * @param pausePointsInMilliseconds list of positions in milliseconds where the video should pause
   */
  public void setPausePoints(@NonNull List<Long> pausePointsInMilliseconds) {
    clearAllPausePoints();

    for (Long positionMs : pausePointsInMilliseconds) {
      if (positionMs != null) {
        PlayerMessage message = createPausePointMessage(positionMs);
        scheduledMessages.add(message);
        message.send();
      }
    }
  }

  /** Clears all scheduled pause points. */
  public void clearAllPausePoints() {
    for (PlayerMessage message : scheduledMessages) {
      message.cancel();
    }
    scheduledMessages.clear();
  }

  /** Call this when the player is disposed to clean up resources. */
  public void dispose() {
    clearAllPausePoints();
  }

  private PlayerMessage createPausePointMessage(Long positionMs) {
    return exoPlayer
        .createMessage(
            (messageType, payload) -> {
              exoPlayer.setPlayWhenReady(false);
              listener.onPausePointReached(positionMs);
            })
        .setLooper(exoPlayer.getApplicationLooper())
        .setPosition(positionMs)
        .setDeleteAfterDelivery(false); // Allow re-triggering on seek back
  }
}
