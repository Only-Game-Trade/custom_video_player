# Auto-Pause Feature Patch for video_player 2.7.1

This document describes the auto-pause functionality added to the `video_player` plugin. This feature allows you to set specific positions in the video where playback will automatically pause.

## Feature Overview

The auto-pause feature enables:
- Setting multiple pause points at specific timestamps
- Automatic pausing when playback reaches any pause point
- Stream notifications when auto-pause is triggered
- Frame-accurate timing using native APIs (ExoPlayer's PlayerMessage on Android, AVPlayer's boundary time observer on iOS)

## API Usage

```dart
// Set pause points
await controller.setPausePoints([
  Duration(seconds: 10),
  Duration(seconds: 30),
  Duration(minutes: 1, seconds: 15),
]);

// Listen for auto-pause events
controller.onAutoPause.listen((position) {
  print('Video paused at $position');
  // Resume playback or show UI
});

// Clear all pause points
await controller.clearAllPausePoints();
```

## Files Added (NEW)

These are standalone files that can be easily copied when upgrading:

| Platform | File | Description |
|----------|------|-------------|
| Android | `video_player_android/.../PausePointManager.java` | All ExoPlayer pause point logic |
| iOS | `video_player_avfoundation/.../FVPPausePointManager.h` | Header for pause point manager |
| iOS | `video_player_avfoundation/.../FVPPausePointManager.m` | All AVPlayer pause point logic |

## Files Modified

All modifications are marked with `#region auto-pause` comments for easy identification.

### Platform Interface
- `video_player_platform_interface/lib/video_player_platform_interface.dart`
  - Added `autoPause` to `VideoEventType` enum
  - Added `autoPausePosition` field to `VideoEvent`
  - Added `setPausePoints()` and `clearAllPausePoints()` abstract methods

### Android
- `video_player_android/pigeons/messages.dart`
  - Added `PausePointsMessage` class
  - Added `setPausePoints()` and `clearAllPausePoints()` methods to API

- `video_player_android/.../VideoPlayer.java`
  - Added `pausePointManager` field
  - Added initialization and delegation methods
  - Added dispose cleanup

- `video_player_android/.../VideoPlayerPlugin.java`
  - Added import for `PausePointsMessage`
  - Implemented `setPausePoints()` and `clearAllPausePoints()` methods

- `video_player_android/lib/src/android_video_player.dart`
  - Added `setPausePoints()` and `clearAllPausePoints()` implementations
  - Added `autoPause` event handling

### iOS
- `video_player_avfoundation/pigeons/messages.dart`
  - Added `PausePointsMessage` class
  - Added `setPausePoints()` and `clearAllPausePoints()` methods to API

- `video_player_avfoundation/.../FLTVideoPlayerPlugin.m`
  - Added import for `FVPPausePointManager.h`
  - Added `FVPPausePointManagerDelegate` conformance
  - Added `pausePointManager` property
  - Added initialization, delegation, and dispose cleanup

- `video_player_avfoundation/lib/src/avfoundation_video_player.dart`
  - Added `setPausePoints()` and `clearAllPausePoints()` implementations
  - Added `autoPause` event handling

### Main Package
- `video_player/lib/video_player.dart`
  - Added `_autoPauseStreamController` field
  - Added `onAutoPause` stream getter
  - Added `setPausePoints()` and `clearAllPausePoints()` methods
  - Added `autoPause` event handling in event listener
  - Added stream controller cleanup in dispose

## Upgrading to a Newer video_player Version

When upgrading to a newer version of `video_player`:

1. **Copy the new manager files:**
   - `PausePointManager.java` (Android)
   - `FVPPausePointManager.h` and `FVPPausePointManager.m` (iOS)

2. **Search for `#region auto-pause` comments** in this patched version to find all modifications

3. **Apply the same changes** to the new version:
   - Platform interface: enum value, field, and abstract methods
   - Pigeon messages: message class and API methods
   - Native integration: field, initialization, delegation, dispose
   - Dart implementations: method implementations and event handling
   - Main package: stream controller, methods, event handling

4. **Regenerate Pigeon code:**
   ```bash
   cd video_player_android && dart run pigeon --input pigeons/messages.dart
   cd video_player_avfoundation && dart run pigeon --input pigeons/messages.dart
   ```

## Base Version

This patch is based on `video_player` version **2.7.1** from the official Flutter packages repository.

- Tag: `video_player-v2.7.1`
- Commit: `b4985e25fe0763ece3cfd7af58e0e8c9b9f04fc4`
