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
package com.google.android.material.motion.sources;

import android.app.Activity;
import android.view.View;

import com.google.android.material.motion.ConstraintApplicator;
import com.google.android.material.motion.MapOperation;
import com.google.android.material.motion.MotionObservable;
import com.google.android.material.motion.MotionRuntime;
import com.google.android.material.motion.gestures.BuildConfig;
import com.google.android.material.motion.gestures.GestureInteraction;
import com.google.android.material.motion.gestures.testing.SimulatedGestureRecognizer;
import com.google.android.material.motion.testing.TrackingMotionObserver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;

import static com.google.android.material.motion.gestures.GestureRecognizer.BEGAN;
import static com.google.android.material.motion.gestures.GestureRecognizer.CHANGED;
import static com.google.android.material.motion.gestures.GestureRecognizer.POSSIBLE;
import static com.google.android.material.motion.gestures.GestureRecognizer.RECOGNIZED;
import static com.google.android.material.motion.MotionState.ACTIVE;
import static com.google.android.material.motion.MotionState.AT_REST;
import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class GestureSourceTests {

  private SimulatedGestureRecognizer gesture;

  @Before
  public void setUp() {
    View view = new View(Robolectric.setupActivity(Activity.class));
    gesture = new SimulatedGestureRecognizer(view);
    view.setOnTouchListener(gesture);
  }

  @Test
  public void createSource() {
    GestureInteraction<SimulatedGestureRecognizer, ?> interaction = createInteraction(gesture);
    GestureSource
      .from(interaction)
      .subscribe()
      .unsubscribe();
  }

  @Test
  public void propogatesInitialState() {
    TrackingMotionObserver<SimulatedGestureRecognizer> tracker = new TrackingMotionObserver<>();

    GestureInteraction<SimulatedGestureRecognizer, ?> interaction = createInteraction(gesture);
    GestureSource
      .from(interaction)
      .subscribe(tracker);

    assertThat(interaction.state.read()).isEqualTo(AT_REST);
  }

  @Test
  public void propogatesValueChanges() {
    TrackingMotionObserver<Integer> tracker = new TrackingMotionObserver<>();

    GestureInteraction<SimulatedGestureRecognizer, ?> interaction = createInteraction(gesture);
    GestureSource
      .from(interaction)
      .compose(new MapOperation<SimulatedGestureRecognizer, Integer>() {
        @Override
        public Integer transform(SimulatedGestureRecognizer value) {
          return value.getState();
        }
      })
      .subscribe(tracker);

    gesture.setState(BEGAN);
    assertThat(tracker.values).isEqualTo(Arrays.asList(POSSIBLE, BEGAN));

    gesture.setState(CHANGED);
    assertThat(tracker.values).isEqualTo(Arrays.asList(POSSIBLE, BEGAN, CHANGED));

    gesture.setState(RECOGNIZED);
    assertThat(tracker.values).isEqualTo(Arrays.asList(POSSIBLE, BEGAN, CHANGED, RECOGNIZED, POSSIBLE));
  }

  @Test
  public void propogatesMotionStateChanges() {
    TrackingMotionObserver<SimulatedGestureRecognizer> tracker = new TrackingMotionObserver<>();

    GestureInteraction<SimulatedGestureRecognizer, ?> interaction = createInteraction(gesture);
    GestureSource
      .from(interaction)
      .subscribe(tracker);

    assertThat(interaction.state.read()).isEqualTo(AT_REST);

    gesture.setState(BEGAN);
    assertThat(interaction.state.read()).isEqualTo(ACTIVE);

    gesture.setState(CHANGED); // Should no-op.
    assertThat(interaction.state.read()).isEqualTo(ACTIVE);

    gesture.setState(RECOGNIZED);
    assertThat(interaction.state.read()).isEqualTo(AT_REST);
  }

  private GestureInteraction<SimulatedGestureRecognizer, ?> createInteraction(
    SimulatedGestureRecognizer gesture) {
    return new GestureInteraction<SimulatedGestureRecognizer, Object>(gesture) {
      @Override
      protected void onApply(
        MotionRuntime runtime,
        MotionObservable<SimulatedGestureRecognizer> stream,
        View target, ConstraintApplicator<Object> constraints) {
        throw new UnsupportedOperationException();
      }
    };
  }
}
