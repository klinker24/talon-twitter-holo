/*
 * Copyright 2014 Luke Klinker
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

package com.klinker.android.twitter.data;

public class Circle {

    public double radius;
    public double height;
    public double bounceTime;
    public double bounceSize;
    public double maxTime;
    public int color;
    public long startTime;

    public float x;
    public float y;

    public Circle(double height, double bounceTime, double bounceSize, int color, float x, float y, long delay) {
        this.radius = 0;
        this.height = height;
        this.bounceTime = bounceTime;
        this.bounceSize = bounceSize;
        this.maxTime = 3.04017 * bounceTime;
        this.color = color;
        this.startTime = System.currentTimeMillis() + delay;
        this.x = x;
        this.y = y;
    }
}
