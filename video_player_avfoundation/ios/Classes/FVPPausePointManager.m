// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#import "FVPPausePointManager.h"

@implementation FVPPausePointManager {
  AVPlayer *_player;
  id _boundaryObserver;
}

- (instancetype)initWithPlayer:(AVPlayer *)player {
  self = [super init];
  if (self) {
    _player = player;
  }
  return self;
}

- (void)setPausePoints:(NSArray<NSNumber *> *)pausePointsInMilliseconds {
  [self clearAllPausePoints];

  if (pausePointsInMilliseconds.count == 0) {
    return;
  }

  NSMutableArray<NSValue *> *times =
      [self convertMillisecondsArrayToCMTimes:pausePointsInMilliseconds];

  __weak typeof(self) weakSelf = self;
  _boundaryObserver = [_player addBoundaryTimeObserverForTimes:times
                                                         queue:dispatch_get_main_queue()
                                                    usingBlock:^{
                                                      __strong typeof(weakSelf) strongSelf =
                                                          weakSelf;
                                                      if (strongSelf) {
                                                        [strongSelf->_player pause];
                                                        CMTime currentTime =
                                                            strongSelf->_player.currentTime;
                                                        int64_t positionMs = (int64_t)(
                                                            CMTimeGetSeconds(currentTime) * 1000);
                                                        [strongSelf.delegate
                                                            pausePointReachedAtPosition:positionMs];
                                                      }
                                                    }];
}

- (void)clearAllPausePoints {
  if (_boundaryObserver) {
    [_player removeTimeObserver:_boundaryObserver];
    _boundaryObserver = nil;
  }
}

- (void)dispose {
  [self clearAllPausePoints];
  _player = nil;
}

- (NSMutableArray<NSValue *> *)convertMillisecondsArrayToCMTimes:
    (NSArray<NSNumber *> *)millisecondsArray {
  NSMutableArray<NSValue *> *times =
      [NSMutableArray arrayWithCapacity:millisecondsArray.count];

  int32_t timeScale = _player.currentItem.asset.duration.timescale;
  if (timeScale == 0) {
    timeScale = 1000;
  }

  for (NSNumber *ms in millisecondsArray) {
    if (ms != nil) {
      int64_t value = (ms.longLongValue * timeScale) / 1000;
      CMTime time = CMTimeMake(value, timeScale);
      [times addObject:[NSValue valueWithCMTime:time]];
    }
  }

  return times;
}

@end
