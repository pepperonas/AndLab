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
import android.graphics.Typeface;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.pepperonas.andlab.R;
import com.pepperonas.materialdialog.MaterialDialog;
import com.pepperonas.materialdialog.MaterialDialog.Builder;
import com.pepperonas.materialdialog.MaterialDialog.DismissListener;
import com.pepperonas.materialdialog.MaterialDialog.ShowListener;
import java.util.List;

/**
 * @author Martin Pfeffer
 * @see <a href="https://celox.io">https://celox.io</a>
 *
 * This dialog shows static data-plots (target data), which are given by the test scenario. A
 * measurement is related to these plots and gets visualisied in real-time on a second graph. After
 * the test ends all measurements are compared to the target data.
 *
 * NOTE: In this showcase the slope may reach a value too high to be interpreted correctly by {@link
 * DialogChartView#TOLERANCE}. This issue is caused by the randomized plot-generation and should not
 * occur in real world usage.
 */
public class DialogChartView {

    private static final String TAG = "DialogChartView";

    private static final double TOLERANCE = 2d;
    //    private static final long TIME_BETWEEN_UPDATES = 50;
    private static final long TIME_BETWEEN_UPDATES = 250;

    private GraphView mGraph;
    private double mGraphLastXValue = -1d;

    private final Handler mHandler = new Handler();
    private Runnable mTimer;

    private LineGraphSeries<DataPoint> mSeriesRealTime;
    private LineGraphSeries<DataPoint> mSeriesSimulation;

    private boolean mFocusRealTimeSeries = true;
    private boolean mShowOverlay = true;
    private String mMsg;

    public DialogChartView(final Context context) {
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

                    View overlayLeft = dialog.findViewById(R.id.overlay_left);
                    View overlayRight = dialog.findViewById(R.id.overlay_right);
                    if (!mShowOverlay) {
                        overlayLeft.setVisibility(View.INVISIBLE);
                        overlayRight.setVisibility(View.INVISIBLE);
                    }

                    mGraph = (GraphView) dialog.findViewById(R.id.graph);
                    initGraph();
                    setSimulationPoints();

                    mSeriesRealTime = new LineGraphSeries<>();
                    mGraph.addSeries(mSeriesRealTime);

                    mTimer = new Runnable() {
                        @Override
                        public void run() {
                            mGraphLastXValue += 1d;
                            mSeriesRealTime.appendData(
                                new DataPoint(mGraphLastXValue, getRandom()), false, 40);
                            mHandler.postDelayed(this, TIME_BETWEEN_UPDATES);

                            if (mFocusRealTimeSeries) {
                                focusRealTimeSeries();
                            }

                            if (mGraphLastXValue == 39) {
                                // end of test: stop and compute deviation for x-values
                                mHandler.removeCallbacks(mTimer);

                                checkDeviation(context);
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

    private void focusRealTimeSeries() {
        if (mGraphLastXValue > 10) {
            mGraph.getViewport().setMinX(mGraphLastXValue - 10);
            mGraph.getViewport().setMaxX(mGraphLastXValue + 10);
        }
    }

    private void initGraph() {
        mGraph.getViewport().setXAxisBoundsManual(true);
        mGraph.getViewport().setMinX(0);
        mGraph.getViewport().setMaxX(20);

        mGraph.getViewport().setYAxisBoundsManual(true);
        mGraph.getViewport().setMinY(-10);
        mGraph.getViewport().setMaxY(100);

        mGraph.getViewport().setScrollable(true);
    }

    private void checkDeviation(final Context context) {
        List<DataPoint> valuesSimulation = mSeriesSimulation.getDataPoints();
        List<DataPoint> valuesRealTime = mSeriesRealTime.getDataPoints();
        mMsg = "";

        if (valuesSimulation.size() != valuesRealTime.size()) {
            int sims = valuesSimulation.size();
            int rts = valuesRealTime.size();
            Log.w(TAG, "WARNING - Record corrupted? (sim=" + sims + " | rts=" + rts + ")");

        } else {
            PointsGraphSeries<DataPoint> seriesNice = new PointsGraphSeries<>();
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

                String log = "sim(t(" + frmt(2, i) + "))=" + frmt(ySim) + "|"
                    + "rt(t(" + frmt(2, i) + "))=" + frmt(yRt) + " "
                    + "->" + frmt(diff) + " " + info;
                Log.i(TAG, log);
                mMsg += (log + "\n");
            }
            mGraph.addSeries(seriesNice);
            mGraph.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    new MaterialDialog.Builder(context)
                        .title("Result")
                        .message(mMsg)
                        .font(Typeface.MONOSPACE)
                        .positiveText("OK")
                        .show();
                }
            });

        }
    }

    private String frmt(int precision, int value) {
        return String.format("%" + precision + "d", value);
    }

    private String frmt(double value) {
        return String.format("%5.2f", value);
    }

    private void setSimulationPoints() {
        mSeriesSimulation = new LineGraphSeries<>(
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

        mGraph.addSeries(mSeriesSimulation);
    }

    private double mLastRandom = 2;

    private double getRandom() {
        mLastRandom++;
        return Math.abs(Math.sin(mLastRandom * 0.5) * 10 * (Math.random() * 10 + 1));
    }

}
