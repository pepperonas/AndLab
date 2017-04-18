/*
 * Copyright (c) 2017 Martin Pfeffer
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

package com.pepperonas.andlab.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.pepperonas.andlab.R;
import com.pepperonas.materialdialog.MaterialDialog.Builder;
import com.pepperonas.materialdialog.MaterialDialog.DismissListener;
import com.pepperonas.materialdialog.MaterialDialog.ShowListener;
import java.util.List;
import java.util.Random;

/**
 * @author Martin Pfeffer
 * @see <a href="https://celox.io">https://celox.io</a>
 */
public class DialogChartView {

    private static final String TAG = "DialogChartView";
    private static final double TOLERANCE = 2d;

    private final Handler mHandler = new Handler();
    private Runnable mTimer;
    private double graphLastXValue = -1d;
    private LineGraphSeries<DataPoint> mSeriesRealTime;
    private LineGraphSeries<DataPoint> mSeriesSimulation;

    public DialogChartView(Context context) {
        new Builder(context)
            .title("Drive Sim")
            .positiveText("ok")
            .customView(R.layout.dialog_chart_view)
            .showListener(new ShowListener() {
                @Override
                public void onShow(AlertDialog dialog) {
                    super.onShow(dialog);

                    // full screen
                    dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT);

                    final GraphView graph = (GraphView) dialog.findViewById(R.id.graph);
                    initGraph(graph);

                    loadSimulationPoints();

                    graph.addSeries(mSeriesSimulation);

                    mTimer = new Runnable() {
                        @Override
                        public void run() {
                            graphLastXValue += 1d;
                            mSeriesRealTime.appendData(
                                new DataPoint(graphLastXValue, getRandom()), false, 40);
                            mHandler.postDelayed(this, 50);

                            if (graphLastXValue == 39) {
                                // end of test: stop and compute deviation for x-values
                                mHandler.removeCallbacks(mTimer);

                                checkDeviation(graph);
                            }

                        }
                    };
                    mHandler.postDelayed(mTimer, 700);

                }
            })
            .dismissListener(new DismissListener() {
                @Override
                public void onDismiss() {
                    super.onDismiss();
                    mHandler.removeCallbacks(mTimer);
                }
            })
            .show();
    }

    private void checkDeviation(GraphView graph) {
        List<DataPoint> valuesSimulation = mSeriesSimulation.getDataPoints();
        List<DataPoint> valuesRealTime = mSeriesRealTime.getDataPoints();

        Log.d(TAG, "checkDeviation valuesSimulation.size()=" + valuesSimulation.size());
        Log.d(TAG, "checkDeviation valuesRealTime.size()=" + valuesRealTime.size());
        if (valuesSimulation.size() != valuesRealTime.size()) {
            Log.w(TAG, "checkDeviation: WARNING - Record corrupted?");
        } else {

            PointsGraphSeries<DataPoint> seriesNice = new PointsGraphSeries<DataPoint>();
            seriesNice.setColor(Color.GREEN);
            seriesNice.setSize(30);

            for (int i = 0; i < valuesSimulation.size(); i++) {
                double ySim = valuesSimulation.get(i).getY();
                double yRt = valuesRealTime.get(i).getY();
                double diff = Math.abs(ySim - yRt);
                String info = "too";
                info += ySim > yRt ? " slow" : " fast";
                if (diff < TOLERANCE && diff > -TOLERANCE) {
                    info = "   NICE!";
                    seriesNice.appendData(new DataPoint(i, yRt), false, 40);
                }

                String msg = "sim(" + frmt(2, i) + ") = " + frmt(ySim) + " | "
                    + "rt(" + frmt(2, i) + ") = " + frmt(yRt) + " "
                    + " -> " + frmt(diff) + " " + info;
                Log.i(TAG, msg);
            }
            graph.addSeries(seriesNice);

        }
    }

    private String frmt(int precision, int value) {
        return String.format("%" + precision + "d", value);
    }

    private String frmt(double value) {
        return String.format("%6.2f", value);
    }

    public void initGraph(GraphView graph) {
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(40);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(-10);
        graph.getViewport().setMaxY(100);

        mSeriesRealTime = new LineGraphSeries<>();
        graph.addSeries(mSeriesRealTime);
    }

    private void loadSimulationPoints() {
        mSeriesSimulation = new LineGraphSeries<DataPoint>(
            new DataPoint[]{
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6),
                new DataPoint(5, 6),
                new DataPoint(6, 8),
                new DataPoint(7, 10),
                new DataPoint(8, 14),
                new DataPoint(9, 20),
                new DataPoint(10, 21),
                new DataPoint(11, 22),
                new DataPoint(12, 23),
                new DataPoint(13, 24),
                new DataPoint(14, 27),
                new DataPoint(15, 30),
                new DataPoint(16, 35),
                new DataPoint(17, 40),
                new DataPoint(18, 46),
                new DataPoint(19, 50),
                new DataPoint(20, 53),
                new DataPoint(21, 55),
                new DataPoint(22, 56),
                new DataPoint(23, 56),
                new DataPoint(24, 56),
                new DataPoint(25, 58),
                new DataPoint(26, 60),
                new DataPoint(27, 62),
                new DataPoint(28, 64),
                new DataPoint(29, 66),
                new DataPoint(30, 71),
                new DataPoint(31, 72),
                new DataPoint(32, 73),
                new DataPoint(33, 74),
                new DataPoint(34, 77),
                new DataPoint(35, 81),
                new DataPoint(36, 83),
                new DataPoint(37, 83),
                new DataPoint(38, 85),
                new DataPoint(39, 90)
            });
    }

    double mLastRandom = 2;
    Random mRand = new Random();

    private double getRandom() {
        mLastRandom++;
        return Math.abs(Math.sin(mLastRandom * 0.5) * 10 * (Math.random() * 10 + 1));
    }


    private class Point {

        double x;
        double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

}
