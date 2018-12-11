/*
 * Copyright (c) 2005, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.sample;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MyBenchmark {

    @State(Scope.Thread)
    public static class MyState {

        @Param({"1000", "1000000"})
        public int mapSize;
        @Param({"100", "200"})
        public int vectorSize;

        public final Map<Integer, Vector> vectors = new HashMap<Integer, Vector>(mapSize);
        public final List<Vector> vectorList = new ArrayList<Vector>(mapSize);
        public int sumAllValues;

        @Setup
        public void setup() {
            for (int i = 0; i < mapSize; i++) {
                Vector p = new Vector(i, i * 2, vectorSize);
                vectors.put(i, p);
                vectorList.add(p);
            }
        }
    }

//    @Benchmark
//    @BenchmarkMode(Mode.Throughput)
//    @OutputTimeUnit(TimeUnit.MINUTES)
//    public void testMapAddString(MyState myState) {
//        // Sum by key lookup.
//        for (int i = 0; i < myState.mapSize; i++) {
//            myState.sumAllValues += Integer.parseInt(myState.vectors.get(i).intString);
//        }
//    }

//    @Benchmark
//    @BenchmarkMode(Mode.Throughput)
//    @OutputTimeUnit(TimeUnit.MINUTES)
//    public void testReferenceAddString(MyState myState) {
//        // Sum by key lookup.
//        for (Vector p : myState.vectors.values()) {
//            myState.sumAllValues += Integer.parseInt(p.intString);
//        }
//    }

//    @Benchmark
//    @BenchmarkMode(Mode.Throughput)
//    @OutputTimeUnit(TimeUnit.MINUTES)
//    public void testMapAddInt(MyState myState) {
//        // Sum by key lookup.
//        for (int i = 0; i < myState.mapSize; i++) {
//            myState.sumAllValues += myState.vectors.get(i).values;
//        }
//    }

//    @Benchmark
//    @BenchmarkMode(Mode.Throughput)
//    @OutputTimeUnit(TimeUnit.MINUTES)
//    public void testReferenceAddInt(MyState myState) {
//        // Sum by key lookup.
//        for (Vector p : myState.vectorList) {
//            myState.sumAllValues += p.values;
//        }
//    }

//    @Benchmark
//    @BenchmarkMode(Mode.Throughput)
//    @OutputTimeUnit(TimeUnit.MINUTES)
//    public void testMapAddStringMultiHit(MyState myState) {
//        // Sum by key lookup.
//        for (int i = 0; i < myState.mapSize * 10; i++) {
//            myState.sumAllValues += Integer.parseInt(myState.vectors.get(i % myState.mapSize).intString)
//                                    + System.currentTimeMillis() % 10;
//        }
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.Throughput)
//    @OutputTimeUnit(TimeUnit.MINUTES)
//    public void testReferenceAddString(MyState myState) {
//        // Sum by key lookup.
//        for (int i = 0; i < 10; i++) {
//            for (Vector p : myState.vectorList) {
//                myState.sumAllValues += Integer.parseInt(p.intString) + System.currentTimeMillis() % 10;
//            }
//        }
//    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MINUTES)
    public void testMapDotProduct(MyState myState) {
        // Sum by key lookup.
        Vector last = myState.vectors.get(0);
        for (int i = 1; i < myState.mapSize; i++) {
            Vector current = myState.vectors.get(i);
            myState.sumAllValues += dotProduct(last.values, current.values);
            last = current;
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MINUTES)
    public void testReferenceDotProduct(MyState myState) {

        // Sum by key lookup.
        Iterator<Vector> iter = myState.vectorList.iterator();
        Vector last = iter.next();
        while (iter.hasNext()) {
            Vector current = iter.next();
            myState.sumAllValues += dotProduct(last.values, current.values);
            last = current;
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MINUTES)
    public long testReferenceDotProductWithSystemCall(MyState myState) {

        // Sum by key lookup.
        Iterator<Vector> iter = myState.vectorList.iterator();
        Vector last = iter.next();
        long currentMs = -1L;
        while (iter.hasNext()) {
            Vector current = iter.next();
            myState.sumAllValues += dotProduct(last.values, current.values);
            last = current;
            currentMs = System.currentTimeMillis();
        }
        return currentMs;
    }

    private long dotProduct(long[] a, long[] b) {
        int sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    public static class Vector {
        public final int id;
        public final String intString;
        public final long[] values;

        public Vector(int id, int startingValue, int size) {
            this.id = id;
            this.intString = Integer.toString(startingValue);
            this.values = new long[size];
            for (int i = 0; i < size; i++) {
                values[i] = startingValue + i;
            }
        }
    }
}
