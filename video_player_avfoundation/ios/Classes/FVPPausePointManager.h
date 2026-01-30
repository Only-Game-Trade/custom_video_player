// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#import <AVFoundation/AVFoundation.h>

NS_ASSUME_NONNULL_BEGIN

/**
 * Protocol for receiving pause point events.
 */
@protocol FVPPausePointManagerDelegate <NSObject>
/**
 * Called when a pause point is reached.
 * @param positionInMilliseconds The position where the pause occurred.
 */
- (void)pausePointReachedAtPosition:(int64_t)positionInMilliseconds;
@end

/**
 * Manages pause points for automatic video pausing at specific positions.
 *
 * This class is self-contained and handles all pause point logic using
 * AVPlayer's boundary time observer API for accurate timing. It can be easily
 * copied when upgrading video_player versions.
 */
@interface FVPPausePointManager : NSObject

/** Delegate to receive pause point events. */
@property(nonatomic, weak, nullable) id<FVPPausePointManagerDelegate> delegate;

/**
 * Initializes a new pause point manager.
 * @param player The AVPlayer instance to manage pause points for.
 * @return A new FVPPausePointManager instance.
 */
- (instancetype)initWithPlayer:(AVPlayer *)player;

/**
 * Sets pause points at the specified positions.
 * Clears any existing pause points first. When playback reaches any of the
 * specified positions, the video will automatically pause and the delegate
 * will be notified.
 * @param pausePointsInMilliseconds Array of positions in milliseconds where
 *                                  the video should pause.
 */
- (void)setPausePoints:(NSArray<NSNumber *> *)pausePointsInMilliseconds;

/**
 * Clears all scheduled pause points.
 */
- (void)clearAllPausePoints;

/**
 * Disposes of the manager and cleans up resources.
 */
- (void)dispose;

@end

NS_ASSUME_NONNULL_END
