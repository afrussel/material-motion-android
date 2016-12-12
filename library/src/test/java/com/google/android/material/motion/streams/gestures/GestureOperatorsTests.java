/*
 * Copyright 2016-present The Material Motion Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.motion.streams.gestures;

import android.app.Activity;
import android.graphics.PointF;
import android.view.View;

import com.google.android.material.motion.gestures.BuildConfig;
import com.google.android.material.motion.gestures.testing.SimulatedGestureRecognizer;
import com.google.android.material.motion.streams.testing.TrackingMotionObserver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;

import static com.google.android.material.motion.streams.MotionObservable.ACTIVE;
import static com.google.android.material.motion.streams.MotionObservable.AT_REST;
import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class GestureOperatorsTests {

  private SimulatedGestureRecognizer gesture;

  @Before
  public void setUp() {
    View view = new View(Robolectric.setupActivity(Activity.class));
    gesture = new SimulatedGestureRecognizer(view);
    view.setOnTouchListener(gesture);
  }

  @Test
  public void extractsCentroid() {
    TrackingMotionObserver<PointF> tracker = new TrackingMotionObserver<>();

    GestureSource
      .of(gesture)
      .extend(GestureOperators.centroid())
      .subscribe(tracker);

    gesture.setCentroid(5f, 5f);

    assertThat(tracker.values).isEqualTo(Arrays.asList(new PointF(0f, 0f), new PointF(5f, 5f)));
    assertThat(tracker.states).isEqualTo(Arrays.asList(AT_REST, ACTIVE));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void constructorIsDisabled() {
    new GestureOperators();
  }
}
